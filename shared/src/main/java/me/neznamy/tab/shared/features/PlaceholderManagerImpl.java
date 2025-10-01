package me.neznamy.tab.shared.features;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TabConstants.CpuUsageCategory;
import me.neznamy.tab.shared.cpu.CpuManager;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.event.impl.TabPlaceholderRegisterEvent;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.JoinListener;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.placeholders.PlaceholderRefreshConfiguration;
import me.neznamy.tab.shared.placeholders.PlaceholderRefreshTask;
import me.neznamy.tab.shared.placeholders.conditions.ConditionManager;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.placeholders.types.PlayerPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.types.RelationalPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.types.ServerPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.types.TabPlaceholder;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Messy class for placeholder management
 */
@Getter
public class PlaceholderManagerImpl extends RefreshableFeature implements PlaceholderManager, JoinListener, Loadable {

    private static final Pattern placeholderPattern = Pattern.compile("%([^%]*)%");

    @NotNull
    private final PlaceholderRefreshConfiguration configuration;

    private final Map<String, Placeholder> registeredPlaceholders = new HashMap<>();

    //map of String-Set of features using placeholder
    private final Map<String, Set<RefreshableFeature>> placeholderUsage = new ConcurrentHashMap<>();
    private Placeholder[] usedPlaceholders = new Placeholder[0];

    private int loopTime;

    @NotNull private final TabExpansion tabExpansion;

    private final CpuManager cpu;

    /** Placeholders which are refreshed on backend server */
    private final Map<String, Integer> bridgePlaceholders = new ConcurrentHashMap<>();

    private final ConditionManager conditionManager = new ConditionManager();

    /**
     * Constructs new instance.
     *
     * @param   cpu
     *          CPU manager for submitting tasks
     * @param   configuration
     *          Placeholder refreshing configuration
     */
    public PlaceholderManagerImpl(@NotNull CpuManager cpu, @NotNull PlaceholderRefreshConfiguration configuration) {
        this.cpu = cpu;
        this.configuration = configuration;
        tabExpansion = TAB.getInstance().getConfiguration().getConfig().getPlaceholders().isRegisterTabExpansion() ?
                TAB.getInstance().getPlatform().createTabExpansion() : new EmptyTabExpansion();
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
        cpu.getPlaceholderThread().execute(new TimedCaughtTask(cpu, () -> {
            // Run in placeholder refreshing thread
            task.run();

            // Back to main thread
            cpu.getProcessingThread().execute(() -> processRefreshResults(task));
        }, getFeatureName(), CpuUsageCategory.PLACEHOLDER_REQUEST));
    }

    private void processRefreshResults(@NotNull PlaceholderRefreshTask task) {
        long time = System.nanoTime();
        Map<RefreshableFeature, Collection<TabPlayer>> update = new HashMap<>();
        for (RefreshableFeature f : updateServerPlaceholders(task.getServerPlaceholderResults())) {
            update.put(f, new HashSet<>(TAB.getInstance().getData().values()));
        }
        updatePlayerPlaceholders(task.getPlayerPlaceholderResults(), update);
        Map<RefreshableFeature, Collection<TabPlayer>> forceUpdate = updateRelationalPlaceholders(task.getRelationalPlaceholderResults());
        cpu.addTime(getFeatureName(), CpuUsageCategory.PLACEHOLDER_SAVE, System.nanoTime() - time);
        cpu.addPlaceholderTimes(task.getUsedTime());

        refreshFeatures(forceUpdate, update);
    }
    
    private void refreshFeatures(@NotNull Map<RefreshableFeature, Collection<TabPlayer>> forceUpdate, @NotNull Map<RefreshableFeature, Collection<TabPlayer>> update) {
        for (Entry<RefreshableFeature, Collection<TabPlayer>> entry : update.entrySet()) {
            TimedCaughtTask task = new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
                for (TabPlayer player : entry.getValue()) {
                    entry.getKey().refresh(player, false);
                }
            }, entry.getKey().getFeatureName(), entry.getKey().getRefreshDisplayName());
            if (entry.getKey() instanceof CustomThreaded) {
                ((CustomThreaded) entry.getKey()).getCustomThread().execute(task);
            } else {
                task.run();
            }
        }
        for (Entry<RefreshableFeature, Collection<TabPlayer>> entry : forceUpdate.entrySet()) {
            TimedCaughtTask task = new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
                for (TabPlayer player : entry.getValue()) {
                    entry.getKey().refresh(player, true);
                }
            }, entry.getKey().getFeatureName(), entry.getKey().getRefreshDisplayName());
            if (entry.getKey() instanceof CustomThreaded) {
                ((CustomThreaded) entry.getKey()).getCustomThread().execute(task);
            } else {
                task.run();
            }
        }
    }

    @NotNull
    private Map<RefreshableFeature, Collection<TabPlayer>> updateRelationalPlaceholders(
            @Nullable Map<RelationalPlaceholderImpl, Map<TabPlayer, Map<TabPlayer, String>>> results) {
        if (results == null) return Collections.emptyMap();
        Map<RefreshableFeature, Collection<TabPlayer>> update = new HashMap<>();
        for (Entry<RelationalPlaceholderImpl, Map<TabPlayer, Map<TabPlayer, String>>> entry : results.entrySet()) {
            RelationalPlaceholderImpl placeholder = entry.getKey();
            Collection<RefreshableFeature> placeholderUsage = getPlaceholderUsage(placeholder.getIdentifier());
            for (Entry<TabPlayer, Map<TabPlayer, String>> viewerResult : entry.getValue().entrySet()) {
                TabPlayer viewer = viewerResult.getKey();
                if (!viewer.isOnline()) continue; // Player disconnected in the meantime while refreshing in another thread
                for (Entry<TabPlayer, String> targetResult : viewerResult.getValue().entrySet()) {
                    TabPlayer target = targetResult.getKey();
                    if (!target.isOnline()) continue; // Player disconnected in the meantime while refreshing in another thread
                    if (placeholder.hasValueChanged(viewer, target, targetResult.getValue())) {
                        placeholder.updateParents(target);
                        for (RefreshableFeature f : placeholderUsage) {
                            update.computeIfAbsent(f, c -> new HashSet<>()).add(target);
                        }
                    }
                }
            }
        }
        return update;
    }

    private void updatePlayerPlaceholders(@NotNull Map<PlayerPlaceholderImpl, Map<TabPlayer, String>> results,
                                          @NotNull Map<RefreshableFeature, Collection<TabPlayer>> update) {
        if (results.isEmpty()) return;
        for (Entry<PlayerPlaceholderImpl, Map<TabPlayer, String>> entry : results.entrySet()) {
            PlayerPlaceholderImpl placeholder = entry.getKey();
            Set<RefreshableFeature> placeholderUsage = getPlaceholderUsage(placeholder.getIdentifier());
            for (Entry<TabPlayer, String> playerResult : entry.getValue().entrySet()) {
                TabPlayer player = playerResult.getKey();
                if (!player.isOnline()) continue; // Player disconnected in the meantime while refreshing in another thread
                if (placeholder.hasValueChanged(player, playerResult.getValue(), true)) {
                    placeholder.updateParents(player);
                    for (RefreshableFeature f : placeholderUsage) {
                        update.computeIfAbsent(f, c -> new HashSet<>()).add(player);
                    }
                    if (placeholder.getIdentifier().equals(TabConstants.Placeholder.VANISHED)) {
                        TAB.getInstance().getFeatureManager().onVanishStatusChange(player);
                    }
                    if (placeholder.getIdentifier().equals(TabConstants.Placeholder.GAMEMODE)) {
                        TAB.getInstance().getFeatureManager().onGameModeChange(player);
                    }
                }
            }
        }
    }

    @NotNull
    private Set<RefreshableFeature> updateServerPlaceholders(@NotNull Map<ServerPlaceholderImpl, String> results) {
        Set<RefreshableFeature> set = new HashSet<>();
        for (Entry<ServerPlaceholderImpl, String> entry : results.entrySet()) {
            ServerPlaceholderImpl placeholder = entry.getKey();
            if (placeholder.hasValueChanged(entry.getValue())) {
                set.addAll(getPlaceholderUsage(placeholder.getIdentifier()));
                for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                    placeholder.updateParents(all);
                }
            }
        }
        return set;
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
    public synchronized <T extends Placeholder> T registerPlaceholder(@NotNull T placeholder) {
        boolean override = registeredPlaceholders.containsKey(placeholder.getIdentifier());
        registeredPlaceholders.put(placeholder.getIdentifier(), placeholder);
        recalculateUsedPlaceholders();
        if (override && placeholderUsage.containsKey(placeholder.getIdentifier())) {
            for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
                if (!p.isLoaded()) continue;
                for (RefreshableFeature f : placeholderUsage.get(placeholder.getIdentifier())) {
                    TimedCaughtTask task = new TimedCaughtTask(cpu, () -> f.refresh(p, true), f.getFeatureName(), f.getRefreshDisplayName());
                    if (f instanceof CustomThreaded) {
                        ((CustomThreaded) f).getCustomThread().execute(task);
                    } else {
                        task.run();
                    }
                }
            }
        }
        return placeholder;
    }

    @Override
    public void load() {
        cpu.getProcessingThread().repeatTask(new TimedCaughtTask(cpu, this::refresh, getFeatureName(), CpuUsageCategory.PLACEHOLDER_REFRESH_INIT),
                TabConstants.Placeholder.MINIMUM_REFRESH_INTERVAL);
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
    @NotNull
    public static List<String> detectPlaceholders(@NonNull String text) {
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
    public synchronized void addUsedPlaceholder(@NonNull String identifier, @NonNull RefreshableFeature feature) {
        if (placeholderUsage.computeIfAbsent(identifier, x -> new HashSet<>()).add(feature)) {
            recalculateUsedPlaceholders();
            TabPlaceholder p = getPlaceholder(identifier);
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                tabExpansion.setPlaceholderValue(all, p.getIdentifier(), p.getLastValueSafe(all));
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
    public Set<RefreshableFeature> getPlaceholderUsage(@NotNull String identifier) {
        Set<RefreshableFeature> usage = placeholderUsage.getOrDefault(identifier, new HashSet<>());
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

    @NotNull
    @Override
    public String getRefreshDisplayName() {
        return "Other";
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        // Condition or placeholder only used in tab expansion, do nothing for now
    }

    /**
     * Returns placeholder with given identifier. If no such placeholder is registered,
     * returns {@code null}.
     *
     * @param   identifier
     *          Placeholder identifier
     * @return  Registered placeholder with given identifier, if present
     */
    @Nullable
    public Placeholder getPlaceholderRaw(@NotNull String identifier) {
        return registeredPlaceholders.get(identifier);
    }

    @NotNull
    public ServerPlaceholderImpl registerInternalServerPlaceholder(@NonNull String identifier, int defaultRefresh, @NonNull Supplier<String> supplier) {
        return registerServerPlaceholder(identifier, configuration.getRefreshInterval(identifier, defaultRefresh), supplier);
    }

    @NotNull
    public PlayerPlaceholderImpl registerInternalPlayerPlaceholder(@NonNull String identifier, int defaultRefresh,
                                                                    @NonNull Function<me.neznamy.tab.api.TabPlayer, String> function) {
        return registerPlayerPlaceholder(identifier, configuration.getRefreshInterval(identifier, defaultRefresh), function);
    }

    @NotNull
    public RelationalPlaceholderImpl registerInternalRelationalPlaceholder(@NonNull String identifier, int defaultRefresh,
                                                                           @NonNull BiFunction<me.neznamy.tab.api.TabPlayer, me.neznamy.tab.api.TabPlayer, String> function) {
        return registerRelationalPlaceholder(identifier, configuration.getRefreshInterval(identifier, defaultRefresh), function);
    }

    @NotNull
    public ServerPlaceholderImpl registerServerPlaceholder(@NonNull String identifier, @NonNull Supplier<String> supplier) {
        return registerServerPlaceholder(identifier, configuration.getRefreshInterval(identifier), supplier);
    }

    @NotNull
    public PlayerPlaceholderImpl registerPlayerPlaceholder(@NonNull String identifier,
                                                           @NonNull Function<me.neznamy.tab.api.TabPlayer, String> function) {
        return registerPlayerPlaceholder(identifier, configuration.getRefreshInterval(identifier), function);
    }

    @NotNull
    public RelationalPlaceholderImpl registerRelationalPlaceholder(@NonNull String identifier,
                                                                   @NonNull BiFunction<me.neznamy.tab.api.TabPlayer, me.neznamy.tab.api.TabPlayer, String> function) {
        return registerRelationalPlaceholder(identifier, configuration.getRefreshInterval(identifier), function);
    }

    // ------------------
    // API Implementation
    // ------------------

    @Override
    @NotNull
    public ServerPlaceholderImpl registerServerPlaceholder(@NonNull String identifier, int refresh, @NonNull Supplier<String> supplier) {
        ensureActive();
        bridgePlaceholders.remove(identifier);
        return registerPlaceholder(new ServerPlaceholderImpl(identifier, refresh, supplier));
    }

    @Override
    @NotNull
    public PlayerPlaceholderImpl registerPlayerPlaceholder(@NonNull String identifier, int refresh,
                                                           @NonNull Function<me.neznamy.tab.api.TabPlayer, String> function) {
        ensureActive();
        bridgePlaceholders.remove(identifier);
        return registerPlaceholder(new PlayerPlaceholderImpl(identifier, refresh, function));
    }

    @Override
    @NotNull
    public RelationalPlaceholderImpl registerRelationalPlaceholder(@NonNull String identifier, int refresh,
                                                                   @NonNull BiFunction<me.neznamy.tab.api.TabPlayer, me.neznamy.tab.api.TabPlayer, String> function) {
        ensureActive();
        bridgePlaceholders.remove(identifier);
        return registerPlaceholder(new RelationalPlaceholderImpl(identifier, refresh, function));
    }

    @NotNull
    public PlayerPlaceholderImpl registerBridgePlaceholder(@NonNull String identifier, int backendRefresh) {
        ensureActive();
        bridgePlaceholders.put(identifier, backendRefresh);
        return registerPlaceholder(new PlayerPlaceholderImpl(identifier, -1, player -> null));
    }

    @NotNull
    public RelationalPlaceholderImpl registerRelationalBridgePlaceholder(@NonNull String identifier, int backendRefresh) {
        ensureActive();
        bridgePlaceholders.put(identifier, backendRefresh);
        return registerPlaceholder(new RelationalPlaceholderImpl(identifier, -1, (viewer, target) -> null));
    }

    @Override
    @NotNull
    public synchronized TabPlaceholder getPlaceholder(@NonNull String identifier) {
        if (identifier.charAt(0) != '%' || identifier.charAt(identifier.length() - 1) != '%') {
            throw new IllegalArgumentException("Placeholder identifier must start and end with %");
        }
        TabPlaceholder p = (TabPlaceholder) registeredPlaceholders.get(identifier);
        if (p == null) {
            TabPlaceholderRegisterEvent event = new TabPlaceholderRegisterEvent(identifier);
            if (TAB.getInstance().getEventBus() != null) TAB.getInstance().getEventBus().fire(event);
            if (event.getServerPlaceholder() != null) {
                registerServerPlaceholder(identifier, event.getServerPlaceholder());
            } else if (event.getPlayerPlaceholder() != null) {
                registerPlayerPlaceholder(identifier, event.getPlayerPlaceholder());
            } else if (event.getRelationalPlaceholder() != null) {
                registerRelationalPlaceholder(identifier, event.getRelationalPlaceholder());
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
        ensureActive();
        unregisterPlaceholder(placeholder.getIdentifier());
    }

    @Override
    public void unregisterPlaceholder(@NonNull String identifier) {
        ensureActive();
        registeredPlaceholders.remove(identifier);
        placeholderUsage.remove(identifier);
        recalculateUsedPlaceholders();
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return "Refreshing placeholders";
    }
}