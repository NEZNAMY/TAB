package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;

import me.neznamy.tab.api.ArmorStand;
import me.neznamy.tab.api.ArmorStandManager;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.premium.SortingType;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.NameTag;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.QuitEventListener;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.features.interfaces.RespawnEventListener;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;

/**
 * The core class for unlimited nametag mode
 */
public class NameTagX extends NameTag implements Loadable, JoinEventListener, QuitEventListener, WorldChangeListener, Refreshable, RespawnEventListener {

	private JavaPlugin plugin;
	public boolean markerFor18x;
	private Set<String> usedPlaceholders;
	public List<String> dynamicLines = Arrays.asList("belowname", "nametag", "abovename");
	public Map<String, Object> staticLines = new ConcurrentHashMap<String, Object>();

	public Map<Integer, List<Integer>> vehicles = new ConcurrentHashMap<>();
	public Map<TabPlayer, List<TabPlayer>> delayedSpawn = new HashMap<TabPlayer, List<TabPlayer>>();
	private EventListener eventListener;

	public NameTagX(JavaPlugin plugin) {
		this.plugin = plugin;
		markerFor18x = Configs.config.getBoolean("unlimited-nametag-prefix-suffix-mode.use-marker-tag-for-1-8-x-clients", false);
		if (Premium.is()) {
			List<String> realList = Premium.premiumconfig.getStringList("unlimited-nametag-mode-dynamic-lines", Arrays.asList("abovename", "nametag", "belowname", "another"));
			dynamicLines = new ArrayList<String>();
			dynamicLines.addAll(realList);
			Collections.reverse(dynamicLines);
			staticLines = Premium.premiumconfig.getConfigurationSection("unlimited-nametag-mode-static-lines");
		}
		refreshUsedPlaceholders();
		eventListener = new EventListener(this);
		Shared.featureManager.registerFeature("nametagx-packet", new PacketListener(this));
	}
	
	@Override
	public void load() {
		Bukkit.getPluginManager().registerEvents(eventListener, plugin);
		for (TabPlayer all : Shared.getPlayers()){
			all.setTeamName(SortingType.INSTANCE.getTeamName(all));
			updateProperties(all);
			if (isDisabledWorld(all.getWorldName())) continue;
			all.registerTeam();
			loadArmorStands(all);
			if (((Entity) all.getPlayer()).getVehicle() != null) {
				Entity vehicle = ((Entity) all.getPlayer()).getVehicle();
				List<Integer> list = new ArrayList<Integer>();
				for (Entity e : getPassengers(vehicle)) {
					list.add(e.getEntityId());
				}
				vehicles.put(vehicle.getEntityId(), list);
			}
			for (TabPlayer worldPlayer : Shared.getPlayers()) {
				if (all == worldPlayer) continue;
				if (!worldPlayer.getWorldName().equals(all.getWorldName())) continue;
				all.getArmorStandManager().spawn(worldPlayer);
			}
		}
		Shared.cpu.startRepeatingMeasuredTask(200, "refreshing nametag visibility", getFeatureType(), UsageType.REFRESHING_NAMETAG_VISIBILITY, new Runnable() {
			public void run() {
				for (TabPlayer p : Shared.getPlayers()) {
					if (!p.isLoaded() || isDisabledWorld(p.getWorldName())) continue;
					boolean onBoat = ((Player)p.getPlayer()).getVehicle() != null && ((Player)p.getPlayer()).getVehicle().getType() == EntityType.BOAT;
					if (p.isOnBoat() != onBoat) {
						p.setOnBoat(onBoat);
						p.updateTeamData();
					}
					p.getArmorStandManager().updateVisibility();
				}
			}
		});
		startCollisionRefreshingTask();
	}
	
	@Override
	public void unload() {
		HandlerList.unregisterAll(eventListener);
		for (TabPlayer p : Shared.getPlayers()) {
			if (!isDisabledWorld(p.getWorldName())) p.unregisterTeam();
			p.getArmorStandManager().destroy();
		}
	}
	
	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		connectedPlayer.setTeamName(SortingType.INSTANCE.getTeamName(connectedPlayer));
		updateProperties(connectedPlayer);
		for (TabPlayer all : Shared.getPlayers()) {
			if (all == connectedPlayer) continue;
			if (!isDisabledWorld(all.getWorldName())) all.registerTeam(connectedPlayer);
		}
		if (isDisabledWorld(connectedPlayer.getWorldName())) return;
		connectedPlayer.registerTeam();
		loadArmorStands(connectedPlayer);
		if (((Entity) connectedPlayer.getPlayer()).getVehicle() != null) {
			Entity vehicle = ((Entity) connectedPlayer.getPlayer()).getVehicle();
			List<Integer> list = new ArrayList<Integer>();
			for (Entity e : getPassengers(vehicle)) {
				list.add(e.getEntityId());
			}
			vehicles.put(vehicle.getEntityId(), list);
		}
		if (delayedSpawn.containsKey(connectedPlayer)) {
			for (TabPlayer viewer : delayedSpawn.get(connectedPlayer)) {
				connectedPlayer.getArmorStandManager().spawn(viewer);
			}
			delayedSpawn.remove(connectedPlayer);
		}
	}
	
	@Override
	public void onQuit(TabPlayer disconnectedPlayer) {
		if (!isDisabledWorld(disconnectedPlayer.getWorldName())) disconnectedPlayer.unregisterTeam();
		Shared.cpu.runTaskLater(100, "Processing player quit", getFeatureType(), UsageType.PLAYER_QUIT_EVENT, new Runnable() {

			@Override
			public void run() {
				for (TabPlayer all : Shared.getPlayers()) {
					if (all.getArmorStandManager() != null) all.getArmorStandManager().unregisterPlayer(disconnectedPlayer);
				}
				disconnectedPlayer.getArmorStandManager().destroy();
			}
		});
	}
	
	@Override
	public void onWorldChange(TabPlayer p, String from, String to) {
		if (!p.isLoaded()) return;
		updateProperties(p);
		if (isDisabledWorld(p.getWorldName()) && !isDisabledWorld(disabledWorlds, from)) {
			p.unregisterTeam();
		} else if (!isDisabledWorld(p.getWorldName()) && isDisabledWorld(disabledWorlds, from)) {
			p.registerTeam();
		} else {
			p.updateTeam();
			p.getArmorStandManager().refresh();
			fixArmorStandHeights(p);
		}
	}
	
	public void loadArmorStands(TabPlayer pl) {
		pl.setArmorStandManager(new ArmorStandManager());
		pl.setProperty("nametag", pl.getProperty("tagprefix").getCurrentRawValue() + pl.getProperty("customtagname").getCurrentRawValue() + pl.getProperty("tagsuffix").getCurrentRawValue());
		double height = -Configs.SECRET_NTX_space;
		for (String line : dynamicLines) {
			Property p = pl.getProperty(line);
			pl.getArmorStandManager().addArmorStand(line, new BukkitArmorStand(pl, p, height+=Configs.SECRET_NTX_space, false));
		}
		for (Entry<String, Object> line : staticLines.entrySet()) {
			Property p = pl.getProperty(line.getKey());
			pl.getArmorStandManager().addArmorStand(line.getKey(), new BukkitArmorStand(pl, p, Double.parseDouble(line.getValue()+""), true));
		}
		fixArmorStandHeights(pl);
	}
	
	public void fixArmorStandHeights(TabPlayer p) {
		p.getArmorStandManager().refresh();
		double currentY = -Configs.SECRET_NTX_space;
		for (ArmorStand as : p.getArmorStandManager().getArmorStands()) {
			if (as.hasStaticOffset()) continue;
			if (as.getProperty().get().length() != 0) {
				currentY += Configs.SECRET_NTX_space;
				as.setOffset(currentY);
			}
		}
	}

	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (isDisabledWorld(refreshed.getWorldName())) return;
		boolean refresh;
		if (force) {
			updateProperties(refreshed);
			refresh = true;
		} else {
			boolean prefix = refreshed.getProperty("tagprefix").update();
			boolean suffix = refreshed.getProperty("tagsuffix").update();
			refresh = prefix || suffix;
		}
		if (refresh) refreshed.updateTeam();
		boolean fix = false;
		for (ArmorStand as : refreshed.getArmorStandManager().getArmorStands()) {
			if (as.getProperty().update() || force) {
				as.refresh();
				fix = true;
			}
		}
		if (fix) fixArmorStandHeights(refreshed);
	}
	
	private void updateProperties(TabPlayer p) {
		p.loadPropertyFromConfig("tagprefix");
		p.loadPropertyFromConfig("customtagname", p.getName());
		p.loadPropertyFromConfig("tagsuffix");
		p.setProperty("nametag", p.getProperty("tagprefix").getCurrentRawValue() + p.getProperty("customtagname").getCurrentRawValue() + p.getProperty("tagsuffix").getCurrentRawValue());
		for (String property : dynamicLines) {
			if (!property.equals("nametag")) p.loadPropertyFromConfig(property);
		}
		for (String property : staticLines.keySet()) {
			if (!property.equals("nametag")) p.loadPropertyFromConfig(property);
		}
	}
	
	@Override
	public Set<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}
	
	@SuppressWarnings("deprecation")
	public List<Entity> getPassengers(Entity vehicle){
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 11) {
			return vehicle.getPassengers();
		} else {
			if (vehicle.getPassenger() != null) {
				return Lists.newArrayList(vehicle.getPassenger()); 
			} else {
				return new ArrayList<Entity>();
			}
		}
	}
	
	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = Configs.config.getUsedPlaceholderIdentifiersRecursive("tagprefix", "customtagname", "tagsuffix");
		for (String line : dynamicLines) {
			usedPlaceholders.addAll(Configs.config.getUsedPlaceholderIdentifiersRecursive(line));
		}
		for (String line : staticLines.keySet()) {
			usedPlaceholders.addAll(Configs.config.getUsedPlaceholderIdentifiersRecursive(line));
		}
	}

	/**
	 * Returns name of the feature displayed in /tab cpu
	 * @return name of the feature displayed in /tab cpu
	 */
	@Override
	public TabFeature getFeatureType() {
		return TabFeature.NAMETAGX;
	}

	@Override
	public void onRespawn(TabPlayer respawned) {
		if (!isDisabledWorld(respawned.getWorldName())) Shared.cpu.runMeasuredTask("processing PlayerRespawnEvent", TabFeature.NAMETAGX, UsageType.PLAYER_RESPAWN_EVENT, new Runnable() {
			
			@Override
			public void run() {
				respawned.getArmorStandManager().teleport();
			}
		});
	}
}