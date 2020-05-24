package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import me.neznamy.tab.platforms.bukkit.Main;
import me.neznamy.tab.platforms.bukkit.TabPlayer;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PluginHooks;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.features.CustomPacketFeature;
import me.neznamy.tab.shared.features.RawPacketFeature;
import me.neznamy.tab.shared.features.SimpleFeature;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class NameTagX implements Listener, SimpleFeature, RawPacketFeature, CustomPacketFeature{

	private int refresh;

	@Override
	public void load() {
		refresh = Configs.config.getInt("nametag-refresh-interval-milliseconds", 1000);
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
		Shared.featureCpu.startRepeatingMeasuredTask(refresh, "refreshing nametags", "NameTags", new Runnable() {
			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) p.updateTeam(false);
			}
		});
		Shared.featureCpu.startRepeatingMeasuredTask(200, "refreshing nametag visibility", "NameTags", new Runnable() {
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
		((TabPlayer)connectedPlayer).loadArmorStands();
	}
	@Override
	public void onQuit(ITabPlayer disconnectedPlayer) {
		if (!disconnectedPlayer.disabledNametag) disconnectedPlayer.unregisterTeam();
		for (ITabPlayer all : Shared.getPlayers()) {
			all.getArmorStands().forEach(a -> a.removeFromRegistered(disconnectedPlayer));
		}
		disconnectedPlayer.getArmorStands().forEach(a -> a.destroy());
		int asCount = disconnectedPlayer.getArmorStands().size();
		int[] armorStandIds = new int[asCount];
		for (int i=0; i<asCount; i++) {
			armorStandIds[i] = disconnectedPlayer.getArmorStands().get(i).getEntityId();
		}
		Shared.featureCpu.runMeasuredTask("Processing player quit", "NameTagX - onQuit", new Runnable() {

			@Override
			public void run() {
				try {
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
		((TabPlayer)p).restartArmorStands();
		if (p.disabledNametag && !p.isDisabledWorld(Configs.disabledNametag, from)) {
			p.unregisterTeam();
		} else if (!p.disabledNametag && p.isDisabledWorld(Configs.disabledNametag, from)) {
			p.registerTeam();
		} else {
			p.updateTeam(true);
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
			if (spawnedPlayer != null && !spawnedPlayer.disabledNametag) Shared.featureCpu.runMeasuredTask("processing NamedEntitySpawn", "NameTagX - NamedEntitySpawn", new Runnable() {
				public void run() {
					spawnArmorStand(spawnedPlayer, receiver);
				}
			});
		}
		if (MethodAPI.PacketPlayOutEntityDestroy.isInstance(packet)) {
			int[] entites = (int[]) MethodAPI.PacketPlayOutEntityDestroy_ENTITIES.get(packet);
			for (int id : entites) {
				ITabPlayer despawnedPlayer = Shared.entityIdMap.get(id);
				if (despawnedPlayer != null && !despawnedPlayer.disabledNametag) Shared.featureCpu.runMeasuredTask("processing EntityDestroy", "NameTagX - EntityDestroy", new Runnable() {
					public void run() {
						despawnedPlayer.getArmorStands().forEach(a -> a.destroy(receiver));
					}
				});
			}
		}
		return packet;
	}
	@Override
	public String getCPUName() {
		return "NameTagX - Packet Listening";
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(PlayerToggleSneakEvent e) {
		ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p == null) return;
		if (!p.disabledNametag) Shared.featureCpu.runMeasuredTask("processing PlayerToggleSneakEvent", "NameTagX - PlayerToggleSneakEvent", new Runnable() {
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
		if (!p.disabledNametag) Shared.featureCpu.runMeasuredTask("processing PlayerMoveEvent", "NameTagX - PlayerMoveEvent", new Runnable() {
			public void run() {
				for (ArmorStand as : p.getArmorStands()) {
					as.updateLocation(e.getTo());
					List<ITabPlayer> nearbyPlayers = as.getNearbyPlayers();
					synchronized (nearbyPlayers){
						for (ITabPlayer nearby : nearbyPlayers) {
							nearby.sendPacket(as.getNMSTeleportPacket(nearby));
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
		if (!p.disabledNametag) Shared.featureCpu.runMeasuredTask("processing PlayerTeleportEvent", "NameTagX - PlayerTeleportEvent", new Runnable() {
			public void run() {
				synchronized (p.armorStands) {
					for (ArmorStand as : p.armorStands) {
						as.updateLocation(e.getTo());
						List<ITabPlayer> nearbyPlayers = as.getNearbyPlayers();
						synchronized (nearbyPlayers){
							for (ITabPlayer nearby : nearbyPlayers) {
								nearby.sendPacket(as.getNMSTeleportPacket(nearby));
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
			viewer.sendCustomBukkitPacket(as.getSpawnPacket(viewer, true));
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 15) {
				String displayName = as.property.hasRelationalPlaceholders() ? PluginHooks.PlaceholderAPI_setRelationalPlaceholders(viewer, armorStandOwner, as.property.get()) : as.property.get();
				viewer.sendPacket(MethodAPI.getInstance().newPacketPlayOutEntityMetadata(as.getEntityId(), as.createDataWatcher(displayName, viewer).toNMS(), true));
			}
		}
	}
}