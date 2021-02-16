package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.google.common.collect.Sets;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.types.packet.PlayerInfoPacketListener;
import me.neznamy.tab.shared.features.types.packet.RawPacketListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;

/**
 * The packet listening part for securing proper functionality of armor stands
 * Bukkit events are too unreliable and delayed/ahead which causes desync
 */
public class PacketListener implements RawPacketListener, PlayerInfoPacketListener {

	private NameTagX nameTagX;
	private NMSStorage nms;
	private TAB tab;
	private boolean modifyNPCnames;

	public PacketListener(NameTagX nameTagX, NMSStorage nms, TAB tab) {
		this.nameTagX = nameTagX;
		this.nms = nms;
		this.tab = tab;
		modifyNPCnames = tab.getConfiguration().config.getBoolean("unlimited-nametag-prefix-suffix-mode.modify-npc-names", false);
	}

	@Override
	public Object onPacketReceive(TabPlayer sender, Object packet) throws Throwable {
		if (sender.getVersion().getMinorVersion() == 8 && nms.PacketPlayInUseEntity.isInstance(packet)) {
			int entityId = nms.PacketPlayInUseEntity_ENTITY.getInt(packet);
			TabPlayer attacked = null;
			for (TabPlayer all : tab.getPlayers()) {
				if (!all.isLoaded()) continue;
				if (all.getArmorStandManager().hasArmorStandWithID(entityId)) {
					attacked = all;
					break;
				}
			}
			if (attacked != null && attacked != sender) {
				nms.PacketPlayInUseEntity_ENTITY.set(packet, ((Player) attacked.getPlayer()).getEntityId());
			}
		}
		return packet;
	}

	@Override
	public void onPacketSend(TabPlayer receiver, Object packet) throws Throwable {
		if (nms.PacketPlayOutEntity.isInstance(packet)) {
			onEntityMove(receiver, nms.PacketPlayOutEntity_ENTITYID.getInt(packet));
		}
		if (nms.PacketPlayOutEntityTeleport.isInstance(packet)) {
			onEntityMove(receiver, nms.PacketPlayOutEntityTeleport_ENTITYID.getInt(packet));
		}
		if (nms.PacketPlayOutNamedEntitySpawn.isInstance(packet)) {
			onEntitySpawn(receiver, nms.PacketPlayOutNamedEntitySpawn_ENTITYID.getInt(packet));
		}
		if (nms.PacketPlayOutEntityDestroy.isInstance(packet)) {
			onEntityDestroy(receiver, (int[]) nms.PacketPlayOutEntityDestroy_ENTITIES.get(packet));
		}
		if (nms.PacketPlayOutMount != null && nms.PacketPlayOutMount.isInstance(packet)) {
			//1.9+ mount detection
			onMount(receiver, nms.PacketPlayOutMount_VEHICLE.getInt(packet), (int[]) nms.PacketPlayOutMount_PASSENGERS.get(packet));
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() == 8 && nms.PacketPlayOutAttachEntity.isInstance(packet) && nms.PacketPlayOutAttachEntity_A.getInt(packet) == 0) {
			//1.8.x mount detection
			onAttach(receiver, nms.PacketPlayOutAttachEntity_VEHICLE.getInt(packet), nms.PacketPlayOutAttachEntity_PASSENGER.getInt(packet));
		}
	}

	public void onEntityMove(TabPlayer receiver, int entityId) {
		TabPlayer pl = nameTagX.entityIdMap.get(entityId);
		Set<Integer> vehicleList;
		if (pl != null) {
			//player moved
			if (!pl.isLoaded() || nameTagX.isDisabledWorld(pl.getWorldName())) return;
			tab.getCPUManager().runMeasuredTask("processing EntityMove", getFeatureType(), UsageType.PACKET_ENTITY_MOVE, () -> pl.getArmorStandManager().teleport(receiver));

			//player can be a vehicle too
			List<Entity> riders = nameTagX.getPassengers((Player) pl.getPlayer());
			for (Entity e : riders) {
				TabPlayer rider = nameTagX.entityIdMap.get(e.getEntityId());
				if (rider != null && !nameTagX.isDisabledWorld(rider.getWorldName())) {
					tab.getCPUManager().runMeasuredTask("processing EntityMove", getFeatureType(), UsageType.PACKET_ENTITY_MOVE, () -> {
						rider.getArmorStandManager().teleport(receiver);
						rider.getArmorStandManager().teleport(pl); //vehicle player has no other way to get this packet
					});
				}
			}
		} else if ((vehicleList = nameTagX.vehicles.get(entityId)) != null){
			//a vehicle carrying something moved
			for (Integer entity : vehicleList) {
				TabPlayer passenger = nameTagX.entityIdMap.get(entity);
				if (passenger != null && passenger.getArmorStandManager() != null && !nameTagX.isDisabledWorld(passenger.getWorldName())) {
					tab.getCPUManager().runMeasuredTask("processing EntityMove", getFeatureType(), UsageType.PACKET_ENTITY_MOVE, () -> passenger.getArmorStandManager().teleport(receiver));
				}
			}
		}
	}

	public void onEntitySpawn(TabPlayer receiver, int entityId) {
		if (receiver.getVersion().getMinorVersion() < 8) return;
		TabPlayer spawnedPlayer = nameTagX.entityIdMap.get(entityId);
		//using bukkit player to check world due to old data on world change due to asynchronous processing & world name changing
		if (spawnedPlayer != null && !nameTagX.isDisabledWorld(((Player)spawnedPlayer.getPlayer()).getWorld().getName()) && spawnedPlayer.isLoaded()) 
			tab.getCPUManager().runMeasuredTask("processing NamedEntitySpawn", getFeatureType(), UsageType.PACKET_NAMED_ENTITY_SPAWN, () -> spawnedPlayer.getArmorStandManager().spawn(receiver));
	}

	public void onEntityDestroy(TabPlayer receiver, int[] entities) {
		for (int id : entities) {
			TabPlayer despawnedPlayer = nameTagX.entityIdMap.get(id);
			if (despawnedPlayer != null && despawnedPlayer.isLoaded()) 
				tab.getCPUManager().runMeasuredTask("processing EntityDestroy", getFeatureType(), UsageType.PACKET_ENTITY_DESTROY, () -> despawnedPlayer.getArmorStandManager().destroy(receiver));
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
			if (pass != null && pass.isLoaded()) tab.getCPUManager().runMeasuredTask("processing Mount", getFeatureType(), UsageType.PACKET_MOUNT, () -> pass.getArmorStandManager().teleport(receiver));
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
		if (pass != null && pass.isLoaded()) tab.getCPUManager().runMeasuredTask("processing Mount", getFeatureType(), UsageType.PACKET_MOUNT, () -> pass.getArmorStandManager().teleport(receiver));
	}

	@Override
	public void onPacketSend(TabPlayer receiver, PacketPlayOutPlayerInfo info) {
		if (!modifyNPCnames || info.action != EnumPlayerInfoAction.ADD_PLAYER) return;
		for (PlayerInfoData playerInfoData : info.entries) {
			if (tab.getPlayerByTablistUUID(playerInfoData.uniqueId) == null && playerInfoData.name.length() <= 15) {
				if (playerInfoData.name.length() <= 14) {
					playerInfoData.name += "\u00a7r";
				} else {
					playerInfoData.name += " ";
				}
			}
		}
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.NAMETAGX;
	}
}