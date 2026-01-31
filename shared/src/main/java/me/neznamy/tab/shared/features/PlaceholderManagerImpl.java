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
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.placeholders.PlaceholderReference;
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
import me.neznamy.tab.shared.util.DumpUtils;
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
import java.util.stream.Collectors;

/**
 * Messy class for placeholder management
 */
@Getter
public class PlaceholderManagerImpl extends RefreshableFeature implements PlaceholderManager, JoinListener, Loadable, Dumpable {

    private static final Pattern placeholderPattern = Pattern.compile("%([^%]*)%");

    @NotNull
    private final PlaceholderRefreshConfiguration configuration;

    private final Map<String, PlaceholderReference> registeredPlaceholders = new HashMap<>();
    private final Map<Pattern, Function<String, Function<Matcher, Placeholder>>> registeredDynamicPlaceholders = new LinkedHashMap<>();

    private PlaceholderReference[] usedPlaceholders = new PlaceholderReference[0];

    private long loopTime;

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
        List<PlaceholderReference> placeholders = new ArrayList<>();
        for (PlaceholderReference placeholder : usedPlaceholders) {
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
            for (Entry<TabPlayer, Map<TabPlayer, String>> viewerResult : entry.getValue().entrySet()) {
                TabPlayer viewer = viewerResult.getKey();
                if (!viewer.isOnline()) continue; // Player disconnected in the meantime while refreshing in another thread
                for (Entry<TabPlayer, String> targetResult : viewerResult.getValue().entrySet()) {
                    TabPlayer target = targetResult.getKey();
                    if (!target.isOnline()) continue; // Player disconnected in the meantime while refreshing in another thread
                    if (placeholder.hasValueChanged(viewer, target, targetResult.getValue())) {
                        placeholder.updateParents(target);
                        for (RefreshableFeature f : placeholder.getUsedByFeatures()) {
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
            for (Entry<TabPlayer, String> playerResult : entry.getValue().entrySet()) {
                TabPlayer player = playerResult.getKey();
                if (!player.isOnline()) continue; // Player disconnected in the meantime while refreshing in another thread
                if (placeholder.hasValueChanged(player, playerResult.getValue(), true)) {
                    placeholder.updateParents(player);
                    for (RefreshableFeature f : placeholder.getUsedByFeatures()) {
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
                set.addAll(placeholder.getUsedByFeatures());
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
        return registeredPlaceholders.values().stream().map(PlaceholderReference::getHandle).collect(Collectors.toList());
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
    public synchronized <T extends Placeholder> PlaceholderReference registerPlaceholder(@NotNull T placeholder) {
        PlaceholderReference existing = registeredPlaceholders.get(placeholder.getIdentifier());
        if (existing != null) {
            existing.setHandle((TabPlaceholder) placeholder);
            for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
                if (!p.isLoaded()) continue;
                for (RefreshableFeature f : ((TabPlaceholder)placeholder).getUsedByFeatures()) {
                    TimedCaughtTask task = new TimedCaughtTask(cpu, () -> f.refresh(p, true), f.getFeatureName(), f.getRefreshDisplayName());
                    if (f instanceof CustomThreaded) {
                        ((CustomThreaded) f).getCustomThread().execute(task);
                    } else {
                        task.run();
                    }
                }
            }
            return existing;
        } else {
            PlaceholderReference reference = new PlaceholderReference(placeholder.getIdentifier(), (TabPlaceholder) placeholder);
            registeredPlaceholders.put(placeholder.getIdentifier(), reference);
            return reference;
        }
    }

    @Override
    public void load() {
        cpu.getProcessingThread().repeatTask(new TimedCaughtTask(cpu, this::refresh, getFeatureName(), CpuUsageCategory.PLACEHOLDER_REFRESH_INIT),
                TabConstants.Placeholder.MINIMUM_REFRESH_INTERVAL);
        for (PlaceholderReference pl : usedPlaceholders) {
            if (pl.getHandle() instanceof ServerPlaceholderImpl) {
                ((ServerPlaceholderImpl)pl.getHandle()).update();
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
        if (getPlaceholder(identifier).addUsedFeature(feature)) {
            recalculateUsedPlaceholders();
            TabPlaceholder p = getPlaceholder(identifier);
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                all.expansionData.setPlaceholderValue(p.getIdentifier(), p.getLastValueSafe(all));
            }
        }
    }

    /**
     * Marks placeholder as used by specified feature.
     *
     * @param   placeholder
     *          Placeholder to mark as used
     * @param   feature
     *          Feature using the placeholder
     */
    public synchronized void addUsedPlaceholder(@NonNull TabPlaceholder placeholder, @NonNull RefreshableFeature feature) {
        if (placeholder.addUsedFeature(feature)) {
            recalculateUsedPlaceholders();
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                all.expansionData.setPlaceholderValue(placeholder.getIdentifier(), placeholder.getLastValueSafe(all));
            }
        }
    }

    /**
     * Updates array of used placeholders.
     */
    private void recalculateUsedPlaceholders() {
        usedPlaceholders = registeredPlaceholders.values().stream().filter(p -> !p.getHandle().getUsedByFeatures().isEmpty()).toArray(PlaceholderReference[]::new);
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
        return getPlaceholderReference(placeholder).getHandle().getReplacements().findReplacement(output);
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        for (PlaceholderReference p : usedPlaceholders) {
            if (p.getHandle() instanceof ServerPlaceholderImpl) { // server placeholders don't update on join
                connectedPlayer.expansionData.setPlaceholderValue(p.getIdentifier(), ((ServerPlaceholderImpl) p.getHandle()).getLastValue());
            }
        }
        // Initialize to avoid onVanishStatusChange being called in the loop after joining because previous value was null
        ((PlayerPlaceholderImpl)registeredPlaceholders.get(TabConstants.Placeholder.VANISHED).getHandle()).update(connectedPlayer);
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
    public PlaceholderReference getPlaceholderRaw(@NotNull String identifier) {
        return registeredPlaceholders.get(identifier);
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
    public RelationalPlaceholderImpl registerRelationalPlaceholder(
            @NonNull String identifier,
            @NonNull BiFunction<me.neznamy.tab.api.TabPlayer, me.neznamy.tab.api.TabPlayer, String> function
    ) {
        return registerRelationalPlaceholder(identifier, configuration.getRefreshInterval(identifier), function);
    }

    /**
     * Parses all placeholders in the given text for the given player.
     *
     * @param   text
     *          Text to parse
     * @param   player
     *          Player to parse for
     * @return  Text with all placeholders replaced
     */
    @NotNull
    public String parsePlaceholders(@NotNull String text, @NotNull TabPlayer player) {
        String output = text;
        for (String placeholder : detectPlaceholders(text)) {
            TabPlaceholder p = getPlaceholder(placeholder);
            String value = p.getLastValueSafe(player);
            output = output.replace(placeholder, value);
        }
        return output;
    }

    // ------------------
    // API Implementation
    // ------------------

    @Override
    @NotNull
    public ServerPlaceholderImpl registerServerPlaceholder(@NonNull String identifier, int refresh, @NonNull Supplier<String> supplier) {
        ensureActive();
        bridgePlaceholders.remove(identifier);
        return (ServerPlaceholderImpl) registerPlaceholder(new ServerPlaceholderImpl(identifier, refresh, supplier)).getHandle();
    }

    @Override
    @NotNull
    public PlayerPlaceholderImpl registerPlayerPlaceholder(@NonNull String identifier, int refresh,
                                                           @NonNull Function<me.neznamy.tab.api.TabPlayer, String> function) {
        ensureActive();
        bridgePlaceholders.remove(identifier);
        return (PlayerPlaceholderImpl) registerPlaceholder(new PlayerPlaceholderImpl(identifier, refresh, function)).getHandle();
    }

    @Override
    @NotNull
    public RelationalPlaceholderImpl registerRelationalPlaceholder(@NonNull String identifier, int refresh,
                                                                   @NonNull BiFunction<me.neznamy.tab.api.TabPlayer, me.neznamy.tab.api.TabPlayer, String> function) {
        ensureActive();
        bridgePlaceholders.remove(identifier);
        return (RelationalPlaceholderImpl) registerPlaceholder(new RelationalPlaceholderImpl(identifier, refresh, function)).getHandle();
    }

    @Override
    public void registerServerPlaceholder(@NonNull Pattern identifier, int refresh, @NonNull Function<Matcher, Supplier<String>> function) {
        ensureActive();
        registeredDynamicPlaceholders.put(identifier, id -> groups -> new ServerPlaceholderImpl(id, refresh, function.apply(groups)));
    }

    @Override
    public void registerPlayerPlaceholder(@NonNull Pattern identifier, int refresh,
                                          @NonNull Function<Matcher, Function<me.neznamy.tab.api.TabPlayer, String>> function) {
        ensureActive();
        registeredDynamicPlaceholders.put(identifier, id -> groups -> new PlayerPlaceholderImpl(id, refresh, function.apply(groups)));
    }

    @Override
    public void registerRelationalPlaceholder(@NonNull Pattern identifier, int refresh,
                                              @NonNull Function<Matcher, BiFunction<me.neznamy.tab.api.TabPlayer, me.neznamy.tab.api.TabPlayer, String>> function) {
        ensureActive();
        registeredDynamicPlaceholders.put(identifier, id -> groups -> new RelationalPlaceholderImpl(id, refresh, function.apply(groups)));
    }

    @NotNull
    public PlayerPlaceholderImpl registerBridgePlaceholder(@NonNull String identifier, int backendRefresh) {
        ensureActive();
        bridgePlaceholders.put(identifier, backendRefresh);
        return (PlayerPlaceholderImpl) registerPlaceholder(new PlayerPlaceholderImpl(identifier, -1, player -> null)).getHandle();
    }

    @NotNull
    public RelationalPlaceholderImpl registerRelationalBridgePlaceholder(@NonNull String identifier, int backendRefresh) {
        ensureActive();
        bridgePlaceholders.put(identifier, backendRefresh);
        return (RelationalPlaceholderImpl) registerPlaceholder(new RelationalPlaceholderImpl(identifier, -1, (viewer, target) -> null)).getHandle();
    }

    @NotNull
    public synchronized PlaceholderReference getPlaceholderReference(@NonNull String identifier) {
        if (identifier.charAt(0) != '%' || identifier.charAt(identifier.length() - 1) != '%') {
            throw new IllegalArgumentException("Placeholder identifier must start and end with % (attempted to use \"" + identifier + "\")");
        }
        // Check if placeholder is already registered
        PlaceholderReference reference = registeredPlaceholders.get(identifier);
        if (reference != null) {
            addUsedPlaceholder(reference.getHandle(), this); // Make sure it refreshes if used via tab expansion or such
            return reference;
        }

        // Placeholder does not exist, create it
        for (Entry<Pattern, Function<String, Function<Matcher, Placeholder>>> entry : registeredDynamicPlaceholders.entrySet()) {
            Matcher m = entry.getKey().matcher(identifier);
            if (m.matches()) {
                return registerPlaceholder(entry.getValue().apply(identifier).apply(m));
            }
        }
        TAB.getInstance().getPlatform().registerUnknownPlaceholder(identifier);
        addUsedPlaceholder(identifier); // Make sure it refreshes if used via tab expansion or such
        return getPlaceholderReference(identifier);
    }

    @Override
    @NotNull
    public synchronized TabPlaceholder getPlaceholder(@NonNull String identifier) {
        return getPlaceholderReference(identifier).getHandle();
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
        recalculateUsedPlaceholders();
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return "Refreshing placeholders";
    }

    @Override
    @NotNull
    public Object dump(@NotNull TabPlayer player) {
        Map<String, Object> map = new LinkedHashMap<>();

        // Placeholder-related config sections
        map.put("configuration", new LinkedHashMap<String, Object>() {{
            put("placeholders", TAB.getInstance().getConfiguration().getConfig().getPlaceholders().getSection().getMap());
            put("placeholder-output-replacements", TAB.getInstance().getConfiguration().getConfig().getReplacements().getSection().getMap());
            put("placeholder-refresh-intervals", TAB.getInstance().getConfiguration().getConfig().getRefresh().getSection().getMap());
            put("conditions", TAB.getInstance().getConfiguration().getConfig().getConditions().getSection().getMap());
            put("animations", TAB.getInstance().getConfiguration().getAnimations().getAnimations().getSection().getMap());
        }});

        // Placeholder values
        List<List<String>> serverPlaceholders = new ArrayList<>();
        List<List<String>> playerPlaceholders = new ArrayList<>();
        Map<TabPlayer, List<List<String>>> relationalPlaceholders = new HashMap<>();
        for (PlaceholderReference reference : usedPlaceholders) {
            TabPlaceholder p = reference.getHandle();
            if (p.getIdentifier().contains("AnonymousCondition")) continue; // These are ugly, don't show them
            if (p instanceof ServerPlaceholderImpl) {
                serverPlaceholders.add(Arrays.asList(p.getIdentifier(), String.valueOf(reference.getRefresh()), ((ServerPlaceholderImpl) p).getLastValue()));
            } else if (p instanceof PlayerPlaceholderImpl) {
                playerPlaceholders.add(Arrays.asList(p.getIdentifier(), String.valueOf(reference.getRefresh()), p.getLastValueSafe(player)));
            } else if (p instanceof RelationalPlaceholderImpl) {
                for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                    relationalPlaceholders.computeIfAbsent(viewer, k -> new ArrayList<>())
                            .add(Arrays.asList(p.getIdentifier(), String.valueOf(reference.getRefresh()), ((RelationalPlaceholderImpl) p).getLastValue(viewer, player)));
                }
            }
        }
        Map<String, Object> placeholderValues = new LinkedHashMap<>();
        placeholderValues.put("server", DumpUtils.tableToLines(Arrays.asList("Identifier", "Refresh", "Current value"), serverPlaceholders));
        placeholderValues.put("player", DumpUtils.tableToLines(Arrays.asList("Identifier", "Refresh", "Current value"), playerPlaceholders));
        Map<String, List<String>> relationalFormatted = new LinkedHashMap<>();
        for (Entry<TabPlayer, List<List<String>>> entry : relationalPlaceholders.entrySet()) {
            relationalFormatted.put("for viewer " + entry.getKey().getName(),
                    DumpUtils.tableToLines(Arrays.asList("Identifier", "Refresh", "Current value"), entry.getValue()));
        }
        placeholderValues.put("relational", relationalFormatted);
        map.put("current-placeholder-values", placeholderValues);

        // Return
        return map;
    }
}