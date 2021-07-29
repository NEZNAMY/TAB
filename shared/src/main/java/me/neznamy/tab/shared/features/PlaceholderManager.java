package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.neznamy.tab.api.TABAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.types.Feature;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import me.neznamy.tab.shared.features.types.event.QuitEventListener;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.PlaceholderRegistry;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
import me.neznamy.tab.shared.placeholders.RelationalPlaceholder;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;

/**
 * Messy class for placeholder management
 */
public class PlaceholderManager implements JoinEventListener, QuitEventListener, Loadable {

	private final Pattern placeholderPattern = Pattern.compile("%([^%]*)%");
	
	private TAB tab;
	private int defaultRefresh;
	private Map<String, Integer> serverPlaceholderRefreshIntervals = new HashMap<>();
	private Map<String, Integer> playerPlaceholderRefreshIntervals = new HashMap<>();
	private Map<String, Integer> relationalPlaceholderRefreshIntervals = new HashMap<>();
	
	//all placeholders used in all configuration files + API, including invalid ones
	private Set<String> allUsedPlaceholderIdentifiers = new HashSet<>();
	
	//plugin internals + PAPI + API
	private Map<String, Placeholder> registeredPlaceholders = new HashMap<>();

	private List<PlaceholderRegistry> registry = new ArrayList<>();
	
	//map of String-Set of features using placeholder
	private Map<String, Set<Refreshable>> placeholderUsage;

	public PlaceholderManager(TAB tab){
		this.tab = tab;
		findAllUsed(tab.getConfiguration().getConfig().getValues());
		findAllUsed(tab.getConfiguration().getAnimationFile().getValues());
		findAllUsed(tab.getConfiguration().getBossbarConfig().getValues());
		if (tab.getConfiguration().getPremiumConfig() != null) findAllUsed(tab.getConfiguration().getPremiumConfig().getValues());
		loadRefreshIntervals();
		AtomicInteger atomic = new AtomicInteger();
		tab.getCPUManager().startRepeatingMeasuredTask(50, "refreshing placeholders", getFeatureType(), UsageType.REPEATING_TASK, () -> {

			int loopTime = atomic.addAndGet(50);
			if (placeholderUsage == null) return; //plugin still starting up
			Map<TabPlayer, Set<Refreshable>> update = null; //not initializing if not needed
			Map<TabPlayer, Set<Refreshable>> forceUpdate = null;
			boolean somethingChanged = false;
			for (String identifier : getAllUsedPlaceholderIdentifiers()) {
				Placeholder placeholder = getPlaceholder(identifier);
				if (placeholder == null || loopTime % placeholder.getRefresh() != 0) continue;
				if (placeholder instanceof RelationalPlaceholder) {
					if (forceUpdate == null) forceUpdate = new HashMap<>();
					if (updateRelationalPlaceholder((RelationalPlaceholder) placeholder, forceUpdate)) somethingChanged = true;
				}
				if (update == null) {
					update = new HashMap<>();
				}
				if (placeholder instanceof PlayerPlaceholder && updatePlayerPlaceholder((PlayerPlaceholder) placeholder, update)) somethingChanged = true;
				if (placeholder instanceof ServerPlaceholder && updateServerPlaceholder((ServerPlaceholder) placeholder, update)) somethingChanged = true;
			}
			if (somethingChanged) refresh(forceUpdate, update);
		});
	}
	
	private boolean updateRelationalPlaceholder(RelationalPlaceholder placeholder, Map<TabPlayer, Set<Refreshable>> forceUpdate) {
		boolean somethingChanged = false;
		long startTime = System.nanoTime();
		for (TabPlayer p1 : tab.getPlayers()) {
			if (!p1.isLoaded()) continue;
			for (TabPlayer p2 : tab.getPlayers()) {
				if (!p2.isLoaded()) continue;
				if (placeholder.update(p1, p2)) {
					if (!forceUpdate.containsKey(p2)) forceUpdate.put(p2, new HashSet<>());
					forceUpdate.get(p2).addAll(placeholderUsage.get(placeholder.getIdentifier()));
					somethingChanged = true;
				}
				if (placeholder.update(p2, p1)) {
					if (!forceUpdate.containsKey(p1)) forceUpdate.put(p1, new HashSet<>());
					forceUpdate.get(p1).addAll(placeholderUsage.get(placeholder.getIdentifier()));
					somethingChanged = true;
				}
			}
		}
		tab.getCPUManager().addPlaceholderTime(placeholder.getIdentifier(), System.nanoTime()-startTime);
		return somethingChanged;
	}
	
	private boolean updatePlayerPlaceholder(PlayerPlaceholder placeholder, Map<TabPlayer, Set<Refreshable>> update) {
		boolean somethingChanged = false;
		long startTime = System.nanoTime();
		for (TabPlayer all : tab.getPlayers()) {
			if (!all.isLoaded()) continue;
			if (placeholder.update(all)) {
				update.computeIfAbsent(all, k -> new HashSet<>());
				update.get(all).addAll(placeholderUsage.get(placeholder.getIdentifier()));
				somethingChanged = true;
			}
		}
		tab.getCPUManager().addPlaceholderTime(placeholder.getIdentifier(), System.nanoTime()-startTime);
		return somethingChanged;
	}
	
	private boolean updateServerPlaceholder(ServerPlaceholder placeholder, Map<TabPlayer, Set<Refreshable>> update) {
		boolean somethingChanged = false;
		long startTime = System.nanoTime();
		if (placeholder.update()) {
			somethingChanged = true;
			for (TabPlayer all : tab.getPlayers()) {
				if (!all.isLoaded()) continue;
				update.computeIfAbsent(all, k -> new HashSet<>());
				update.get(all).addAll(placeholderUsage.get(placeholder.getIdentifier()));
			}
		}
		tab.getCPUManager().addPlaceholderTime(placeholder.getIdentifier(), System.nanoTime()-startTime);
		return somethingChanged;
	}
	
	private void loadRefreshIntervals() {
		for (Object category : tab.getConfiguration().getConfig().getConfigurationSection("placeholderapi-refresh-intervals").keySet()) {
			if (!Arrays.asList("default-refresh-interval", "server", "player", "relational").contains(category.toString())) {
				tab.getErrorManager().startupWarn("Unknown placeholder category \"" + category + "\". Valid categories are \"server\", \"player\" and \"relational\"");
			}
		}
		defaultRefresh = tab.getConfiguration().getConfig().getInt("placeholderapi-refresh-intervals.default-refresh-interval", 100);
		for (Entry<Object, Object> placeholder : tab.getConfiguration().getConfig().getConfigurationSection("placeholderapi-refresh-intervals.server").entrySet()) {
			getServerPlaceholderRefreshIntervals().put(placeholder.getKey().toString(), tab.getErrorManager().parseInteger(placeholder.getValue().toString(), getDefaultRefresh(), "refresh interval of server placeholder"));
		}
		for (Entry<Object, Object> placeholder : tab.getConfiguration().getConfig().getConfigurationSection("placeholderapi-refresh-intervals.player").entrySet()) {
			getPlayerPlaceholderRefreshIntervals().put(placeholder.getKey().toString(), tab.getErrorManager().parseInteger(placeholder.getValue().toString(), getDefaultRefresh(), "refresh interval of player placeholder"));
		}
		for (Entry<Object, Object> placeholder : tab.getConfiguration().getConfig().getConfigurationSection("placeholderapi-refresh-intervals.relational").entrySet()) {
			relationalPlaceholderRefreshIntervals.put(placeholder.getKey().toString(), tab.getErrorManager().parseInteger(placeholder.getValue().toString(), getDefaultRefresh(), "refresh interval of relational placeholder"));
		}
	}
	
	private void refresh(Map<TabPlayer, Set<Refreshable>> forceUpdate, Map<TabPlayer, Set<Refreshable>> update) {
		if (forceUpdate != null) 
			for (Entry<TabPlayer, Set<Refreshable>> entry : update.entrySet()) {
				if (forceUpdate.containsKey(entry.getKey())) {
					entry.getValue().removeAll(forceUpdate.get(entry.getKey()));
				}
			}
		long startRefreshTime = System.nanoTime();
		if (forceUpdate != null) 
			for (Entry<TabPlayer, Set<Refreshable>> entry : forceUpdate.entrySet()) {
				for (Refreshable r : entry.getValue()) {
					long startTime = System.nanoTime();
					r.refresh(entry.getKey(), true);
					tab.getCPUManager().addTime(r.getFeatureType(), UsageType.REFRESHING, System.nanoTime()-startTime);
				}
			}
		for (Entry<TabPlayer, Set<Refreshable>> entry : update.entrySet()) {
			for (Refreshable r : entry.getValue()) {
				long startTime = System.nanoTime();
				r.refresh(entry.getKey(), false);
				tab.getCPUManager().addTime(r.getFeatureType(), UsageType.REFRESHING, System.nanoTime()-startTime);
			}
		}
		//subtracting back usage by this method from placeholder refreshing usage, since it is already counted under different name in this method
		tab.getCPUManager().addTime(getFeatureType(), UsageType.REPEATING_TASK, startRefreshTime-System.nanoTime());
	}

	public void refreshPlaceholderUsage() {
		Map<String, Set<Refreshable>> placeholderUsage0 = new HashMap<>();
		for (String placeholder : getAllUsedPlaceholderIdentifiers()) {
			Set<Refreshable> set = new HashSet<>();
			for (Feature r : tab.getFeatureManager().getAllFeatures()) {
				if (!(r instanceof Refreshable)) continue;
				try {
					if (((Refreshable)r).getUsedPlaceholders().contains(placeholder)) set.add((Refreshable) r);
				} catch (Exception e) {
					//temporarily avoiding error until recode comes out
					set.add((Refreshable) r);
				}
			}
			placeholderUsage0.put(placeholder, set);
		}
		placeholderUsage = placeholderUsage0;
	}

	@Override
	public void load() {
		refreshPlaceholderUsage();
		for (TabPlayer p : tab.getPlayers()) {
			onJoin(p);
		}
	}

	@Override
	public void unload() {
		//nothing
	}

	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		for (String identifier : getAllUsedPlaceholderIdentifiers()) {
			Placeholder pl = getPlaceholder(identifier);
			if (pl instanceof RelationalPlaceholder) {
				for (TabPlayer all : tab.getPlayers()) {
					((RelationalPlaceholder)pl).update(connectedPlayer, all);
					((RelationalPlaceholder)pl).update(all, connectedPlayer);
				}
			}
			if (pl instanceof PlayerPlaceholder) {
				((PlayerPlaceholder)pl).update(connectedPlayer);
			}
		}
	}
	
	@Override
	public void onQuit(TabPlayer disconnectedPlayer) {
		for (String identifier : getAllUsedPlaceholderIdentifiers()) {
			Placeholder pl = getPlaceholder(identifier);
			if (pl instanceof RelationalPlaceholder) {
				for (TabPlayer all : tab.getPlayers()) {
					((RelationalPlaceholder)pl).getLastValues().remove(all.getName() + "-" + disconnectedPlayer.getName());
					((RelationalPlaceholder)pl).getLastValues().remove(disconnectedPlayer.getName() + "-" + all.getName());
				}
			}
			if (pl instanceof PlayerPlaceholder) {
				((PlayerPlaceholder)pl).getLastValues().remove(disconnectedPlayer.getName());
				((PlayerPlaceholder)pl).getForceUpdate().remove(disconnectedPlayer.getName());
			}
		}
	}

	public void addRegistry(PlaceholderRegistry registry) {
		this.registry.add(registry);
	}
	public void registerPlaceholders() {
		for (PlaceholderRegistry r : registry) {
			for (Placeholder p : r.registerPlaceholders()) {
				registerPlaceholder(p);
			}
		}
		TABAPI.getAPIPlaceholders().values().forEach(this::registerPlaceholder);
		for (String placeholder : getAllUsedPlaceholderIdentifiers()) {
			categorizeUsedPlaceholder(placeholder);
		}
	}
	
	public int getRelationalRefresh(String identifier) {
		if (relationalPlaceholderRefreshIntervals.containsKey(identifier)) {
			return relationalPlaceholderRefreshIntervals.get(identifier);
		} else {
			return getDefaultRefresh();
		}
	}

	public Collection<Placeholder> getAllPlaceholders(){
		return new ArrayList<>(registeredPlaceholders.values());
	}
	
	public Placeholder getPlaceholder(String identifier) {
		return registeredPlaceholders.get(identifier);
	}
	
	public List<String> getUsedPlaceholderIdentifiersRecursive(String... strings){
		List<String> base = new ArrayList<>();
		for (String string : strings) {
			for (String s : detectAll(color(string))) {
				if (!base.contains(s)) base.add(s);
			}
		}
		for (String placeholder : new HashSet<>(base)) {
			Placeholder pl = TAB.getInstance().getPlaceholderManager().getPlaceholder(placeholder);
			if (pl == null) continue;
			for (String nestedString : pl.getNestedStrings()) {
				base.addAll(getUsedPlaceholderIdentifiersRecursive(color(nestedString)));
			}
		}
		return base;
	}
	
	public List<String> detectAll(String text){
		List<String> placeholders = new ArrayList<>();
		if (text == null) return placeholders;
		Matcher m = placeholderPattern.matcher(text);
		while (m.find()) {
			placeholders.add(m.group());
		}
		return placeholders;
	}
	
	@SuppressWarnings("unchecked")
	public void findAllUsed(Object object) {
		if (object instanceof Map) {
			for (Object value : ((Map<String, Object>) object).values()) {
				findAllUsed(value);
			}
		}
		if (object instanceof List) {
			for (Object line : (List<Object>)object) {
				findAllUsed(line);
			}
		}
		if (object instanceof String) {
			for (String placeholder : detectAll((String) object)) {
				getAllUsedPlaceholderIdentifiers().add(color(placeholder));
			}
		}
	}
	
	public void categorizeUsedPlaceholder(String identifier) {
		if (registeredPlaceholders.containsKey(identifier)) {
			//internal placeholder
			return;
		}

		//placeholderapi or invalid
		tab.getPlatform().registerUnknownPlaceholder(identifier);
	}
	
	public void registerPlaceholder(Placeholder placeholder) {
		if (placeholder == null) throw new IllegalArgumentException("placeholder cannot be null");
		registeredPlaceholders.put(placeholder.getIdentifier(), placeholder);
	}

	public void checkForRegistration(String... texts) {
		for (String text : texts) {
			for (String identifier : detectAll(text)) {
				getAllUsedPlaceholderIdentifiers().add(identifier);
				categorizeUsedPlaceholder(identifier);
			}
		}
		tab.getFeatureManager().refreshUsedPlaceholders();
		refreshPlaceholderUsage();
	}
	
	//code taken from bukkit, so it can work on bungee too
	public String color(String textToTranslate){
		if (textToTranslate == null) return null;
		if (!textToTranslate.contains("&")) return textToTranslate;
		char[] b = textToTranslate.toCharArray();
		for (int i = 0; i < b.length - 1; i++) {
			if ((b[i] == '&') && ("0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[(i + 1)]) > -1)){
				b[i] = '\u00a7';
				b[(i + 1)] = Character.toLowerCase(b[(i + 1)]);
			}
		}
		return new String(b);
	}
	
	//code taken from bukkit, so it can work on bungee too
	public String getLastColors(String input) {
		String result = "";
		int length = input.length();
		for (int index = length - 1; index > -1; index--){
			char section = input.charAt(index);
			if ((section == '\u00a7' || section == '&') && (index < length - 1)){
				char c = input.charAt(index + 1);
				if ("0123456789AaBbCcDdEeFfKkLlMmNnOoRr".contains(String.valueOf(c))) {
					result = "\u00a7" + c + result;
					if ("0123456789AaBbCcDdEeFfRr".contains(String.valueOf(c))) {
						break;
					}
				}
			}
		}
		return result;
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.PLACEHOLDER_REFRESHING;
	}

	public Set<String> getAllUsedPlaceholderIdentifiers() {
		return allUsedPlaceholderIdentifiers;
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
}