package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import me.neznamy.tab.api.AFKProvider;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
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
	public Map<String, Integer> relationalPlaceholderRefreshIntervals = new HashMap<String, Integer>();

	private AFKProvider afk;
	private List<PlaceholderRegistry> registry = new ArrayList<>();
	
	private static PlaceholderManager instance;

	public PlaceholderManager(){
		instance = this;
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

		AtomicInteger atomic = new AtomicInteger();
		Shared.cpu.startRepeatingMeasuredTask(50, "refreshing placeholders", getFeatureType(), UsageType.REPEATING_TASK, new Runnable() {

			@Override
			public void run() {
				int loopTime = atomic.addAndGet(50);
				Collection<ITabPlayer> allPlayers = Shared.getPlayers();
				Collection<ITabPlayer> players = new ArrayList<ITabPlayer>();
				for (ITabPlayer p : allPlayers) {
					if (p.onJoinFinished) players.add(p);
				}
				Map<ITabPlayer, Set<Refreshable>> update = new HashMap<ITabPlayer, Set<Refreshable>>();
				Map<ITabPlayer, Set<Refreshable>> forceUpdate = new HashMap<ITabPlayer, Set<Refreshable>>();
				boolean somethingChanged = false;
				for (RelationalPlaceholder relPlaceholder : Placeholders.registeredRelationalPlaceholders.values()) {
					if (loopTime % relPlaceholder.refresh != 0) continue;
					long startTime = System.nanoTime();
					for (ITabPlayer p1 : players) {
						for (ITabPlayer p2 : players) {
							if (relPlaceholder.update(p1, p2)) {
								if (!forceUpdate.containsKey(p2)) forceUpdate.put(p2, new HashSet<Refreshable>());
								forceUpdate.get(p2).addAll(getPlaceholderUsage(relPlaceholder.identifier));
								somethingChanged = true;
							}
							if (relPlaceholder.update(p2, p1)) {
								if (!forceUpdate.containsKey(p1)) forceUpdate.put(p1, new HashSet<Refreshable>());
								forceUpdate.get(p1).addAll(getPlaceholderUsage(relPlaceholder.identifier));
								somethingChanged = true;
							}
						}
					}
					Shared.cpu.addPlaceholderTime(relPlaceholder.identifier, System.nanoTime()-startTime);
				}
				for (Placeholder placeholder : new HashSet<>(Placeholders.usedPlaceholders)) { //avoiding concurrent modification on reload
					if (loopTime % placeholder.cooldown != 0) continue;
//					System.out.println(placeholder.getIdentifier() + " - " + placeholder.cooldown);
					if (placeholder instanceof PlayerPlaceholder) {
						long startTime = System.nanoTime();
						for (ITabPlayer all : players) {
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
							for (ITabPlayer all : players) {
								if (!update.containsKey(all)) update.put(all, new HashSet<Refreshable>());
								update.get(all).addAll(usage);
							}
						}
						Shared.cpu.addPlaceholderTime(placeholder.getIdentifier(), System.nanoTime()-startTime);
					}
				}
				if (somethingChanged) {
					for (Entry<ITabPlayer, Set<Refreshable>> entry : update.entrySet()) {
						if (forceUpdate.containsKey(entry.getKey())) {
							entry.getValue().removeAll(forceUpdate.get(entry.getKey()));
						}
					}
					Shared.cpu.runTask("refreshing", new Runnable() {

						@Override
						public void run() {
							for (Entry<ITabPlayer, Set<Refreshable>> entry : forceUpdate.entrySet()) {
								for (Refreshable r : entry.getValue()) {
									long startTime = System.nanoTime();
									r.refresh(entry.getKey(), true);
									Shared.cpu.addTime(r.getFeatureType(), UsageType.REFRESHING, System.nanoTime()-startTime);
								}
							}
							for (Entry<ITabPlayer, Set<Refreshable>> entry : update.entrySet()) {
								for (Refreshable r : entry.getValue()) {
									long startTime = System.nanoTime();
									r.refresh(entry.getKey(), false);
									Shared.cpu.addTime(r.getFeatureType(), UsageType.REFRESHING, System.nanoTime()-startTime);
								}
							}
						}
					});
				}
			}
		});
	}
	public static PlaceholderManager getInstance() {
		return instance;
	}
	public static Set<Refreshable> getPlaceholderUsage(String identifier){
		Set<Refreshable> set = new HashSet<Refreshable>();
		for (Refreshable r : Shared.featureManager.refreshables) {
			if (r.getUsedPlaceholders().contains(identifier)) set.add(r);
		}
		return set;
	}

	@Override
	public void onQuit(ITabPlayer disconnectedPlayer) {
		for (Placeholder pl : Placeholders.getAllPlaceholders()) {
			if (pl instanceof PlayerPlaceholder) {
				((PlayerPlaceholder)pl).lastValue.remove(disconnectedPlayer.getName());
			}
		}
		for (RelationalPlaceholder pl : Placeholders.registeredRelationalPlaceholders.values()) {
			for (ITabPlayer all : Shared.getPlayers()) {
				pl.lastValue.remove(all.getName() + "-" + disconnectedPlayer.getName());
				pl.lastValue.remove(disconnectedPlayer.getName() + "-" + all.getName());
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
	@Override
	public TabFeature getFeatureType() {
		return TabFeature.PLACEHOLDER_REFRESHING;
	}
}