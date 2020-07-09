package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import me.neznamy.tab.platforms.bukkit.Main;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.premium.SortingType;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.PlayerInfoPacketListener;
import me.neznamy.tab.shared.features.interfaces.QuitEventListener;
import me.neznamy.tab.shared.features.interfaces.RawPacketFeature;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class NameTagX implements Listener, Loadable, JoinEventListener, QuitEventListener, WorldChangeListener, RawPacketFeature, PlayerInfoPacketListener, Refreshable{

	private boolean modifyNPCnames;
	public boolean markerFor18x;
	private Set<String> usedPlaceholders;
	public List<String> dynamicLines = Arrays.asList("belowname", "nametag", "abovename");
	public Map<String, Object> staticLines = new HashMap<String, Object>();

	@SuppressWarnings("unchecked")
	public NameTagX() {
		usedPlaceholders = Configs.config.getUsedPlaceholderIdentifiersRecursive("tagprefix", "customtagname", "tagsuffix");
		for (String line : dynamicLines) {
			usedPlaceholders.addAll(Configs.config.getUsedPlaceholderIdentifiersRecursive(line));
		}
		for (String line : staticLines.keySet()) {
			usedPlaceholders.addAll(Configs.config.getUsedPlaceholderIdentifiersRecursive(line));
		}
		modifyNPCnames = Configs.config.getBoolean("unlimited-nametag-prefix-suffix-mode.modify-npc-names", false);
		markerFor18x = Configs.config.getBoolean("unlimited-nametag-prefix-suffix-mode.use-marker-tag-for-1-8-x-clients", false);
		if (Premium.is()) {
			List<String> realList = Premium.premiumconfig.getStringList("unlimited-nametag-mode-dynamic-lines", Arrays.asList("abovename", "nametag", "belowname", "another"));
			dynamicLines = new ArrayList<String>();
			dynamicLines.addAll(realList);
			Collections.reverse(dynamicLines);
			staticLines = Premium.premiumconfig.getConfigurationSection("unlimited-nametag-mode-static-lines");
		}
	}
	@Override
	public void load() {
		Bukkit.getPluginManager().registerEvents(this, Main.instance);
		for (ITabPlayer all : Shared.getPlayers()){
			all.teamName = SortingType.INSTANCE.getTeamName(all);
			updateProperties(all);
			if (all.disabledNametag) continue;
			all.registerTeam();
			loadArmorStands(all);
			for (ITabPlayer worldPlayer : Shared.getPlayers()) {
				if (all == worldPlayer) continue;
				if (!worldPlayer.getWorldName().equals(all.getWorldName())) continue;
				spawnArmorStand(all, worldPlayer);
			}
		}
		Shared.featureCpu.startRepeatingMeasuredTask(200, "refreshing nametag visibility", CPUFeature.NAMETAGX_INVISCHECK, new Runnable() {
			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) {
					if (p.disabledNametag) continue;
					synchronized(p.armorStands) {
						p.getArmorStands().forEach(a -> a.updateVisibility());
					}
				}
			}
		});
	}
	@Override
	public void unload() {
		HandlerList.unregisterAll(this);
		for (ITabPlayer p : Shared.getPlayers()) {
			if (!p.disabledNametag) p.unregisterTeam();
			p.getArmorStands().forEach(a -> a.destroy());
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
		if (connectedPlayer.armorStands.isEmpty()) {
			//armor stands force loaded before due to an inefficient placeholder
			loadArmorStands(connectedPlayer);
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
		Shared.featureCpu.runMeasuredTask("Processing player quit", CPUFeature.NAMETAGX_EVENT_QUIT, new Runnable() {

			@Override
			public void run() {
				try {
					if (!disconnectedPlayer.disabledNametag) disconnectedPlayer.unregisterTeam();
					for (ITabPlayer all : Shared.getPlayers()) {
						List<ArmorStand> armorStands = new ArrayList<>();
						armorStands.addAll(all.getArmorStands());
						armorStands.forEach(a -> a.removeFromRegistered(disconnectedPlayer));
					}
					disconnectedPlayer.getArmorStands().forEach(a -> a.destroy());
					int asCount = disconnectedPlayer.getArmorStands().size();
					int[] armorStandIds = new int[asCount];
					for (int i=0; i<asCount; i++) {
						armorStandIds[i] = disconnectedPlayer.getArmorStands().get(i).getEntityId();
					}
					Thread.sleep(100);
					for (ITabPlayer all : Shared.getPlayers()) {
						all.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityDestroy(armorStandIds));
					}
				} catch (InterruptedException e) {

				}
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
			List<ArmorStand> list = new ArrayList<ArmorStand>();
			list.addAll(p.armorStands);
			list.forEach(a -> a.refresh());
			fixArmorStandHeights(p);
		}
	}
	public void restartArmorStands(ITabPlayer p) {
		p.getArmorStands().forEach(a -> a.destroy());
		p.armorStands.clear();
		if (p.disabledNametag) return;
		loadArmorStands(p);
		for (ITabPlayer worldPlayer : Shared.getPlayers()) {
			if (p == worldPlayer) continue;
			if (!worldPlayer.getWorldName().equals(p.getWorldName())) continue;
			spawnArmorStand(p, worldPlayer);
		}
		if (p.previewingNametag) spawnArmorStand(p, p);
	}
	public void loadArmorStands(ITabPlayer pl) {
		pl.armorStands.clear();
		pl.setProperty("nametag", pl.properties.get("tagprefix").getCurrentRawValue() + pl.properties.get("customtagname").getCurrentRawValue() + pl.properties.get("tagsuffix").getCurrentRawValue(), null);
		double height = -Configs.SECRET_NTX_space;
		for (String line : dynamicLines) {
			Property p = pl.properties.get(line);
			pl.armorStands.add(new ArmorStand(pl, p, height+=Configs.SECRET_NTX_space, false));
		}
		for (Entry<String, Object> line : staticLines.entrySet()) {
			Property p = pl.properties.get(line.getKey());
			pl.armorStands.add(new ArmorStand(pl, p, Double.parseDouble(line.getValue()+""), true));
		}
		fixArmorStandHeights(pl);
	}
	public void fixArmorStandHeights(ITabPlayer p) {
		p.armorStands.forEach(a -> a.refresh());
		double currentY = -Configs.SECRET_NTX_space;
		for (ArmorStand as : p.getArmorStands().toArray(new ArmorStand[0])) {
			if (as.hasStaticOffset()) continue;
			if (as.property.get().length() != 0) {
				currentY += Configs.SECRET_NTX_space;
				as.setOffset(currentY);
			}
		}
	}
	@Override
	public Object onPacketReceive(ITabPlayer sender, Object packet) throws Throwable {
		if (sender.getVersion().getMinorVersion() == 8 && MethodAPI.PacketPlayInUseEntity.isInstance(packet)) {
			int entityId = MethodAPI.PacketPlayInUseEntity_ENTITY.getInt(packet);
			ITabPlayer attacked = null;
			loop:
				for (ITabPlayer all : Shared.getPlayers()) {
					for (ArmorStand as : all.getArmorStands()) {
						if (as.getEntityId() == entityId) {
							attacked = all;
							break loop;
						}
					}
				}
			if (attacked != null && attacked != sender) {
				MethodAPI.PacketPlayInUseEntity_ENTITY.set(packet, attacked.getBukkitEntity().getEntityId());
			}
		}
		return packet;
	}
	@Override
	public Object onPacketSend(ITabPlayer receiver, Object packet) throws Throwable {
		if (MethodAPI.PacketPlayOutNamedEntitySpawn.isInstance(packet)) {
			int entity = MethodAPI.PacketPlayOutNamedEntitySpawn_ENTITYID.getInt(packet);
			ITabPlayer spawnedPlayer = Shared.entityIdMap.get(entity);
			if (spawnedPlayer != null && !spawnedPlayer.disabledNametag) Shared.featureCpu.runMeasuredTask("processing NamedEntitySpawn", CPUFeature.NAMETAGX_PACKET_NAMED_ENTITY_SPAWN, new Runnable() {
				public void run() {
					spawnArmorStand(spawnedPlayer, receiver);
				}
			});
		}
		if (MethodAPI.PacketPlayOutEntityDestroy.isInstance(packet)) {
			int[] entites = (int[]) MethodAPI.PacketPlayOutEntityDestroy_ENTITIES.get(packet);
			for (int id : entites) {
				ITabPlayer despawnedPlayer = Shared.entityIdMap.get(id);
				if (despawnedPlayer != null && !despawnedPlayer.disabledNametag) Shared.featureCpu.runMeasuredTask("processing EntityDestroy", CPUFeature.NAMETAGX_PACKET_ENTITY_DESTROY, new Runnable() {
					public void run() {
						despawnedPlayer.getArmorStands().forEach(a -> a.destroy(receiver));
					}
				});
			}
		}
		return packet;
	}
	@Override
	public CPUFeature getCPUName() {
		return CPUFeature.NAMETAGX_PACKET_LISTENING;
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(PlayerToggleSneakEvent e) {
		ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p == null) return;
		if (!p.disabledNametag) Shared.featureCpu.runMeasuredTask("processing PlayerToggleSneakEvent", CPUFeature.NAMETAGX_EVENT_SNEAK, new Runnable() {
			public void run() {
				p.getArmorStands().forEach(a -> a.sneak(e.isSneaking()));
			}
		});
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(PlayerMoveEvent e) {
		if (e.getFrom().getX() == e.getTo().getX() && e.getFrom().getY() == e.getTo().getY() && e.getFrom().getZ() == e.getTo().getZ()) return; //player only moved head
		ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p == null) return;
		if (!p.disabledNametag) Shared.featureCpu.runMeasuredTask("processing PlayerMoveEvent", CPUFeature.NAMETAGX_EVENT_MOVE, new Runnable() {
			public void run() {
				for (ArmorStand as : p.getArmorStands()) {
					as.updateLocation(e.getTo());
					List<ITabPlayer> nearbyPlayers = as.getNearbyPlayers();
					synchronized (nearbyPlayers){
						for (ITabPlayer nearby : nearbyPlayers) {
							nearby.sendPacket(as.getTeleportPacket(nearby));
						}
					}
				}
			}
		});
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(PlayerRespawnEvent e) {
		ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p == null) return;
		if (!p.disabledNametag) Shared.featureCpu.runMeasuredTask("processing PlayerRespawnEvent", CPUFeature.NAMETAGX_EVENT_RESPAWN, new Runnable() {
			public void run() {
				for (ArmorStand as : p.getArmorStands()) {
					as.updateLocation(e.getRespawnLocation());
					List<ITabPlayer> nearbyPlayers = as.getNearbyPlayers();
					synchronized (nearbyPlayers){
						for (ITabPlayer nearby : nearbyPlayers) {
							nearby.sendPacket(as.getTeleportPacket(nearby));
						}
					}
				}
			}
		});
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(PlayerTeleportEvent e) {
		ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p == null) return;
		if (!p.disabledNametag) Shared.featureCpu.runMeasuredTask("processing PlayerTeleportEvent", CPUFeature.NAMETAGX_EVENT_TELEPORT, new Runnable() {
			public void run() {
				synchronized (p.armorStands) {
					for (ArmorStand as : p.armorStands) {
						as.updateLocation(e.getTo());
						List<ITabPlayer> nearbyPlayers = as.getNearbyPlayers();
						synchronized (nearbyPlayers){
							for (ITabPlayer nearby : nearbyPlayers) {
								nearby.sendPacket(as.getTeleportPacket(nearby));
							}
						}
					}
				}
			}
		});
	}
	@Override
	public PacketPlayOutPlayerInfo onPacketSend(ITabPlayer receiver, PacketPlayOutPlayerInfo info) {
		if (!modifyNPCnames || receiver.getVersion().getMinorVersion() < 8 || info.action != EnumPlayerInfoAction.ADD_PLAYER) return info;
		for (PlayerInfoData playerInfoData : info.entries) {
			if (Shared.getPlayerByTablistUUID(playerInfoData.uniqueId) == null && playerInfoData.name.length() <= 15) {
				if (playerInfoData.name.length() <= 14) {
					playerInfoData.name += Placeholders.colorChar + "r";
				} else {
					playerInfoData.name += " ";
				}
			}
		}
		return info;
	}
	public void spawnArmorStand(ITabPlayer armorStandOwner, ITabPlayer viewer) {
		for (ArmorStand as : armorStandOwner.getArmorStands().toArray(new ArmorStand[0])) {
			for (Object packet : as.getSpawnPackets(viewer, true)) {
				viewer.sendPacket(packet);
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
		for (ArmorStand as : refreshed.armorStands.toArray(new ArmorStand[0])) {
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
}