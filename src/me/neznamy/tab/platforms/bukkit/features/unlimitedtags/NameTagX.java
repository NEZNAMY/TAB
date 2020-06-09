package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

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
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.interfaces.CustomPacketFeature;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.QuitEventListener;
import me.neznamy.tab.shared.features.interfaces.RawPacketFeature;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class NameTagX implements Listener, Loadable, JoinEventListener, QuitEventListener, WorldChangeListener, RawPacketFeature, CustomPacketFeature{

	@Override
	public void load() {
		int refresh = Configs.config.getInt("nametag-refresh-interval-milliseconds", 1000);
		if (refresh < 50) Shared.errorManager.refreshTooLow("NameTags", refresh);
		Bukkit.getPluginManager().registerEvents(this, Main.instance);
		for (ITabPlayer all : Shared.getPlayers()){
			onJoin(all);
			if (all.disabledNametag) continue;
			for (ITabPlayer worldPlayer : Shared.getPlayers()) {
				if (all == worldPlayer) continue;
				if (!worldPlayer.getWorldName().equals(all.getWorldName())) continue;
				spawnArmorStand(all, worldPlayer);
			}
		}
		Shared.featureCpu.startRepeatingMeasuredTask(refresh, "refreshing nametags", CPUFeature.NAMETAG, new Runnable() {
			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) {
					p.updateTeam(false);
					synchronized(p.armorStands) {
						p.armorStands.forEach(a -> a.refreshName());
						fixArmorStandHeights(p);
					}
				}
			}
		});
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
		if (connectedPlayer.disabledNametag) return;
		connectedPlayer.registerTeam();
		for (ITabPlayer all : Shared.getPlayers()) {
			if (all == connectedPlayer) continue; //already registered 2 lines above
			if (!all.disabledNametag) all.registerTeam(connectedPlayer);
		}
		loadArmorStands(connectedPlayer);
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
			p.updateTeam(true);
			List<ArmorStand> list = new ArrayList<ArmorStand>();
			list.addAll(p.armorStands);
			list.forEach(a -> a.refreshName());
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
			NameTagX.spawnArmorStand(p, worldPlayer);
		}
		if (p.previewingNametag) NameTagX.spawnArmorStand(p, p);
	}
	public void loadArmorStands(ITabPlayer pl) {
		pl.armorStands.clear();
		pl.setProperty("nametag", pl.properties.get("tagprefix").getCurrentRawValue() + pl.properties.get("customtagname").getCurrentRawValue() + pl.properties.get("tagsuffix").getCurrentRawValue(), null);
		double height = -Configs.SECRET_NTX_space;
		for (String line : Premium.dynamicLines) {
			Property p = pl.properties.get(line);
			pl.armorStands.add(new ArmorStand(pl, p, height+=Configs.SECRET_NTX_space, false));
		}
		for (Entry<String, Double> line : Premium.staticLines.entrySet()) {
			Property p = pl.properties.get(line.getKey());
			pl.armorStands.add(new ArmorStand(pl, p, line.getValue(), true));
		}
		fixArmorStandHeights(pl);
	}
	public void fixArmorStandHeights(ITabPlayer p) {
		p.armorStands.forEach(a -> a.refreshName());
		double currentY = -Configs.SECRET_NTX_space;
		for (ArmorStand as : p.getArmorStands()) {
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
	public UniversalPacketPlayOut onPacketSend(ITabPlayer receiver, UniversalPacketPlayOut packet) {
		if (!Configs.modifyNPCnames) return packet;
		if (!(packet instanceof PacketPlayOutPlayerInfo)) return packet;
		if (receiver.getVersion().getMinorVersion() < 8) return packet;
		PacketPlayOutPlayerInfo info = (PacketPlayOutPlayerInfo) packet;
		if (info.action == EnumPlayerInfoAction.ADD_PLAYER) {
			for (PlayerInfoData playerInfoData : info.entries) {
				if (Shared.getPlayerByTablistUUID(playerInfoData.uniqueId) == null) {
					if (playerInfoData.name.length() <= 15) {
						if (playerInfoData.name.length() <= 14) {
							playerInfoData.name += Placeholders.colorChar + "r";
						} else {
							playerInfoData.name += " ";
						}
					}
				}
			}
		}
		return info;
	}
	public static void spawnArmorStand(ITabPlayer armorStandOwner, ITabPlayer viewer) {
		for (ArmorStand as : armorStandOwner.getArmorStands().toArray(new ArmorStand[0])) {
			for (Object packet : as.getSpawnPackets(viewer, true)) {
				viewer.sendPacket(packet);
			}
		}
	}
}