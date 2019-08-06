package me.neznamy.tab.bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import me.neznamy.tab.bukkit.packets.DataWatcher.Item;
import me.neznamy.tab.bukkit.packets.NMSClass;
import me.neznamy.tab.bukkit.packets.PacketAPI;
import me.neznamy.tab.bukkit.packets.PacketPlayOut;
import me.neznamy.tab.bukkit.packets.PacketPlayOutAttachEntity_1_8_x;
import me.neznamy.tab.bukkit.packets.PacketPlayOutEntity.PacketPlayOutRelEntityMove;
import me.neznamy.tab.bukkit.packets.PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook;
import me.neznamy.tab.bukkit.packets.PacketPlayOutEntityDestroy;
import me.neznamy.tab.bukkit.packets.PacketPlayOutEntityMetadata;
import me.neznamy.tab.bukkit.packets.PacketPlayOutEntityTeleport;
import me.neznamy.tab.bukkit.packets.PacketPlayOutMount;
import me.neznamy.tab.bukkit.packets.PacketPlayOutNamedEntitySpawn;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;

public class NameTagX{

	public static boolean enable;
	public static int refresh;
	public static ConcurrentHashMap<Integer, List<Integer>> vehicles = new ConcurrentHashMap<Integer, List<Integer>>();

	public static void unload() {
		if (!enable) return;
		for (ITabPlayer p : Shared.getPlayers()) {
			p.unregisterTeam();
			NameTagLineManager.destroy(p);
		}
	}
	public static void load() {
		if (!enable) return;
		for (ITabPlayer all : Shared.getPlayers()){
			all.registerTeam();
			for (Player w : ((Player) all.getPlayer()).getWorld().getPlayers()) {
				ITabPlayer wPlayer = Shared.getPlayer(w.getUniqueId());
				if (wPlayer == null) {
					Shared.error("Data of " + w.getName() + " don't exist ?");
					continue;
				}
				if (all == wPlayer) continue;
				NameTagLineManager.spawnArmorStand(all, wPlayer, true);
			}
		}
		Shared.scheduleRepeatingTask(refresh, "refreshing nametags", new Runnable() {

			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) p.updateTeam();
			}
		});
		Shared.scheduleRepeatingTask(200, "refreshing nametag visibility", new Runnable() {

			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) NameTagLineManager.updateVisibility(p);
			}
		});
	}
	public static void playerJoin(ITabPlayer p) {
		if (!enable) return;
		p.registerTeam();
		for (ITabPlayer all : Shared.getPlayers()) all.registerTeam(p);
	}
	public static void playerQuit(ITabPlayer p) {
		if (enable) p.unregisterTeam();
	}
	public static void modifyPacketIN(Object packetplayinuseentity) throws Exception {
		int id = PacketAPI.PacketPlayInUseEntity_ENTITYID.getInt(packetplayinuseentity);
		for (ITabPlayer all : Shared.getPlayers()) {
			if (NameTagLineManager.isArmorStandID(all, id)) {
				PacketAPI.PacketPlayInUseEntity_ENTITYID.set(packetplayinuseentity, all.getEntityId());
			}
		}
	}
	public static void processPacketOUT(PacketPlayOut packet, ITabPlayer packetReceiver){
		try {
			boolean teleportPacket = packet instanceof PacketPlayOutEntityTeleport;
			if (packet instanceof PacketPlayOutRelEntityMove || packet instanceof PacketPlayOutRelEntityMoveLook || teleportPacket) {
				int id = 0;
				if (teleportPacket) id = ((PacketPlayOutEntityTeleport)packet).getEntityId();
				if (packet instanceof PacketPlayOutRelEntityMove) id = ((PacketPlayOutRelEntityMove)packet).getEntityId();
				if (packet instanceof PacketPlayOutRelEntityMoveLook) id = ((PacketPlayOutRelEntityMoveLook)packet).getEntityId();
				ITabPlayer pl = Shared.getPlayer(id);
				if (pl != null) {
					//player moved
					if (((Player) pl.getPlayer()).isFlying() && !teleportPacket) {
						//fixing a client-sided bug
						NameTagLineManager.teleportOwner(pl, packetReceiver);
					} else {
						NameTagLineManager.teleportArmorStand(pl, packetReceiver);
					}
				} else if (vehicles.containsKey(id)){
					//an animal carrying a player moved
					for (Integer entity : vehicles.get(id)) {
						ITabPlayer passenger = Shared.getPlayer(entity);
						if (passenger != null) NameTagLineManager.teleportArmorStand(passenger, packetReceiver);
					}
				}
			}
			if (packet instanceof PacketPlayOutNamedEntitySpawn) {
				ITabPlayer spawnedPlayer = Shared.getPlayer(((PacketPlayOutNamedEntitySpawn)packet).getEntityId());
				if (spawnedPlayer != null) NameTagLineManager.spawnArmorStand(spawnedPlayer, packetReceiver, true);
			}
			if (packet instanceof PacketPlayOutEntityDestroy) {
				int[] ids = ((PacketPlayOutEntityDestroy)packet).getEntities();
				for (int id : ids) {
					ITabPlayer despawnedPlayer = Shared.getPlayer(id);
					if (despawnedPlayer != null) NameTagLineManager.destroy(despawnedPlayer, packetReceiver);
				}
			}
			if (packet instanceof PacketPlayOutEntityMetadata) {
				List<Item> items = ((PacketPlayOutEntityMetadata)packet).getList();
				ITabPlayer metaPlayer = Shared.getPlayer(((PacketPlayOutEntityMetadata) packet).getEntityId());
				if (metaPlayer != null){
					for (Item i : items) {
						if (i.getType().getPosition() == 0) {
							byte properties = (Byte) i.getValue();
							boolean sneaking = (properties & 2) != 0;
							NameTagLineManager.sneak(metaPlayer, packetReceiver, sneaking);
						}
					}
				}
			}
			if (packet instanceof PacketPlayOutMount) {
				//1.9+ mount detection
				Integer vehicle = ((PacketPlayOutMount) packet).getVehicle();
				int[] passg = ((PacketPlayOutMount) packet).getPassengers();
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
			if (packet instanceof PacketPlayOutAttachEntity_1_8_x) {
				//1.8.x mount detection
				if (((PacketPlayOutAttachEntity_1_8_x) packet).getA() == 0) {
					int vehicle = ((PacketPlayOutAttachEntity_1_8_x) packet).getVehicle();
					int passenger = ((PacketPlayOutAttachEntity_1_8_x) packet).getPassenger();
					if (vehicle != -1) {
						//attach
						vehicles.put(vehicle, Lists.newArrayList(passenger));
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
		} catch (Exception e) {
			Shared.error("An error occured when processing packetOUT:", e);
		}
	}
	@SuppressWarnings("deprecation")
	public static List<Entity> getPassengers(Entity vehicle){
		List<Entity> passengers = new ArrayList<Entity>();
		if (NMSClass.versionNumber >= 11) {
			passengers = vehicle.getPassengers();
		} else {
			if (vehicle.getPassenger() != null) passengers.add(vehicle.getPassenger());
		}
		return passengers;
	}
}