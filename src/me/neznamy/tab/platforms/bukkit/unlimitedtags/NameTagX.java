package me.neznamy.tab.platforms.bukkit.unlimitedtags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import me.neznamy.tab.platforms.bukkit.Main;
import me.neznamy.tab.platforms.bukkit.TabPlayer;
import me.neznamy.tab.platforms.bukkit.unlimitedtags.NameTagXPacket.PacketType;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.Shared.Feature;

public class NameTagX implements Listener{

	public static boolean enable;
	public static int refresh;
	public static ConcurrentHashMap<Integer, List<Integer>> vehicles = new ConcurrentHashMap<Integer, List<Integer>>();
	private static boolean EVENTS_REGISTERED = false;

	public static void unload() {
		if (!enable) return;
		for (ITabPlayer p : Shared.getPlayers()) {
			p.unregisterTeam(false);
			NameTagLineManager.destroy(p);
			if (p.previewingNametag) NameTagLineManager.destroy(p, p);
		}
	}
	public static void load(){
		if (!enable) return;
		if (!EVENTS_REGISTERED) {
			EVENTS_REGISTERED = true;
			Bukkit.getPluginManager().registerEvents(new NameTagX(), Main.instance);
		}
		for (ITabPlayer all : Shared.getPlayers()){
			all.registerTeam();
			for (Player w : (((TabPlayer)all).player).getWorld().getPlayers()) {
				ITabPlayer wPlayer = Shared.getPlayer(w.getUniqueId());
				if (wPlayer == null) {
					Shared.error(null, "Data of " + w.getName() + " don't exist ?");
					continue;
				}
				if (all == wPlayer) continue;
				NameTagLineManager.spawnArmorStand(all, wPlayer, true);
			}
		}
		Shared.scheduleRepeatingTask(refresh, "refreshing nametags", Feature.NAMETAG, new Runnable() {
			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) p.updateTeam();
			}
		});
		Shared.scheduleRepeatingTask(200, "refreshing nametag visibility", Feature.NAMETAGX, new Runnable() {
			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) NameTagLineManager.updateVisibility(p);
			}
		});
	}
	public static void playerJoin(ITabPlayer p) {
		if (!enable) return;
		p.registerTeam();
		for (ITabPlayer all : Shared.getPlayers()) {
			if (all == p) continue; //already registered 2 lines above
			all.registerTeam(p);
		}
	}
	public static void playerQuit(ITabPlayer p) {
		if (enable) p.unregisterTeam(false);
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void a(PlayerToggleSneakEvent e) {
		if (Shared.disabled || !enable) return;
		ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p == null) {
			Shared.error(null, "Data of " + e.getPlayer().getName() + " did not exist when player sneaked");
			return;
		}
		Shared.runTask("processing sneak toggle", Feature.NAMETAGX, new Runnable() {
			public void run() {
				NameTagLineManager.sneak(p, e.isSneaking());
			}
		});
	}
	@EventHandler
	public void a(PlayerMoveEvent e) {
		if (Shared.disabled || !enable) return;
		ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p == null) {
			Shared.error(null, "Data of " + e.getPlayer().getName() + " did not exist when player moved");
			return;
		}
		if (p.previewingNametag) Shared.runTask("processing move", Feature.NAMETAGX, new Runnable() {

			public void run() {
				NameTagLineManager.teleportArmorStand(p, p);
			}
		});
	}
	public static void processPacketOUT(NameTagXPacket packet, ITabPlayer packetReceiver){
		boolean teleportPacket = packet.getPacketType() == PacketType.ENTITY_TELEPORT;
		if (packet.getPacketType() == PacketType.ENTITY_MOVE || teleportPacket) {
			int id = (int) packet.a;
			ITabPlayer pl = Shared.getPlayer(id);
			if (pl != null) {
				//player moved
				if (((TabPlayer)pl).player.isFlying() && !teleportPacket) {
					//fixing a client-sided bug
					NameTagLineManager.teleportOwner(pl, packetReceiver);
				} else {
					NameTagLineManager.teleportArmorStand(pl, packetReceiver);
				}
			} else if (vehicles.containsKey(id)){
				//a vehicle carrying something moved
				for (Integer entity : vehicles.get(id)) {
					ITabPlayer passenger = Shared.getPlayer(entity);
					if (passenger != null) {
						NameTagLineManager.teleportArmorStand(passenger, packetReceiver);

						//activating this code will fix desync on boats
						//however, boat movement will be extremely laggy
						//seems to only work for 1.8.x servers idk why
/*							if (((Player)passenger.getPlayer()).getVehicle() != null){ //bukkit api bug
								if (packetReceiver == passenger) continue;
								if (packet.getPacketType() == PacketType.ENTITY_TELEPORT) continue;
								new PacketPlayOutEntityTeleport(((Player)passenger.getPlayer()).getVehicle()).send(packetReceiver);
							}*/
					}
				}
			}
		}
		if (packet.getPacketType() == PacketType.NAMED_ENTITY_SPAWN) {
			ITabPlayer spawnedPlayer = Shared.getPlayer((int)packet.a);
//			if (spawnedPlayer != null && !spawnedPlayer.disabledNametag && !packetReceiver.disabledNametag) NameTagLineManager.spawnArmorStand(spawnedPlayer, packetReceiver, true);			
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
	public static List<Entity> getPassengers(Entity vehicle){
		List<Entity> passengers = new ArrayList<Entity>();
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 11) {
			passengers = vehicle.getPassengers();
		} else {
			if (vehicle.getPassenger() != null) passengers.add(vehicle.getPassenger());
		}
		return passengers;
	}
}