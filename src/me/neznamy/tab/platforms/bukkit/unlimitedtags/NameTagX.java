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

import com.google.common.collect.Lists;

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

	public static void unload() {
		if (!enable) return;
		for (ITabPlayer p : Shared.getPlayers()) {
			p.unregisterTeam();
			NameTagLineManager.destroy(p);
		}
	}
	public static void load(){
		if (!enable) return;
		Bukkit.getPluginManager().registerEvents(new NameTagX(), Main.instance);
		for (ITabPlayer all : Shared.getPlayers()){
			all.registerTeam();
			for (Player w : (((TabPlayer)all).player).getWorld().getPlayers()) {
				ITabPlayer wPlayer = Shared.getPlayer(w.getUniqueId());
				if (wPlayer == null) {
					Shared.error("Data of " + w.getName() + " don't exist ?");
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
		if (enable) p.unregisterTeam();
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void a(final PlayerToggleSneakEvent e) {
		final ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p == null) {
			Shared.error("Data of " + e.getPlayer().getName() + " did not exist when player sneaked");
			return;
		}
		Shared.runTask("processing sprint toggle", Feature.NAMETAGX, new Runnable() {
			public void run() {
				NameTagLineManager.sneak(p, e.isSneaking());
			}
		});
	}
	@EventHandler
	public void a(PlayerMoveEvent e) {
		final ITabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (p.previewingNametag) Shared.runTask("processing move", Feature.NAMETAGX, new Runnable() {

			public void run() {
				NameTagLineManager.teleportArmorStand(p, p);
			}
		});
	}
	public static void processPacketOUT(NameTagXPacket packet, ITabPlayer packetReceiver){
		try {
			boolean teleportPacket = packet.getPacketType() == PacketType.ENTITY_TELEPORT;
			if (packet.getPacketType() == PacketType.ENTITY_MOVE || teleportPacket) {
				int id = packet.getEntityId();
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
				ITabPlayer spawnedPlayer = Shared.getPlayer(packet.getEntityId());
				if (spawnedPlayer != null) NameTagLineManager.spawnArmorStand(spawnedPlayer, packetReceiver, true);
			}
			if (packet.getPacketType() == PacketType.ENTITY_DESTROY) {
				int[] ids = packet.getEntityArray();
				for (int id : ids) {
					ITabPlayer despawnedPlayer = Shared.getPlayer(id);
					if (despawnedPlayer != null) NameTagLineManager.destroy(despawnedPlayer, packetReceiver);
				}
			}
			if (packet.getPacketType() == PacketType.MOUNT) {
				//1.9+ mount detection
				int vehicle = packet.getEntityId();
				int[] passg = packet.getEntityArray();
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
				if (packet.getExtra() == 0) {
					int vehicle = packet.getEntityId();
					int passenger = packet.getEntityArray()[0];
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
		} catch (Throwable e) {
			Shared.error("An error occured when processing packetOUT:", e);
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