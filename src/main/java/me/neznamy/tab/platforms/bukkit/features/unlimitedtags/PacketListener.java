package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOut;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.features.interfaces.PlayerInfoPacketListener;
import me.neznamy.tab.shared.features.interfaces.RawPacketFeature;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;

/**
 * The packet listening part for securing proper functionality of armor stands
 * Bukkit events are too unreliable and delayed/ahead which causes desync
 */
public class PacketListener implements RawPacketFeature, PlayerInfoPacketListener {

	private static Class<?> PacketPlayOutEntityDestroy;
	private static Field PacketPlayOutEntityDestroy_ENTITIES;

	private static Class<?> PacketPlayInUseEntity;
	private static Field PacketPlayInUseEntity_ENTITY;

	private static Class<?> PacketPlayOutNamedEntitySpawn;
	private static Field PacketPlayOutNamedEntitySpawn_ENTITYID;

	private static Class<?> PacketPlayOutEntity;
	private static Field PacketPlayOutEntity_ENTITYID;

	public static Class<?> PacketPlayOutMount;
	private static Field PacketPlayOutMount_VEHICLE;
	private static Field PacketPlayOutMount_PASSENGERS;

	private static Class<?> PacketPlayOutAttachEntity;
	private static Field PacketPlayOutAttachEntity_A;
	private static Field PacketPlayOutAttachEntity_PASSENGER;
	private static Field PacketPlayOutAttachEntity_VEHICLE;
	
	private static Class<?> PacketPlayOutEntityTeleport;
	private static Field PacketPlayOutEntityTeleport_ENTITYID;

	private boolean modifyNPCnames;
	private NameTagX nameTagX;

	/**
	 * Initializes required NMS classes and fields
	 * @throws Exception - if something fails
	 */
	public static void initializeClass() throws Exception {
		PacketPlayInUseEntity = PacketPlayOut.getNMSClass("PacketPlayInUseEntity");
		PacketPlayOutEntity = PacketPlayOut.getNMSClass("PacketPlayOutEntity");
		PacketPlayOutEntityDestroy = PacketPlayOut.getNMSClass("PacketPlayOutEntityDestroy");
		PacketPlayOutNamedEntitySpawn = PacketPlayOut.getNMSClass("PacketPlayOutNamedEntitySpawn");
		PacketPlayOutEntityTeleport = PacketPlayOut.getNMSClass("PacketPlayOutEntityTeleport");
		(PacketPlayOutEntityTeleport_ENTITYID = PacketPlayOutEntityTeleport.getDeclaredField("a")).setAccessible(true);

		(PacketPlayInUseEntity_ENTITY = PacketPlayInUseEntity.getDeclaredField("a")).setAccessible(true);
		(PacketPlayOutEntity_ENTITYID = PacketPlayOutEntity.getDeclaredField("a")).setAccessible(true);
		(PacketPlayOutEntityDestroy_ENTITIES = PacketPlayOutEntityDestroy.getDeclaredField("a")).setAccessible(true);
		(PacketPlayOutNamedEntitySpawn_ENTITYID = PacketPlayOutNamedEntitySpawn.getDeclaredField("a")).setAccessible(true);

		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			PacketPlayOutMount = PacketPlayOut.getNMSClass("PacketPlayOutMount");
			(PacketPlayOutMount_VEHICLE = PacketPlayOutMount.getDeclaredField("a")).setAccessible(true);
			(PacketPlayOutMount_PASSENGERS = PacketPlayOutMount.getDeclaredField("b")).setAccessible(true);
		} else {
			PacketPlayOutAttachEntity = PacketPlayOut.getNMSClass("PacketPlayOutAttachEntity");
			(PacketPlayOutAttachEntity_A = PacketPlayOutAttachEntity.getDeclaredField("a")).setAccessible(true);
			(PacketPlayOutAttachEntity_PASSENGER = PacketPlayOutAttachEntity.getDeclaredField("b")).setAccessible(true);
			(PacketPlayOutAttachEntity_VEHICLE = PacketPlayOutAttachEntity.getDeclaredField("c")).setAccessible(true);
		}
	}

	public PacketListener(NameTagX nameTagX) {
		modifyNPCnames = Configs.config.getBoolean("unlimited-nametag-prefix-suffix-mode.modify-npc-names", false);
		this.nameTagX = nameTagX;
	}

	@Override
	public Object onPacketReceive(TabPlayer sender, Object packet) throws Throwable {
		if (sender.getVersion().getMinorVersion() == 8 && PacketPlayInUseEntity.isInstance(packet)) {
			int entityId = PacketPlayInUseEntity_ENTITY.getInt(packet);
			TabPlayer attacked = null;
			for (TabPlayer all : Shared.getPlayers()) {
				if (all.getArmorStandManager().hasArmorStandWithID(entityId)) {
					attacked = all;
					break;
				}
			}
			if (attacked != null && attacked != sender) {
				PacketPlayInUseEntity_ENTITY.set(packet, ((Player) attacked.getPlayer()).getEntityId());
			}
		}
		return packet;
	}

	@Override
	public void onPacketSend(TabPlayer receiver, Object packet) throws Throwable {
		if (receiver.getVersion().getMinorVersion() < 8) return;
		if (PacketPlayOutEntity.isInstance(packet)) {
			onEntityMove(receiver, PacketPlayOutEntity_ENTITYID.getInt(packet));
		}
		if (PacketPlayOutEntityTeleport.isInstance(packet)) {
			onEntityMove(receiver, PacketPlayOutEntityTeleport_ENTITYID.getInt(packet));
		}
		if (PacketPlayOutNamedEntitySpawn.isInstance(packet)) {
			onEntitySpawn(receiver, PacketPlayOutNamedEntitySpawn_ENTITYID.getInt(packet));
		}
		if (PacketPlayOutEntityDestroy.isInstance(packet)) {
			onEntityDestroy(receiver, (int[]) PacketPlayOutEntityDestroy_ENTITIES.get(packet));
		}
		if (PacketPlayOutMount != null && PacketPlayOutMount.isInstance(packet)) {
			//1.9+ mount detection
			onMount(receiver, PacketPlayOutMount_VEHICLE.getInt(packet), (int[]) PacketPlayOutMount_PASSENGERS.get(packet));
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() == 8 && PacketPlayOutAttachEntity.isInstance(packet) && PacketPlayOutAttachEntity_A.getInt(packet) == 0) {
			//1.8.x mount detection
			onAttach(receiver, PacketPlayOutAttachEntity_VEHICLE.getInt(packet), PacketPlayOutAttachEntity_PASSENGER.getInt(packet));
		}
	}

	public void onEntityMove(TabPlayer receiver, int entityId) {
		TabPlayer pl = nameTagX.entityIdMap.get(entityId);
		Set<Integer> vehicleList;
		if (pl != null) {
			//player moved
			if (!pl.isLoaded()) return;
			Shared.cpu.runMeasuredTask("processing EntityMove", getFeatureType(), UsageType.PACKET_ENTITY_MOVE, () -> pl.getArmorStandManager().teleport(receiver));
			
			//player can be a vehicle too
			List<Entity> riders = nameTagX.getPassengers((Player) pl.getPlayer());
			for (Entity e : riders) {
				TabPlayer rider = nameTagX.entityIdMap.get(e.getEntityId());
				if (rider != null) {
					Shared.cpu.runMeasuredTask("processing EntityMove", getFeatureType(), UsageType.PACKET_ENTITY_MOVE, () -> {
						rider.getArmorStandManager().teleport(receiver);
						rider.getArmorStandManager().teleport(pl); //vehicle player has no other way to get this packet
					});
				}
			}
		} else if ((vehicleList = nameTagX.vehicles.get(entityId)) != null){
			//a vehicle carrying something moved
			for (Integer entity : vehicleList) {
				TabPlayer passenger = nameTagX.entityIdMap.get(entity);
				if (passenger != null) {
					Shared.cpu.runMeasuredTask("processing EntityMove", getFeatureType(), UsageType.PACKET_ENTITY_MOVE, () -> passenger.getArmorStandManager().teleport(receiver));
				}
			}
		}
	}

	public void onEntitySpawn(TabPlayer receiver, int entityId) {
		TabPlayer spawnedPlayer = nameTagX.entityIdMap.get(entityId);
		if (spawnedPlayer != null && !nameTagX.isDisabledWorld(spawnedPlayer.getWorldName()) && spawnedPlayer.isLoaded()) 
			Shared.cpu.runMeasuredTask("processing NamedEntitySpawn", getFeatureType(), UsageType.PACKET_NAMED_ENTITY_SPAWN, () -> spawnedPlayer.getArmorStandManager().spawn(receiver));
	}

	public void onEntityDestroy(TabPlayer receiver, int[] entities) {
		for (int id : entities) {
			TabPlayer despawnedPlayer = nameTagX.entityIdMap.get(id);
			if (despawnedPlayer != null && despawnedPlayer.isLoaded()) 
				Shared.cpu.runMeasuredTask("processing EntityDestroy", getFeatureType(), UsageType.PACKET_ENTITY_DESTROY, () -> despawnedPlayer.getArmorStandManager().destroy(receiver));
		}
	}

	public void onMount(TabPlayer receiver, int vehicle, int[] passengers) {
		if (passengers.length == 0) {
			//detach
			nameTagX.vehicles.remove(vehicle);
		} else {
			//attach
			nameTagX.vehicles.put(vehicle, Arrays.stream(passengers).boxed().collect(Collectors.toSet()));
		}
		for (int entity : passengers) {
			TabPlayer pass = nameTagX.entityIdMap.get(entity);
			if (pass != null && pass.isLoaded()) Shared.cpu.runMeasuredTask("processing Mount", getFeatureType(), UsageType.PACKET_MOUNT, () -> pass.getArmorStandManager().teleport(receiver));
		}
	}

	public void onAttach(TabPlayer receiver, int vehicle, int passenger) {
		if (vehicle != -1) {
			//attach
			nameTagX.vehicles.put(vehicle, Sets.newHashSet(passenger));
		} else {
			//detach
			for (Entry<Integer, Set<Integer>> entry : nameTagX.vehicles.entrySet()) {
				if (entry.getValue().contains(passenger)) {
					nameTagX.vehicles.remove(entry.getKey());
				}
			}
		}
		TabPlayer pass = nameTagX.entityIdMap.get(passenger);
		if (pass != null && pass.isLoaded()) Shared.cpu.runMeasuredTask("processing Mount", getFeatureType(), UsageType.PACKET_MOUNT, () -> pass.getArmorStandManager().teleport(receiver));
	}
	
	@Override
	public void onPacketSend(TabPlayer receiver, PacketPlayOutPlayerInfo info) {
		if (!modifyNPCnames || info.action != EnumPlayerInfoAction.ADD_PLAYER) return;
		for (PlayerInfoData playerInfoData : info.entries) {
			if (Shared.getPlayerByTablistUUID(playerInfoData.uniqueId) == null && playerInfoData.name.length() <= 15) {
				if (playerInfoData.name.length() <= 14) {
					playerInfoData.name += PlaceholderManager.colorChar + "r";
				} else {
					playerInfoData.name += " ";
				}
			}
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
}