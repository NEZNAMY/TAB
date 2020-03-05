package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import me.neznamy.tab.platforms.bukkit.Main;
import me.neznamy.tab.platforms.bukkit.TabPlayer;
import me.neznamy.tab.platforms.bukkit.features.unlimitedtags.NameTagXPacket.PacketType;
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
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class NameTagX implements Listener, SimpleFeature, RawPacketFeature, CustomPacketFeature{

	private int refresh;
	private Map<Integer, List<Integer>> vehicles = new ConcurrentHashMap<Integer, List<Integer>>();

	@Override
	public void load() {
		refresh = Configs.config.getInt("nametag-refresh-interval-milliseconds", 1000);
		Bukkit.getPluginManager().registerEvents(this, Main.instance);
		for (ITabPlayer all : Shared.getPlayers()){
			onJoin(all);
			for (ITabPlayer worldPlayer : Shared.getPlayers()) {
				if (all == worldPlayer) continue;
				if (!worldPlayer.getWorldName().equals(all.getWorldName())) continue;
				NameTagLineManager.spawnArmorStand(all, worldPlayer, true);
			}
		}
		Shared.cpu.startRepeatingMeasuredTask(refresh, "refreshing nametags", "Nametags", new Runnable() {
			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) p.updateTeam();
			}
		});
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() == 8 || PluginHooks.viaversion || PluginHooks.protocolsupport)
			Shared.cpu.startRepeatingMeasuredTask(200, "refreshing nametag visibility", "Nametags", new Runnable() {
				public void run() {
					for (ITabPlayer p : Shared.getPlayers()) NameTagLineManager.updateVisibility(p);
				}
			});
	}
	@Override
	public void unload() {
		HandlerList.unregisterAll(this);
		for (ITabPlayer p : Shared.getPlayers()) {
			p.unregisterTeam(false);
			NameTagLineManager.destroy(p);
			if (p.previewingNametag) NameTagLineManager.destroy(p, p);
		}
	}
	@Override
	public void onJoin(ITabPlayer p) {
		p.registerTeam();
		for (ITabPlayer all : Shared.getPlayers()) {
			if (all == p) continue; //already registered 2 lines above
			all.registerTeam(p);
		}
		Player player = ((TabPlayer)p).player;
		if (player.getVehicle() != null) {
			Entity vehicle = player.getVehicle();
			List<Integer> list = new ArrayList<Integer>();
			for (Entity e : getPassengers(vehicle)) {
				list.add(e.getEntityId());
			}
			vehicles.put(vehicle.getEntityId(), list);
		}
		((TabPlayer)p).loadArmorStands();
	}
	@Override
	public void onQuit(ITabPlayer p) {
		p.unregisterTeam(false);
		for (ITabPlayer all : Shared.getPlayers()) {
			NameTagLineManager.removeFromRegistered(all, p);
		}
		NameTagLineManager.destroy(p);
	}
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		p.restartArmorStands();
		if (p.disabledNametag && !p.isDisabledWorld(Configs.disabledNametag, from)) {
			p.unregisterTeam(true);
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
			if (pack.a != null && pack.a instanceof Integer) {
				packetPlayer = Shared.getPlayer((int)pack.a);
			}
			if (packetPlayer == null && pack.b != null && pack.b instanceof Integer) {
				packetPlayer = Shared.getPlayer((int)pack.b);
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
	@EventHandler(priority = EventPriority.MONITOR)
	public void a(PlayerToggleSneakEvent e) {
		Shared.cpu.runMeasuredTask("processing sneak toggle", "NameTagX - sneak event", new Runnable() {
			public void run() {
				NameTagLineManager.sneak(Shared.getPlayer(e.getPlayer().getUniqueId()), e.isSneaking());
			}
		});
	}
	@EventHandler
	public void a(PlayerMoveEvent e) {
		ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p.previewingNametag) Shared.cpu.runMeasuredTask("processing move", "NameTagX - move event", new Runnable() {
			public void run() {
				NameTagLineManager.teleportArmorStand(p, p);
			}
		});
	}
	public void processPacketOUT(NameTagXPacket packet, ITabPlayer packetReceiver){
		boolean teleportPacket = packet.getPacketType() == PacketType.ENTITY_TELEPORT;
		if (packet.getPacketType() == PacketType.ENTITY_MOVE || teleportPacket) {
			int id = (int) packet.a;
			ITabPlayer pl = Shared.getPlayer(id);
			List<Integer> vehicleList;
			if (pl != null) {
				//player moved
				if (((TabPlayer)pl).player.isFlying() && !teleportPacket) {
					//fixing a client-sided bug
					NameTagLineManager.teleportOwner(pl, packetReceiver);
				} else {
					NameTagLineManager.teleportArmorStand(pl, packetReceiver);
				}
			} else if ((vehicleList = vehicles.get(id)) != null){
				//a vehicle carrying something moved
				for (Integer entity : vehicleList) {
					ITabPlayer passenger = Shared.getPlayer(entity);
					if (passenger != null) {
						NameTagLineManager.teleportArmorStand(passenger, packetReceiver);
					}
				}
			}
		}
		if (packet.getPacketType() == PacketType.NAMED_ENTITY_SPAWN) {
			ITabPlayer spawnedPlayer = Shared.getPlayer((int)packet.a);
			if (spawnedPlayer != null) NameTagLineManager.spawnArmorStand(spawnedPlayer, packetReceiver, true);
		}
		if (packet.getPacketType() == PacketType.ENTITY_DESTROY) {
			for (int id : (int[])packet.a) {
				ITabPlayer despawnedPlayer = Shared.getPlayer(id);
				if (despawnedPlayer != null) NameTagLineManager.destroy(despawnedPlayer, packetReceiver);
			}
		}
		if (packet.getPacketType() == PacketType.MOUNT) {
			//1.9+ mount detection
			int vehicle = (int) packet.a;
			int[] passg = (int[]) packet.b;
			Integer[] passengers = new Integer[passg.length];
			for (int i=0; i<passg.length; i++) {
				passengers[i] = passg[i];
			}
			if (passengers.length == 0) {
				//detach
				vehicles.remove(vehicle);
			} else {
				//attach
				vehicles.put(vehicle, Arrays.asList(passengers));
			}
			for (int entity : passengers) {
				ITabPlayer pass = Shared.getPlayer(entity);
				if (pass != null) NameTagLineManager.teleportArmorStand(pass, packetReceiver);
			}
		}
		if (packet.getPacketType() == PacketType.ATTACH_ENTITY) {
			//1.8.x mount detection
			if ((int)packet.a == 0) {
				int vehicle = (int) packet.c;
				int passenger = (int) packet.b;
				if (vehicle != -1) {
					//attach
					vehicles.put(vehicle, Arrays.asList(passenger));
				} else {
					//detach
					for (Entry<Integer, List<Integer>> entry : vehicles.entrySet()) {
						if (entry.getValue().contains(passenger)) {
							vehicles.remove(entry.getKey());
						}
					}
				}
				ITabPlayer pass = Shared.getPlayer(passenger);
				if (pass != null) NameTagLineManager.teleportArmorStand(pass, packetReceiver);
			}
		}
	}
	@SuppressWarnings("deprecation")
	public List<Entity> getPassengers(Entity vehicle){
		List<Entity> passengers = new ArrayList<Entity>();
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 11) {
			passengers = vehicle.getPassengers();
		} else {
			if (vehicle.getPassenger() != null) passengers.add(vehicle.getPassenger());
		}
		return passengers;
	}
	@Override
	public UniversalPacketPlayOut onPacketSend(ITabPlayer receiver, UniversalPacketPlayOut packet) {
		if (!(packet instanceof PacketPlayOutPlayerInfo)) return packet;
		if (receiver.getVersion().getMinorVersion() < 8) return packet;
		PacketPlayOutPlayerInfo info = (PacketPlayOutPlayerInfo) packet;
		if (info.action == EnumPlayerInfoAction.ADD_PLAYER) {
			for (PlayerInfoData playerInfoData : info.players) {
				ITabPlayer packetPlayer = Shared.getPlayerByTablistUUID(playerInfoData.uniqueId);
				if (packetPlayer == null && Configs.modifyNPCnames) {
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