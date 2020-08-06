package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;

import com.google.common.collect.Lists;

import me.neznamy.tab.platforms.bukkit.Main;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.premium.SortingType;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.QuitEventListener;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;

public class NameTagX implements Loadable, JoinEventListener, QuitEventListener, WorldChangeListener, Refreshable{

	public boolean markerFor18x;
	private Set<String> usedPlaceholders;
	public List<String> dynamicLines = Arrays.asList("belowname", "nametag", "abovename");
	public Map<String, Object> staticLines = new ConcurrentHashMap<String, Object>();

	public Map<Integer, List<Integer>> vehicles = new ConcurrentHashMap<>();
	private EventListener eventListener;
	private PacketListener packetListener;

	@SuppressWarnings("unchecked")
	public NameTagX() {
		markerFor18x = Configs.config.getBoolean("unlimited-nametag-prefix-suffix-mode.use-marker-tag-for-1-8-x-clients", false);
		if (Premium.is()) {
			List<String> realList = Premium.premiumconfig.getStringList("unlimited-nametag-mode-dynamic-lines", Arrays.asList("abovename", "nametag", "belowname", "another"));
			dynamicLines = new ArrayList<String>();
			dynamicLines.addAll(realList);
			Collections.reverse(dynamicLines);
			staticLines = Premium.premiumconfig.getConfigurationSection("unlimited-nametag-mode-static-lines");
		}
		refreshUsedPlaceholders();
		eventListener = new EventListener();
		packetListener = new PacketListener(this);
		Shared.registerFeature("nametagx-packet", packetListener);
	}
	@Override
	public void load() {
		Bukkit.getPluginManager().registerEvents(eventListener, Main.INSTANCE);
		for (ITabPlayer all : Shared.getPlayers()){
			all.teamName = SortingType.INSTANCE.getTeamName(all);
			updateProperties(all);
			if (all.disabledNametag) continue;
			all.registerTeam();
			loadArmorStands(all);
			if (all.getBukkitEntity().getVehicle() != null) {
				Entity vehicle = all.getBukkitEntity().getVehicle();
				List<Integer> list = new ArrayList<Integer>();
				for (Entity e : getPassengers(vehicle)) {
					list.add(e.getEntityId());
				}
				vehicles.put(vehicle.getEntityId(), list);
			}
			for (ITabPlayer worldPlayer : Shared.getPlayers()) {
				if (all == worldPlayer) continue;
				if (!worldPlayer.getWorldName().equals(all.getWorldName())) continue;
				all.getArmorStandManager().spawn(worldPlayer);
			}
		}
		Shared.featureCpu.startRepeatingMeasuredTask(200, "refreshing nametag visibility", CPUFeature.NAMETAGX_INVISCHECK, new Runnable() {
			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) {
					if (!p.onJoinFinished || p.disabledNametag) continue;
					p.getArmorStandManager().updateVisibility();
				}
			}
		});
		Shared.featureCpu.startRepeatingMeasuredTask(200, "refreshing collision", CPUFeature.NAMETAG_COLLISION, new Runnable() {
			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) {
					if (!p.onJoinFinished || p.disabledNametag) continue;
					boolean collision = p.getTeamPush();
					if (p.lastCollision != collision) {
						p.updateTeamData();
					}
				}
			}
		});
	}
	@Override
	public void unload() {
		HandlerList.unregisterAll(eventListener);
		for (ITabPlayer p : Shared.getPlayers()) {
			if (!p.disabledNametag) p.unregisterTeam();
			p.getArmorStandManager().destroy();
		}
	}
	@Override
	public void onJoin(ITabPlayer connectedPlayer) {
		connectedPlayer.teamName = SortingType.INSTANCE.getTeamName(connectedPlayer);
		updateProperties(connectedPlayer);
		for (ITabPlayer all : Shared.getPlayers()) {
			if (all == connectedPlayer) continue;
			if (!all.disabledNametag) all.registerTeam(connectedPlayer);
		}
		if (connectedPlayer.disabledNametag) return;
		connectedPlayer.registerTeam();
		loadArmorStands(connectedPlayer);
		if (connectedPlayer.getBukkitEntity().getVehicle() != null) {
			Entity vehicle = connectedPlayer.getBukkitEntity().getVehicle();
			List<Integer> list = new ArrayList<Integer>();
			for (Entity e : getPassengers(vehicle)) {
				list.add(e.getEntityId());
			}
			vehicles.put(vehicle.getEntityId(), list);
		}
	}
	private void updateProperties(ITabPlayer p) {
		p.properties.get("tagprefix").update();
		p.properties.get("customtagname").update();
		p.properties.get("tagsuffix").update();
		for (String line : dynamicLines) {
			p.properties.get(line).update();
		}
		for (String line : staticLines.keySet()) {
			p.properties.get(line).update();
		}
	}
	@Override
	public void onQuit(ITabPlayer disconnectedPlayer) {
		Shared.featureCpu.runTaskLater(100, "Processing player quit", CPUFeature.NAMETAGX_EVENT_QUIT, new Runnable() {

			@Override
			public void run() {
				if (!disconnectedPlayer.disabledNametag) disconnectedPlayer.unregisterTeam();
				for (ITabPlayer all : Shared.getPlayers()) {
					all.getArmorStandManager().unregisterPlayer(disconnectedPlayer);
				}
				disconnectedPlayer.getArmorStandManager().destroy();
			}
		});
	}
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		if (p.disabledNametag && !p.isDisabledWorld(Configs.disabledNametag, from)) {
			p.unregisterTeam();
		} else if (!p.disabledNametag && p.isDisabledWorld(Configs.disabledNametag, from)) {
			p.registerTeam();
		} else {
			p.updateTeam();
			p.getArmorStandManager().refresh();
			fixArmorStandHeights(p);
		}
	}
	public void loadArmorStands(ITabPlayer pl) {
		pl.setArmorStandManager(new ArmorStandManager());
		pl.setProperty("nametag", pl.properties.get("tagprefix").getCurrentRawValue() + pl.properties.get("customtagname").getCurrentRawValue() + pl.properties.get("tagsuffix").getCurrentRawValue(), null);
		double height = -Configs.SECRET_NTX_space;
		for (String line : dynamicLines) {
			Property p = pl.properties.get(line);
			pl.getArmorStandManager().addArmorStand(line, new ArmorStand(pl, p, height+=Configs.SECRET_NTX_space, false));
		}
		for (Entry<String, Object> line : staticLines.entrySet()) {
			Property p = pl.properties.get(line.getKey());
			pl.getArmorStandManager().addArmorStand(line.getKey(), new ArmorStand(pl, p, Double.parseDouble(line.getValue()+""), true));
		}
		fixArmorStandHeights(pl);
	}
	public void fixArmorStandHeights(ITabPlayer p) {
		p.getArmorStandManager().refresh();
		double currentY = -Configs.SECRET_NTX_space;
		for (ArmorStand as : p.getArmorStandManager().getArmorStands()) {
			if (as.hasStaticOffset()) continue;
			if (as.property.get().length() != 0) {
				currentY += Configs.SECRET_NTX_space;
				as.setOffset(currentY);
			}
		}
	}

	@Override
	public void refresh(ITabPlayer refreshed, boolean force) {
		if (refreshed.disabledNametag) return;
		boolean prefix = refreshed.properties.get("tagprefix").update();
		boolean suffix = refreshed.properties.get("tagsuffix").update();
		if (prefix || suffix || force) refreshed.updateTeam();
		boolean fix = false;
		for (ArmorStand as : refreshed.getArmorStandManager().getArmorStands()) {
			if (as.property.update() || force) {
				as.refresh();
				fix = true;
			}
		}
		if (fix) fixArmorStandHeights(refreshed);
	}
	@Override
	public Set<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}
	@Override
	public CPUFeature getRefreshCPU() {
		return CPUFeature.NAMETAG;
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
}