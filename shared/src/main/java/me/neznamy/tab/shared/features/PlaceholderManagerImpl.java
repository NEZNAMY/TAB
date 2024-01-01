package me.neznamy.tab.shared.features;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.PlaceholderRefreshTask;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.event.impl.TabPlaceholderRegisterEvent;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.placeholders.types.PlayerPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.types.RelationalPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.types.ServerPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.types.TabPlaceholder;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import org.jetbrains.annotations.NotNull;

/**
 * Messy class for placeholder management
 */
public class PlaceholderManagerImpl extends TabFeature implements PlaceholderManager, JoinListener, Loadable,
        Refreshable {

    private final Pattern placeholderPattern = Pattern.compile("%([^%]*)%");

    @Getter private final String featureName = "Refreshing placeholders";
    @Getter private final String refreshDisplayName = "Other";

    private final boolean registerExpansion = config().getBoolean("placeholders.register-tab-expansion", true);
    private final Map<String, Integer> refreshIntervals = config().getConfigurationSection("placeholderapi-refresh-intervals");
    private final int defaultRefresh;

    private final Map<String, Placeholder> registeredPlaceholders = new HashMap<>();

    //map of String-Set of features using placeholder
    private final Map<String, Set<Refreshable>> placeholderUsage = new ConcurrentHashMap<>();
    private Placeholder[] usedPlaceholders = new Placeholder[0];

    @Getter private int loopTime;

    @NotNull @Getter private final TabExpansion tabExpansion = registerExpansion ?
            TAB.getInstance().getPlatform().createTabExpansion() : new EmptyTabExpansion();

    /**
     * Constructs new instance and loads refresh intervals from config.
     */
    public PlaceholderManagerImpl() {
        TAB.getInstance().getConfigHelper().startup().fixRefreshIntervals(refreshIntervals);
        defaultRefresh = refreshIntervals.getOrDefault("default-refresh-interval", 500);
    }

    private void refresh() {
        loopTime += TabConstants.Placeholder.MINIMUM_REFRESH_INTERVAL;
        List<Placeholder> placeholders = new ArrayList<>();
        for (Placeholder placeholder : usedPlaceholders) {
            if (placeholder.getRefresh() == -1 || loopTime % placeholder.getRefresh() != 0) continue;
            placeholders.add(placeholder);
        }
        if (placeholders.isEmpty()) return;
        PlaceholderRefreshTask task = new PlaceholderRefreshTask(placeholders);
        TAB.getInstance().getCPUManager().getPlaceholderThread().submit(() -> {
            // Run in placeholder refreshing thread
            long time = System.nanoTime();
            task.run();
            TAB.getInstance().getCPUManager().addTime(featureName, TabConstants.CpuUsageCategory.PLACEHOLDER_REQUEST, System.nanoTime() - time);

            // Back to main thread
            TAB.getInstance().getCPUManager().runTask(() -> processRefreshResults(task));
        });
    }

    private void processRefreshResults(@NotNull PlaceholderRefreshTask task) {
        long time = System.nanoTime();
        Map<TabPlayer, Set<Refreshable>> update = new HashMap<>(TAB.getInstance().getOnlinePlayers().length + 1, 1);
        updateServerPlaceholders(task.getServerPlaceholderResults(), update);
        updatePlayerPlaceholders(task.getPlayerPlaceholderResults(), update);
        Map<TabPlayer, Set<Refreshable>> forceUpdate = updateRelationalPlaceholders(task.getRelationalPlaceholderResults());
        TAB.getInstance().getCPUManager().addTime(featureName, TabConstants.CpuUsageCategory.PLACEHOLDER_SAVE, System.nanoTime() - time);

        refreshFeatures(forceUpdate, update);
    }
    
    private void refreshFeatures(@NotNull Map<TabPlayer, Set<Refreshable>> forceUpdate, @NotNull Map<TabPlayer, Set<Refreshable>> update) {
        for (Entry<TabPlayer, Set<Refreshable>> entry : update.entrySet()) {
            for (Refreshable r : entry.getValue()) {
                long startTime = System.nanoTime();
                r.refresh(entry.getKey(), false);
                TAB.getInstance().getCPUManager().addTime(r.getFeatureName(), r.getRefreshDisplayName(), System.nanoTime() - startTime);
            }
        }
        for (Entry<TabPlayer, Set<Refreshable>> entry : forceUpdate.entrySet()) {
            for (Refreshable r : entry.getValue()) {
                long startTime = System.nanoTime();
                r.refresh(entry.getKey(), true);
                TAB.getInstance().getCPUManager().addTime(r.getFeatureName(), r.getRefreshDisplayName(), System.nanoTime() - startTime);
            }
        }
    }

    @NotNull
    private Map<TabPlayer, Set<Refreshable>> updateRelationalPlaceholders(
            @NotNull Map<RelationalPlaceholderImpl, Map<TabPlayer, Map<TabPlayer, Object>>> results) {
        Map<TabPlayer, Set<Refreshable>> update = new HashMap<>(TAB.getInstance().getOnlinePlayers().length + 1, 1);
        for (Entry<RelationalPlaceholderImpl, Map<TabPlayer, Map<TabPlayer, Object>>> entry : results.entrySet()) {
            RelationalPlaceholderImpl placeholder = entry.getKey();
            for (Entry<TabPlayer, Map<TabPlayer, Object>> viewerResult : entry.getValue().entrySet()) {
                for (Entry<TabPlayer, Object> targetResult : viewerResult.getValue().entrySet()) {
                    if (placeholder.hasValueChanged(viewerResult.getKey(), targetResult.getKey(), targetResult.getValue())) {
                        placeholder.updateParents(targetResult.getKey());
                        update.computeIfAbsent(targetResult.getKey(), x -> new HashSet<>()).addAll(getPlaceholderUsage(placeholder.getIdentifier()));
                    }
                }
            }
        }
        return update;
    }

    private void updatePlayerPlaceholders(@NotNull Map<PlayerPlaceholderImpl, Map<TabPlayer, Object>> results,
                                          @NotNull Map<TabPlayer, Set<Refreshable>> update) {
        for (Entry<PlayerPlaceholderImpl, Map<TabPlayer, Object>> entry : results.entrySet()) {
            PlayerPlaceholderImpl placeholder = entry.getKey();
            for (Entry<TabPlayer, Object> playerResult : entry.getValue().entrySet()) {
                if (placeholder.hasValueChanged(playerResult.getKey(), playerResult.getValue())) {
                    placeholder.updateParents(playerResult.getKey());
                    update.computeIfAbsent(playerResult.getKey(), k -> new HashSet<>()).addAll(getPlaceholderUsage(placeholder.getIdentifier()));
                    if (placeholder.getIdentifier().equals(TabConstants.Placeholder.VANISHED)) {
                        TAB.getInstance().getFeatureManager().onVanishStatusChange(playerResult.getKey());
                    }
                    if (placeholder.getIdentifier().equals(TabConstants.Placeholder.GAMEMODE)) {
                        TAB.getInstance().getFeatureManager().onGameModeChange(playerResult.getKey());
                    }
                }
            }
        }
    }

    private void updateServerPlaceholders(@NotNull Map<ServerPlaceholderImpl, Object> results,
                                          @NotNull Map<TabPlayer, Set<Refreshable>> update) {
        for (Entry<ServerPlaceholderImpl, Object> entry : results.entrySet()) {
            ServerPlaceholderImpl placeholder = entry.getKey();
            if (placeholder.hasValueChanged(entry.getValue())) {
                for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                    placeholder.updateParents(all);
                    update.computeIfAbsent(all, k -> new HashSet<>()).addAll(getPlaceholderUsage(placeholder.getIdentifier()));
                }
            }
        }
    }

    /**
     * Returns refresh interval the placeholder has configured. If not configured,
     * default refresh interval is returned.
     *
     * @param   identifier
     *          Placeholder identifier
     * @return  Configured refresh interval for placeholder
     */
    public int getRefreshInterval(@NotNull String identifier) {
        return refreshIntervals.getOrDefault(identifier, defaultRefresh);
    }

    /**
     * Returns collection of all currently registered placeholders.
     *
     * @return  collection of all currently registered placeholders
     */
    @NotNull
    public Collection<Placeholder> getAllPlaceholders() {
        return new ArrayList<>(registeredPlaceholders.values());
    }

    /**
     * Registers placeholder into the system.
     *
     * @param   placeholder
     *          Placeholder to register
     * @return  Registered placeholder (input)
     * @param   <T>
     *          Specific placeholder class
     */
    public <T extends Placeholder> T registerPlaceholder(@NotNull T placeholder) {
        boolean override = registeredPlaceholders.containsKey(placeholder.getIdentifier());
        registeredPlaceholders.put(placeholder.getIdentifier(), placeholder);
        recalculateUsedPlaceholders();
        if (override && placeholderUsage.containsKey(placeholder.getIdentifier())) {
            for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
                if (!p.isLoaded()) continue;
                placeholderUsage.get(placeholder.getIdentifier()).forEach(f -> f.refresh(p, true));
            }
        }
        return placeholder;
    }

    @Override
    public void load() {
        TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(TabConstants.Placeholder.MINIMUM_REFRESH_INTERVAL,
                featureName, TabConstants.CpuUsageCategory.PLACEHOLDER_REFRESH_INIT, this::refresh);
        for (Placeholder pl : usedPlaceholders) {
            if (pl instanceof ServerPlaceholderImpl) {
                ((ServerPlaceholderImpl)pl).update();
            }
        }
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            onJoin(p);
        }
    }

    /**
     * Detects placeholders in text using %% pattern and returns list of all detected identifiers
     *
     * @param   text
     *          text to detect placeholders in
     * @return  list of detected identifiers
     */
    public @NotNull List<String> detectPlaceholders(@NonNull String text) {
        if (!text.contains("%")) return Collections.emptyList();
        if (text.charAt(0) == '%' && text.charAt(text.length()-1) == '%') {
            int count = 0;
            char[] array = text.toCharArray();
            for (char c : array) {
                if (c == '%') {
                    count++;
                }
            }
            if (count == 2) return Collections.singletonList(text);
        }
        List<String> placeholders = new ArrayList<>();
        Matcher m = placeholderPattern.matcher(text);
        while (m.find()) {
            placeholders.add(m.group());
        }
        return placeholders;
    }

    /**
     * Marks placeholder as used by specified feature.
     *
     * @param   identifier
     *          Placeholder to mark as used
     * @param   feature
     *          Feature using the placeholder
     */
    public void addUsedPlaceholder(@NonNull String identifier, @NonNull Refreshable feature) {
        if (placeholderUsage.computeIfAbsent(identifier, x -> new HashSet<>()).add(feature)) {
            recalculateUsedPlaceholders();
            TabPlaceholder p = getPlaceholder(identifier);
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                tabExpansion.setPlaceholderValue(all, p.getIdentifier(), p.getLastValue(all));
            }
        }
    }

    /**
     * Updates array of used placeholders.
     */
    private void recalculateUsedPlaceholders() {
        usedPlaceholders = placeholderUsage.keySet().stream().map(this::getPlaceholder).distinct().toArray(Placeholder[]::new);
    }

    /**
     * Finds replacement for specified placeholder and output.
     *
     * @param   placeholder
     *          Placeholder to find replacement for
     *
     * @param   output
     *          Output the placeholder has returned
     * @return  New output based on configuration, may be identical to {@code output}
     */
    @NotNull
    public String findReplacement(@NonNull String placeholder, @NonNull String output) {
        return getPlaceholder(placeholder).getReplacements().findReplacement(output);
    }

    /**
     * Returns set of features using specified placeholder.
     *
     * @param   identifier
     *          Placeholder to get usage of
     * @return  Set of features using the placeholder
     */
    @NotNull
    public Set<Refreshable> getPlaceholderUsage(@NotNull String identifier) {
        Set<Refreshable> usage = placeholderUsage.getOrDefault(identifier, new HashSet<>());
        for (String parent : getPlaceholder(identifier).getParents()) {
            usage.addAll(getPlaceholderUsage(parent));
        }
        return usage;
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        for (Placeholder p : usedPlaceholders) {
            if (p instanceof ServerPlaceholderImpl) { // server placeholders don't update on join
                tabExpansion.setPlaceholderValue(connectedPlayer, p.getIdentifier(), ((ServerPlaceholderImpl) p).getLastValue());
            }
        }
        // Initialize to avoid onVanishStatusChange being called in the loop after joining because previous value was null
        ((PlayerPlaceholderImpl)registeredPlaceholders.get(TabConstants.Placeholder.VANISHED)).update(connectedPlayer);
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        // Condition or placeholder only used in tab expansion, do nothing for now
    }

    // ------------------
    // API Implementation
    // ------------------

    @Override
    public @NotNull ServerPlaceholderImpl registerServerPlaceholder(@NonNull String identifier, int refresh, @NonNull Supplier<Object> supplier) {
        return registerPlaceholder(new ServerPlaceholderImpl(identifier, refresh, supplier));
    }

    @Override
    public @NotNull PlayerPlaceholderImpl registerPlayerPlaceholder(@NonNull String identifier, int refresh, @NonNull Function<me.neznamy.tab.api.TabPlayer, Object> function) {
        return registerPlaceholder(new PlayerPlaceholderImpl(identifier, refresh, function));
    }

    @Override
    public @NotNull RelationalPlaceholderImpl registerRelationalPlaceholder(
            @NonNull String identifier, int refresh, @NonNull BiFunction<me.neznamy.tab.api.TabPlayer, me.neznamy.tab.api.TabPlayer, Object> function) {
        return registerPlaceholder(new RelationalPlaceholderImpl(identifier, refresh, function));
    }

    @Override
    public @NotNull TabPlaceholder getPlaceholder(@NonNull String identifier) {
        TabPlaceholder p = (TabPlaceholder) registeredPlaceholders.get(identifier);
        if (p == null) {
            TabPlaceholderRegisterEvent event = new TabPlaceholderRegisterEvent(identifier);
            if (TAB.getInstance().getEventBus() != null) TAB.getInstance().getEventBus().fire(event);
            if (event.getServerPlaceholder() != null) {
                registerServerPlaceholder(identifier, getRefreshInterval(identifier), event.getServerPlaceholder());
            } else if (event.getPlayerPlaceholder() != null) {
                registerPlayerPlaceholder(identifier, getRefreshInterval(identifier), event.getPlayerPlaceholder());
            } else if (event.getRelationalPlaceholder() != null) {
                registerRelationalPlaceholder(identifier, getRefreshInterval(identifier), event.getRelationalPlaceholder());
            } else {
                TAB.getInstance().getPlatform().registerUnknownPlaceholder(identifier);
            }
            addUsedPlaceholder(identifier, this); //likely used via tab expansion
            return getPlaceholder(identifier);
        }
        if (!placeholderUsage.containsKey(identifier)) {
            //tab expansion for internal placeholder
            addUsedPlaceholder(identifier, this);
        }
        return p;
    }

    @Override
    public void unregisterPlaceholder(@NonNull Placeholder placeholder) {
        unregisterPlaceholder(placeholder.getIdentifier());
    }

    @Override
    public void unregisterPlaceholder(@NonNull String identifier) {
        registeredPlaceholders.remove(identifier);
        placeholderUsage.remove(identifier);
        recalculateUsedPlaceholders();
    }

    /**
     * Returns {@code true} if placeholder is registered, {@code false} if not.
     *
     * @param   identifier
     *          Placeholder to check
     * @return  {@code true} if placeholder is registered, {@code false} if not
     */
    public boolean isPlaceholderRegistered(@NotNull String identifier) {
        return registeredPlaceholders.containsKey(identifier);
    }
}