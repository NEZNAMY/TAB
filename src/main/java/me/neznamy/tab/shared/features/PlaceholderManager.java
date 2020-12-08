package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import me.neznamy.tab.api.AFKProvider;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.interfaces.Feature;
import me.neznamy.tab.shared.features.interfaces.QuitEventListener;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.PlaceholderRegistry;
import me.neznamy.tab.shared.placeholders.Placeholders;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
import me.neznamy.tab.shared.placeholders.RelationalPlaceholder;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;

/**
 * Messy class for placeholder management
 */
public class PlaceholderManager implements QuitEventListener {

	public int defaultRefresh;
	public Map<String, Integer> serverPlaceholderRefreshIntervals = new HashMap<String, Integer>();
	public Map<String, Integer> playerPlaceholderRefreshIntervals = new HashMap<String, Integer>();
	private Map<String, Integer> relationalPlaceholderRefreshIntervals = new HashMap<String, Integer>();

	private AFKProvider afk;
	private List<PlaceholderRegistry> registry = new ArrayList<>();
	
	private static PlaceholderManager instance;
	
	public long lastSuccessfulRefresh;

	public PlaceholderManager(){
		instance = this;
		loadRefreshIntervals();
		AtomicInteger atomic = new AtomicInteger();
		Shared.cpu.startRepeatingMeasuredTask(50, "refreshing placeholders", getFeatureType(), UsageType.REPEATING_TASK, new Runnable() {

			@Override
			public void run() {
				int loopTime = atomic.addAndGet(50);
				Set<TabPlayer> players = new HashSet<TabPlayer>();
				for (TabPlayer p : Shared.getPlayers()) {
					if (p.isLoaded()) players.add(p);
				}
				Map<TabPlayer, Set<Refreshable>> update = new HashMap<TabPlayer, Set<Refreshable>>();
				Map<TabPlayer, Set<Refreshable>> forceUpdate = new HashMap<TabPlayer, Set<Refreshable>>();
				boolean somethingChanged = false;
				for (String identifier : Placeholders.allUsedPlaceholderIdentifiers) {
					Placeholder placeholder = Placeholders.getPlaceholder(identifier);
					if (placeholder == null) continue;
					if (loopTime % placeholder.getRefresh() != 0) continue;
					if (placeholder instanceof RelationalPlaceholder) {
						RelationalPlaceholder relPlaceholder = (RelationalPlaceholder) placeholder;
						long startTime = System.nanoTime();
						for (TabPlayer p1 : players) {
							for (TabPlayer p2 : players) {
								if (relPlaceholder.update(p1, p2)) {
									if (!forceUpdate.containsKey(p2)) forceUpdate.put(p2, new HashSet<Refreshable>());
									forceUpdate.get(p2).addAll(getPlaceholderUsage(relPlaceholder.getIdentifier()));
									somethingChanged = true;
								}
								if (relPlaceholder.update(p2, p1)) {
									if (!forceUpdate.containsKey(p1)) forceUpdate.put(p1, new HashSet<Refreshable>());
									forceUpdate.get(p1).addAll(getPlaceholderUsage(relPlaceholder.getIdentifier()));
									somethingChanged = true;
								}
							}
						}
						Shared.cpu.addPlaceholderTime(relPlaceholder.getIdentifier(), System.nanoTime()-startTime);
					}
					if (placeholder instanceof PlayerPlaceholder) {
						long startTime = System.nanoTime();
						for (TabPlayer all : players) {
							if (((PlayerPlaceholder)placeholder).update(all)) {
								if (!update.containsKey(all)) update.put(all, new HashSet<Refreshable>());
								update.get(all).addAll(getPlaceholderUsage(placeholder.getIdentifier()));
								somethingChanged = true;
							}
						}
						Shared.cpu.addPlaceholderTime(placeholder.getIdentifier(), System.nanoTime()-startTime);
					}
					if (placeholder instanceof ServerPlaceholder) {
						long startTime = System.nanoTime();
						if (((ServerPlaceholder)placeholder).update()) {
							Set<Refreshable> usage = getPlaceholderUsage(placeholder.getIdentifier());
							somethingChanged = true;
							for (TabPlayer all : players) {
								if (!update.containsKey(all)) update.put(all, new HashSet<Refreshable>());
								update.get(all).addAll(usage);
							}
						}
						Shared.cpu.addPlaceholderTime(placeholder.getIdentifier(), System.nanoTime()-startTime);
					}
				}
				if (somethingChanged) {
					refresh(forceUpdate, update);
				}
				lastSuccessfulRefresh = System.currentTimeMillis();
			}
		});
	}
	
	private void loadRefreshIntervals() {
		for (Object category : Configs.config.getConfigurationSection("placeholderapi-refresh-intervals").keySet()) {
			if (!Arrays.asList("default-refresh-interval", "server", "player", "relational").contains(category.toString())) {
				Shared.errorManager.startupWarn("Unknown placeholder category \"" + category + "\". Valid categories are \"server\", \"player\" and \"relational\"");
			}
		}
		defaultRefresh = Configs.config.getInt("placeholderapi-refresh-intervals.default-refresh-interval", 100);
		for (Entry<Object, Object> placeholder : Configs.config.getConfigurationSection("placeholderapi-refresh-intervals.server").entrySet()) {
			serverPlaceholderRefreshIntervals.put(placeholder.getKey()+"", Shared.errorManager.parseInteger(placeholder.getValue()+"", defaultRefresh, "refresh interval"));
			Shared.debug("Loaded refresh " + placeholder.getValue() + " for SERVER placeholder " + placeholder.getKey());
		}
		for (Entry<Object, Object> placeholder : Configs.config.getConfigurationSection("placeholderapi-refresh-intervals.player").entrySet()) {
			playerPlaceholderRefreshIntervals.put(placeholder.getKey()+"", Shared.errorManager.parseInteger(placeholder.getValue()+"", defaultRefresh, "refresh interval"));
			Shared.debug("Loaded refresh " + placeholder.getValue() + " for PLAYER placeholder " + placeholder.getKey());
		}
		for (Entry<Object, Object> placeholder : Configs.config.getConfigurationSection("placeholderapi-refresh-intervals.relational").entrySet()) {
			relationalPlaceholderRefreshIntervals.put(placeholder.getKey()+"", Shared.errorManager.parseInteger(placeholder.getValue()+"", defaultRefresh, "refresh interval"));
			Shared.debug("Loaded refresh " + placeholder.getValue() + " for RELATIONAL placeholder " + placeholder.getKey());
		}
	}
	
	private void refresh(Map<TabPlayer, Set<Refreshable>> forceUpdate, Map<TabPlayer, Set<Refreshable>> update) {
		for (Entry<TabPlayer, Set<Refreshable>> entry : update.entrySet()) {
			if (forceUpdate.containsKey(entry.getKey())) {
				entry.getValue().removeAll(forceUpdate.get(entry.getKey()));
			}
		}
		Shared.cpu.runTask("refreshing", new Runnable() {

			@Override
			public void run() {
				for (Entry<TabPlayer, Set<Refreshable>> entry : forceUpdate.entrySet()) {
					for (Refreshable r : entry.getValue()) {
						long startTime = System.nanoTime();
						r.refresh(entry.getKey(), true);
						Shared.cpu.addTime(r.getFeatureType(), UsageType.REFRESHING, System.nanoTime()-startTime);
					}
				}
				for (Entry<TabPlayer, Set<Refreshable>> entry : update.entrySet()) {
					for (Refreshable r : entry.getValue()) {
						long startTime = System.nanoTime();
						r.refresh(entry.getKey(), false);
						Shared.cpu.addTime(r.getFeatureType(), UsageType.REFRESHING, System.nanoTime()-startTime);
					}
				}
			}
		});
	}
	public static PlaceholderManager getInstance() {
		return instance;
	}
	public static Set<Refreshable> getPlaceholderUsage(String identifier){
		Set<Refreshable> set = new HashSet<Refreshable>();
		for (Feature r : new ArrayList<>(Shared.featureManager.getAllFeatures())) {
			if (!(r instanceof Refreshable)) continue;
			if (((Refreshable)r).getUsedPlaceholders().contains(identifier)) set.add((Refreshable) r);
		}
		return set;
	}

	@Override
	public void onQuit(TabPlayer disconnectedPlayer) {
		for (Placeholder pl : Placeholders.getAllPlaceholders()) {
			if (pl instanceof RelationalPlaceholder) {
				for (TabPlayer all : Shared.getPlayers()) {
					((RelationalPlaceholder)pl).lastValue.remove(all.getName() + "-" + disconnectedPlayer.getName());
					((RelationalPlaceholder)pl).lastValue.remove(disconnectedPlayer.getName() + "-" + all.getName());
				}
			}
			if (pl instanceof PlayerPlaceholder) {
				((PlayerPlaceholder)pl).lastValue.remove(disconnectedPlayer.getName());
				((PlayerPlaceholder)pl).forceUpdate.remove(disconnectedPlayer.getName());
			}
		}
	}
	public AFKProvider getAFKProvider() {
		return afk;
	}
	public void setAFKProvider(AFKProvider afk) {
		Shared.debug("Loaded AFK provider: " + afk.getClass().getSimpleName());
		this.afk = afk;
	}
	public void addRegistry(PlaceholderRegistry registry) {
		this.registry.add(registry);
	}
	public void registerPlaceholders() {
		registry.forEach(r -> r.registerPlaceholders());
		for (String placeholder : Placeholders.allUsedPlaceholderIdentifiers) {
			Placeholders.categorizeUsedPlaceholder(placeholder);
		}
	}
	
	public int getRelationalRefresh(String identifier) {
		if (relationalPlaceholderRefreshIntervals.containsKey(identifier)) {
			return relationalPlaceholderRefreshIntervals.get(identifier);
		} else {
			return defaultRefresh;
		}
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.PLACEHOLDER_REFRESHING;
	}
}