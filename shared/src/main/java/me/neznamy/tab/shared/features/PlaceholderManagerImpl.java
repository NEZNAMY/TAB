package me.neznamy.tab.shared.features;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.api.placeholder.PlayerPlaceholder;
import me.neznamy.tab.api.placeholder.RelationalPlaceholder;
import me.neznamy.tab.api.placeholder.ServerPlaceholder;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.event.impl.TabPlaceholderRegisterEvent;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.RelationalPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.ServerPlaceholderImpl;
import me.neznamy.tab.shared.placeholders.TabPlaceholder;

/**
 * Messy class for placeholder management
 */
public class PlaceholderManagerImpl extends TabFeature implements PlaceholderManager {

    private final Pattern placeholderPattern = Pattern.compile("%([^%]*)%");

    private final int defaultRefresh = TAB.getInstance().getConfiguration().getConfig().getInt("placeholderapi-refresh-intervals.default-refresh-interval", 100);
    private final Map<String, Integer> serverPlaceholderRefreshIntervals = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("placeholderapi-refresh-intervals.server");
    private final Map<String, Integer> playerPlaceholderRefreshIntervals = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("placeholderapi-refresh-intervals.player");
    private final Map<String, Integer> relationalPlaceholderRefreshIntervals = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("placeholderapi-refresh-intervals.relational");

    //plugin internals + PAPI + API
    private final Map<String, Placeholder> registeredPlaceholders = new HashMap<>();

    //map of String-Set of features using placeholder
    private final Map<String, Set<TabFeature>> placeholderUsage = new ConcurrentHashMap<>();
    private Placeholder[] usedPlaceholders = new Placeholder[0];
    
    private final AtomicInteger atomic = new AtomicInteger();
    private int refreshInterval = 10000;
    private Future<?> refreshTask = TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(refreshInterval, this, "Refreshing placeholders", this::refresh);

    private TabExpansion tabExpansion;

    public PlaceholderManagerImpl(){
        super("Refreshing placeholders", "Updating placeholders");
    }
    
    private void refresh() {
        int loopTime = atomic.addAndGet(refreshInterval);
        int size = TAB.getInstance().getOnlinePlayers().length;
        Map<TabPlayer, Set<TabFeature>> update = new HashMap<>(size);
        Map<TabPlayer, Set<TabFeature>> forceUpdate = new HashMap<>(size);
        boolean somethingChanged = false;
        for (Placeholder placeholder : usedPlaceholders) {
            if (placeholder.getRefresh() == -1 || loopTime % placeholder.getRefresh() != 0) continue;
            if (placeholder instanceof RelationalPlaceholderImpl && updateRelationalPlaceholder((RelationalPlaceholderImpl) placeholder, forceUpdate)) somethingChanged = true;
            if (placeholder instanceof PlayerPlaceholderImpl && updatePlayerPlaceholder((PlayerPlaceholderImpl) placeholder, update)) somethingChanged = true;
            if (placeholder instanceof ServerPlaceholderImpl && updateServerPlaceholder((ServerPlaceholderImpl) placeholder, update)) somethingChanged = true;
        }
        if (somethingChanged) refresh(forceUpdate, update);
    }
    
    private void refresh(Map<TabPlayer, Set<TabFeature>> forceUpdate, Map<TabPlayer, Set<TabFeature>> update) {
        long startRefreshTime = System.nanoTime();
        for (Entry<TabPlayer, Set<TabFeature>> entry : update.entrySet()) {
            for (TabFeature r : entry.getValue()) {
                long startTime = System.nanoTime();
                r.refresh(entry.getKey(), false);
                TAB.getInstance().getCPUManager().addTime(r.getFeatureName(), r.getRefreshDisplayName(), System.nanoTime() - startTime);
            }
        }
        for (Entry<TabPlayer, Set<TabFeature>> entry : forceUpdate.entrySet()) {
            for (TabFeature r : entry.getValue()) {
                long startTime = System.nanoTime();
                r.refresh(entry.getKey(), true);
                TAB.getInstance().getCPUManager().addTime(r.getFeatureName(), r.getRefreshDisplayName(), System.nanoTime() - startTime);
            }
        }
        //subtracting back usage by this method from placeholder refreshing usage, since it is already counted under different name in this method
        TAB.getInstance().getCPUManager().addTime(getFeatureName(), TabConstants.CpuUsageCategory.PLACEHOLDER_REFRESHING, startRefreshTime-System.nanoTime());
    }

    private boolean updateRelationalPlaceholder(RelationalPlaceholderImpl placeholder, Map<TabPlayer, Set<TabFeature>> forceUpdate) {
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

    private boolean updatePlayerPlaceholder(PlayerPlaceholderImpl placeholder, Map<TabPlayer, Set<TabFeature>> update) {
        boolean somethingChanged = false;
        long startTime = System.nanoTime();
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (placeholder.update(all)) {
                if (placeholder.getIdentifier().equals(TabConstants.Placeholder.VANISHED)) TAB.getInstance().getFeatureManager().onVanishStatusChange(all);
                update.computeIfAbsent(all, k -> new HashSet<>()).addAll(placeholderUsage.get(placeholder.getIdentifier()));
                somethingChanged = true;
            }
        }
        TAB.getInstance().getCPUManager().addPlaceholderTime(placeholder.getIdentifier(), System.nanoTime()-startTime);
        return somethingChanged;
    }

    private boolean updateServerPlaceholder(ServerPlaceholderImpl placeholder, Map<TabPlayer, Set<TabFeature>> update) {
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

    public int getRelationalRefresh(String identifier) {
        return relationalPlaceholderRefreshIntervals.getOrDefault(identifier, defaultRefresh);
    }

    public Collection<Placeholder> getAllPlaceholders(){
        return new ArrayList<>(registeredPlaceholders.values());
    }

    public Placeholder registerPlaceholder(Placeholder placeholder) {
        if (placeholder == null) throw new IllegalArgumentException("Placeholder cannot be null");
        boolean override = registeredPlaceholders.containsKey(placeholder.getIdentifier());
        registeredPlaceholders.put(placeholder.getIdentifier(), placeholder);
        recalculateUsedPlaceholders();
        if (override && placeholderUsage.containsKey(placeholder.getIdentifier())) {
            ((TabPlaceholder) placeholder).markAsUsed();
            for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
                if (!p.isLoaded()) continue;
                placeholderUsage.get(placeholder.getIdentifier()).forEach(f -> f.refresh(p, true));
            }
        }
        return placeholder;
    }

    public Map<String, Integer> getServerPlaceholderRefreshIntervals() {
        return serverPlaceholderRefreshIntervals;
    }

    public Map<String, Integer> getPlayerPlaceholderRefreshIntervals() {
        return playerPlaceholderRefreshIntervals;
    }

    public int getDefaultRefresh() {
        return defaultRefresh;
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
    public void unload() {
        registeredPlaceholders.values().forEach(Placeholder::unload);
    }

    @Override
    public ServerPlaceholder registerServerPlaceholder(String identifier, int refresh, Supplier<Object> supplier) {
        return (ServerPlaceholder) registerPlaceholder(new ServerPlaceholderImpl(identifier, refresh, supplier));
    }
    
    @Override
    public PlayerPlaceholder registerPlayerPlaceholder(String identifier, int refresh, Function<TabPlayer, Object> function) {
        return (PlayerPlaceholder) registerPlaceholder(new PlayerPlaceholderImpl(identifier, refresh, function));
    }

    @Override
    public RelationalPlaceholder registerRelationalPlaceholder(String identifier, int refresh, BiFunction<TabPlayer, TabPlayer, Object> function) {
        return (RelationalPlaceholder) registerPlaceholder(new RelationalPlaceholderImpl(identifier, refresh, function));
    }

    @Override
    public List<String> detectPlaceholders(String text){
        if (text == null || !text.contains("%")) return Collections.emptyList();
        List<String> placeholders = new ArrayList<>();
        Matcher m = placeholderPattern.matcher(text);
        while (m.find()) {
            placeholders.add(m.group());
        }
        return placeholders;
    }

    @Override
    public TabPlaceholder getPlaceholder(String identifier) {
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

    @Override
    public void addUsedPlaceholder(String identifier, TabFeature feature) {
        if (placeholderUsage.computeIfAbsent(identifier, x -> new HashSet<>()).add(feature)) {
            recalculateUsedPlaceholders();
            TabPlaceholder p = getPlaceholder(identifier);
            p.markAsUsed();
            if (tabExpansion != null) {
                for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                    tabExpansion.setPlaceholderValue(all, p.getIdentifier(), p.getLastValue(all));
                }
            }
            if (p.getRefresh() % 50 == 0 && p.getRefresh() > 0) {
                int refresh = gcd(p.getRefresh(), refreshInterval);
                if (refreshInterval != refresh) {
                    TAB.getInstance().debug("Decreasing refresh interval of placeholder refreshing task to " + refresh + "ms due to placeholder " + identifier);
                    refreshTask.cancel(true);
                    refreshInterval = refresh;
                    refreshTask = TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(refreshInterval, this, "Refreshing placeholders", this::refresh);
                    atomic.set(0);
                }
            }
        }
    }

    private int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }
    
    public void recalculateUsedPlaceholders() {
        usedPlaceholders = placeholderUsage.keySet().stream().map(this::getPlaceholder).distinct().toArray(Placeholder[]::new);
    }

    @Override
    public String findReplacement(String placeholder, String output) {
        return getPlaceholder(placeholder).getReplacements().findReplacement(output);
    }

    @Override
    public List<String> getUsedPlaceholders() {
        return Arrays.stream(usedPlaceholders).map(Placeholder::getIdentifier).collect(Collectors.toList());
    }

    public Map<String, Set<TabFeature>> getPlaceholderUsage(){
        return placeholderUsage;
    }
    
    public AtomicInteger getLoopTime() {
        return atomic;
    }

    public TabExpansion getTabExpansion() {
        return tabExpansion;
    }

    public void setTabExpansion(TabExpansion tabExpansion) {
        this.tabExpansion = tabExpansion;
    }

    @Override
    public void onJoin(TabPlayer connectedPlayer) {
        if (tabExpansion == null) return;
        for (Placeholder p : usedPlaceholders) {
            if (p instanceof ServerPlaceholder) { // server placeholders don't update on join
                tabExpansion.setPlaceholderValue(connectedPlayer, p.getIdentifier(), ((ServerPlaceholder) p).getLastValue());
            }
        }
    }
}