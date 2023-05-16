package me.neznamy.tab.shared.features;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
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
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.event.impl.TabPlaceholderRegisterEvent;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.RelationalPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.ServerPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.TabPlaceholder;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import org.jetbrains.annotations.NotNull;

/**
 * Messy class for placeholder management
 */
public class PlaceholderManagerImpl extends TabFeature implements PlaceholderManager, JoinListener, Loadable,
        Refreshable {

    private final Pattern placeholderPattern = Pattern.compile("%([^%]*)%");

    @Getter private final String refreshDisplayName = "Updating placeholders";
    @Getter private final String featureName = "Refreshing placeholders";
    @Getter private final Map<String, Integer> refreshIntervals = TAB.getInstance().getConfig().getConfigurationSection("placeholderapi-refresh-intervals");
    @Getter private final int defaultRefresh;

    @Getter private final Map<String, Placeholder> registeredPlaceholders = new HashMap<>();

    //map of String-Set of features using placeholder
    @Getter private final Map<String, Set<Refreshable>> placeholderUsage = new ConcurrentHashMap<>();
    private Placeholder[] usedPlaceholders = new Placeholder[0];

    @Getter private final AtomicInteger loopTime = new AtomicInteger();

    @Getter @NonNull private final TabExpansion tabExpansion = TAB.getInstance().getConfig().getBoolean("placeholders.register-tab-expansion", false) ?
            TAB.getInstance().getPlatform().getTabExpansion() : new EmptyTabExpansion();

    public PlaceholderManagerImpl() {
        TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(TabConstants.Placeholder.MINIMUM_REFRESH_INTERVAL, featureName, TabConstants.CpuUsageCategory.PLACEHOLDER_REFRESHING, this::refresh);
        TAB.getInstance().getMisconfigurationHelper().fixRefreshIntervals(refreshIntervals);
        defaultRefresh = refreshIntervals.getOrDefault("default-refresh-interval", 500);
    }

    private void refresh() {
        int loopTime = this.loopTime.addAndGet(TabConstants.Placeholder.MINIMUM_REFRESH_INTERVAL);
        int size = TAB.getInstance().getOnlinePlayers().length;
        Map<TabPlayer, Set<Refreshable>> update = new HashMap<>(size);
        Map<TabPlayer, Set<Refreshable>> forceUpdate = new HashMap<>(size);
        boolean somethingChanged = false;
        for (Placeholder placeholder : usedPlaceholders) {
            if (placeholder.getRefresh() == -1 || loopTime % placeholder.getRefresh() != 0) continue;
            if (placeholder instanceof RelationalPlaceholderImpl && updateRelationalPlaceholder((RelationalPlaceholderImpl) placeholder, forceUpdate)) somethingChanged = true;
            if (placeholder instanceof PlayerPlaceholderImpl && updatePlayerPlaceholder((PlayerPlaceholderImpl) placeholder, update)) somethingChanged = true;
            if (placeholder instanceof ServerPlaceholderImpl && updateServerPlaceholder((ServerPlaceholderImpl) placeholder, update)) somethingChanged = true;
        }
        if (somethingChanged) refresh(forceUpdate, update);
    }
    
    private void refresh(@NonNull Map<TabPlayer, Set<Refreshable>> forceUpdate, Map<TabPlayer, @NonNull Set<Refreshable>> update) {
        long startRefreshTime = System.nanoTime();
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
        //subtracting back usage by this method from placeholder refreshing usage, since it is already counted under different name in this method
        TAB.getInstance().getCPUManager().addTime(featureName, TabConstants.CpuUsageCategory.PLACEHOLDER_REFRESHING, startRefreshTime-System.nanoTime());
    }

    private boolean updateRelationalPlaceholder(@NonNull RelationalPlaceholderImpl placeholder, @NonNull Map<TabPlayer, Set<Refreshable>> forceUpdate) {
        boolean somethingChanged = false;
        long startTime = System.nanoTime();
        for (TabPlayer p1 : TAB.getInstance().getOnlinePlayers()) {
            for (TabPlayer p2 : TAB.getInstance().getOnlinePlayers()) {
                if (placeholder.update(p1, p2)) {
                    forceUpdate.computeIfAbsent(p2, x -> new HashSet<>()).addAll(placeholderUsage.get(placeholder.getIdentifier()));
                    somethingChanged = true;
                }
                if (placeholder.update(p2, p1)) {
                    forceUpdate.computeIfAbsent(p1, x -> new HashSet<>()).addAll(placeholderUsage.get(placeholder.getIdentifier()));
                    somethingChanged = true;
                }
            }
        }
        TAB.getInstance().getCPUManager().addPlaceholderTime(placeholder.getIdentifier(), System.nanoTime()-startTime);
        return somethingChanged;
    }

    private boolean updatePlayerPlaceholder(@NonNull PlayerPlaceholderImpl placeholder, @NonNull Map<TabPlayer, Set<Refreshable>> update) {
        boolean somethingChanged = false;
        long startTime = System.nanoTime();
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (placeholder.update(all)) {
                if (placeholder.getIdentifier().equals(TabConstants.Placeholder.VANISHED)) TAB.getInstance().getFeatureManager().onVanishStatusChange(all);
                if (placeholder.getIdentifier().equals(TabConstants.Placeholder.GAMEMODE)) TAB.getInstance().getFeatureManager().onGameModeChange(all);
                update.computeIfAbsent(all, k -> new HashSet<>()).addAll(placeholderUsage.get(placeholder.getIdentifier()));
                somethingChanged = true;
            }
        }
        TAB.getInstance().getCPUManager().addPlaceholderTime(placeholder.getIdentifier(), System.nanoTime()-startTime);
        return somethingChanged;
    }

    private boolean updateServerPlaceholder(@NonNull ServerPlaceholderImpl placeholder, @NonNull Map<TabPlayer, Set<Refreshable>> update) {
        boolean somethingChanged = false;
        long startTime = System.nanoTime();
        if (placeholder.update()) {
            somethingChanged = true;
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                update.computeIfAbsent(all, k -> new HashSet<>()).addAll(placeholderUsage.get(placeholder.getIdentifier()));
            }
        }
        TAB.getInstance().getCPUManager().addPlaceholderTime(placeholder.getIdentifier(), System.nanoTime()-startTime);
        return somethingChanged;
    }

    public int getRefreshInterval(@NonNull String identifier) {
        return refreshIntervals.getOrDefault(identifier, defaultRefresh);
    }

    public @NotNull Collection<Placeholder> getAllPlaceholders() {
        return new ArrayList<>(registeredPlaceholders.values());
    }

    public <T extends Placeholder> T registerPlaceholder(@NonNull T placeholder) {
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
        for (Placeholder pl : usedPlaceholders) {
            if (pl instanceof ServerPlaceholderImpl) {
                ((ServerPlaceholderImpl)pl).update();
            }
        }
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            onJoin(p);
        }
    }

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

    /**
     * Detects placeholders in text using %% pattern and returns list of all detected identifiers
     *
     * @param   text
     *          text to detect placeholders in
     * @return  list of detected identifiers
     */
    public @NotNull List<String> detectPlaceholders(@NonNull String text) {
        if (!text.contains("%")) return Collections.emptyList();
        List<String> placeholders = new ArrayList<>();
        Matcher m = placeholderPattern.matcher(text);
        while (m.find()) {
            placeholders.add(m.group());
        }
        return placeholders;
    }

    @Override
    public @NotNull TabPlaceholder getPlaceholder(@NonNull String identifier) {
        TabPlaceholder p = (TabPlaceholder) registeredPlaceholders.get(identifier);
        if (p == null) {
            TabPlaceholderRegisterEvent event = new TabPlaceholderRegisterEvent(identifier);
            if (TAB.getInstance().getEventBus() != null) TAB.getInstance().getEventBus().fire(event);
            if (event.getPlaceholder() != null) {
                registerPlaceholder(event.getPlaceholder());
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

    public void addUsedPlaceholder(@NonNull String identifier, @NonNull Refreshable feature) {
        if (placeholderUsage.computeIfAbsent(identifier, x -> new HashSet<>()).add(feature)) {
            recalculateUsedPlaceholders();
            TabPlaceholder p = getPlaceholder(identifier);
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                tabExpansion.setPlaceholderValue(all, p.getIdentifier(), p.getLastValue(all));
            }
        }
    }

    public void recalculateUsedPlaceholders() {
        usedPlaceholders = placeholderUsage.keySet().stream().map(this::getPlaceholder).distinct().toArray(Placeholder[]::new);
    }

    public @NotNull String findReplacement(@NonNull String placeholder, @NonNull String output) {
        return getPlaceholder(placeholder).getReplacements().findReplacement(output);
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        for (Placeholder p : usedPlaceholders) {
            long startTime = System.nanoTime();
            if (p instanceof RelationalPlaceholderImpl) {
                for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                    ((RelationalPlaceholderImpl)p).update(connectedPlayer, all);
                    ((RelationalPlaceholderImpl)p).update(all, connectedPlayer);
                }
            }
            if (p instanceof PlayerPlaceholderImpl) {
                ((PlayerPlaceholderImpl)p).update(connectedPlayer);
            }
            if (p instanceof ServerPlaceholderImpl) { // server placeholders don't update on join
                tabExpansion.setPlaceholderValue(connectedPlayer, p.getIdentifier(), ((ServerPlaceholderImpl) p).getLastValue());
            }
            TAB.getInstance().getCPUManager().addPlaceholderTime(p.getIdentifier(), System.nanoTime()-startTime);
        }
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        // Condition or placeholder only used in tab expansion, do nothing for now
    }
}