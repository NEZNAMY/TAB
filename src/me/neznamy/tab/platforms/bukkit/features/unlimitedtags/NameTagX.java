package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

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
import me.neznamy.tab.platforms.bukkit.features.unlimitedtags.NameTagXPacket.PacketType;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
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
				NameTagLineManager.spawnArmorStand(all, worldPlayer, true);
			}
		}
		Shared.cpu.startRepeatingMeasuredTask(refresh, "refreshing nametags", "NameTags", new Runnable() {
			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) p.updateTeam();
			}
		});
		Shared.cpu.startRepeatingMeasuredTask(200, "refreshing nametag visibility", "NameTags", new Runnable() {
			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) {
					if (p.disabledNametag) continue;
					p.getArmorStands().forEach(a -> a.updateVisibility());
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
	}
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		((TabPlayer)p).restartArmorStands();
		if (p.disabledNametag && !p.isDisabledWorld(Configs.disabledNametag, from)) {
			p.unregisterTeam();
		} else if (!p.disabledNametag && p.isDisabledWorld(Configs.disabledNametag, from)) {
			p.registerTeam();
		} else {
			p.updateTeam();
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
				MethodAPI.PacketPlayInUseEntity_ENTITY.set(packet, ((TabPlayer)attacked).player.getEntityId());
			}
		}
		return packet;
	}
	@Override
	public Object onPacketSend(ITabPlayer receiver, Object packet) throws Throwable {
		NameTagXPacket pack = NameTagXPacket.fromNMS(packet);
		if (pack != null) {
			ITabPlayer packetPlayer = null;
			if (pack.entity instanceof Integer) {
				packetPlayer = Shared.entityIdMap.get((int)pack.entity);
			}
			if (packetPlayer == null || !packetPlayer.disabledNametag) {
				//sending packets outside of the packet reader or protocollib will cause problems
				Shared.cpu.runMeasuredTask("processing packet out", "NameTagX - processing", new Runnable() {
					public void run() {
						processPacketOUT(pack, receiver);
					}
				});
			}
		}
		return packet;
	}
	@Override
	public String getCPUName() {
		return "NameTagX - reading";
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(PlayerToggleSneakEvent e) {
		ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p == null) return;
		Shared.cpu.runMeasuredTask("processing PlayerToggleSneakEvent", "NameTagX - PlayerToggleSneakEvent", new Runnable() {
			public void run() {
				p.getArmorStands().forEach(a -> a.sneak(e.isSneaking()));
			}
		});
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(PlayerMoveEvent e) {
		ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p == null) return;
		Shared.cpu.runMeasuredTask("processing PlayerMoveEvent", "NameTagX - PlayerMoveEvent", new Runnable() {
			public void run() {
				NameTagLineManager.teleportArmorStand(p, e.getTo());
			}
		});
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void a(PlayerTeleportEvent e) {
		ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p == null) return;
		Shared.cpu.runMeasuredTask("processing PlayerTeleportEvent", "NameTagX - PlayerTeleportEvent", new Runnable() {
			public void run() {
				NameTagLineManager.teleportArmorStand(p, e.getTo());
			}
		});
	}
	public void processPacketOUT(NameTagXPacket packet, ITabPlayer packetReceiver){
		if (packet.getPacketType() == PacketType.NAMED_ENTITY_SPAWN) {
			ITabPlayer spawnedPlayer = Shared.entityIdMap.get((int)packet.entity);
			if (spawnedPlayer != null) NameTagLineManager.spawnArmorStand(spawnedPlayer, packetReceiver, true);
		}
		if (packet.getPacketType() == PacketType.ENTITY_DESTROY) {
			for (int id : (int[])packet.entity) {
				ITabPlayer despawnedPlayer = Shared.entityIdMap.get(id);
				if (despawnedPlayer != null) despawnedPlayer.getArmorStands().forEach(a -> a.destroy(packetReceiver));
			}
		}
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
}