package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOut;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.interfaces.PlayerInfoPacketListener;
import me.neznamy.tab.shared.features.interfaces.RawPacketFeature;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class PacketListener implements RawPacketFeature, PlayerInfoPacketListener {

	private Field PacketPlayInUseEntity_ENTITY;

	private Field PacketPlayOutNamedEntitySpawn_ENTITYID;
	private Field PacketPlayOutEntityDestroy_ENTITIES;
	private Field PacketPlayOutEntity_ENTITYID;

	private Field PacketPlayOutMount_VEHICLE;
	private Field PacketPlayOutMount_PASSENGERS;

	private Field PacketPlayOutAttachEntity_A;
	private Field PacketPlayOutAttachEntity_PASSENGER;
	private Field PacketPlayOutAttachEntity_VEHICLE;
	
	private boolean modifyNPCnames;
	private NameTagX nameTagX;
	
	public PacketListener(NameTagX nameTagX) {
		PacketPlayInUseEntity_ENTITY = PacketPlayOut.getFields(MethodAPI.PacketPlayInUseEntity).get("a");
		PacketPlayOutNamedEntitySpawn_ENTITYID = PacketPlayOut.getFields(MethodAPI.PacketPlayOutNamedEntitySpawn).get("a");
		PacketPlayOutEntityDestroy_ENTITIES = PacketPlayOut.getFields(MethodAPI.PacketPlayOutEntityDestroy).get("a");
		PacketPlayOutEntity_ENTITYID = PacketPlayOut.getFields(MethodAPI.PacketPlayOutEntity).get("a");

		Map<String, Field> mount = PacketPlayOut.getFields(MethodAPI.PacketPlayOutMount);
		PacketPlayOutMount_VEHICLE = mount.get("a");
		PacketPlayOutMount_PASSENGERS = mount.get("b");

		Map<String, Field> attachentity = PacketPlayOut.getFields(MethodAPI.PacketPlayOutAttachEntity);
		PacketPlayOutAttachEntity_A = attachentity.get("a");
		PacketPlayOutAttachEntity_PASSENGER = attachentity.get("b");
		PacketPlayOutAttachEntity_VEHICLE = attachentity.get("c");
		
		modifyNPCnames = Configs.config.getBoolean("unlimited-nametag-prefix-suffix-mode.modify-npc-names", false);
		this.nameTagX = nameTagX;
	}
	
	@Override
	public Object onPacketReceive(ITabPlayer sender, Object packet) throws Throwable {
		if (sender.getVersion().getMinorVersion() == 8 && MethodAPI.PacketPlayInUseEntity.isInstance(packet)) {
			int entityId = PacketPlayInUseEntity_ENTITY.getInt(packet);
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
				PacketPlayInUseEntity_ENTITY.set(packet, attacked.getBukkitEntity().getEntityId());
			}
		}
		return packet;
	}
	
	@Override
	public Object onPacketSend(ITabPlayer receiver, Object packet) throws Throwable {
		if (MethodAPI.PacketPlayOutEntity.isInstance(packet)) {
			int id = PacketPlayOutEntity_ENTITYID.getInt(packet);
			ITabPlayer pl = Shared.entityIdMap.get(id);
			List<Integer> vehicleList;
			if (pl != null) {
				//player moved
				Shared.featureCpu.runMeasuredTask("processing EntityMove", CPUFeature.NAMETAGX_PACKET_ENTITY_MOVE, new Runnable() {
					public void run() {
						pl.getArmorStands().forEach(a -> receiver.sendPacket(a.getTeleportPacket(receiver)));
					}
				});
			} else if ((vehicleList = nameTagX.vehicles.get(id)) != null){
				//a vehicle carrying something moved
				for (Integer entity : vehicleList) {
					ITabPlayer passenger = Shared.entityIdMap.get(entity);
					if (passenger != null) {
						Shared.featureCpu.runMeasuredTask("processing EntityMove", CPUFeature.NAMETAGX_PACKET_ENTITY_MOVE, new Runnable() {
							public void run() {
								passenger.getArmorStands().forEach(a -> receiver.sendPacket(a.getTeleportPacket(receiver)));
							}
						});
					}
				}
			}
		}
		if (MethodAPI.PacketPlayOutNamedEntitySpawn.isInstance(packet)) {
			int entity = PacketPlayOutNamedEntitySpawn_ENTITYID.getInt(packet);
			ITabPlayer spawnedPlayer = Shared.entityIdMap.get(entity);
			if (spawnedPlayer != null && !spawnedPlayer.disabledNametag) Shared.featureCpu.runMeasuredTask("processing NamedEntitySpawn", CPUFeature.NAMETAGX_PACKET_NAMED_ENTITY_SPAWN, new Runnable() {
				public void run() {
					nameTagX.spawnArmorStand(spawnedPlayer, receiver);
				}
			});
		}
		if (MethodAPI.PacketPlayOutEntityDestroy.isInstance(packet)) {
			int[] entites = (int[]) PacketPlayOutEntityDestroy_ENTITIES.get(packet);
			for (int id : entites) {
				ITabPlayer despawnedPlayer = Shared.entityIdMap.get(id);
				if (despawnedPlayer != null && !despawnedPlayer.disabledNametag) Shared.featureCpu.runMeasuredTask("processing EntityDestroy", CPUFeature.NAMETAGX_PACKET_ENTITY_DESTROY, new Runnable() {
					public void run() {
						despawnedPlayer.getArmorStands().forEach(a -> a.destroy(receiver));
					}
				});
			}
		}
		if (MethodAPI.PacketPlayOutMount != null && MethodAPI.PacketPlayOutMount.isInstance(packet)) {
			//1.9+ mount detection
			int vehicle = PacketPlayOutMount_VEHICLE.getInt(packet);
			int[] passg = (int[]) PacketPlayOutMount_PASSENGERS.get(packet);
			Integer[] passengers = new Integer[passg.length];
			for (int i=0; i<passg.length; i++) {
				passengers[i] = passg[i];
			}
			if (passengers.length == 0) {
				//detach
				nameTagX.vehicles.remove(vehicle);
			} else {
				//attach
				nameTagX.vehicles.put(vehicle, Arrays.asList(passengers));
			}
			for (int entity : passengers) {
				ITabPlayer pass = Shared.entityIdMap.get(entity);
				if (pass != null) Shared.featureCpu.runMeasuredTask("processing Mount", CPUFeature.NAMETAGX_PACKET_MOUNT, new Runnable() {
					public void run() {
						pass.getArmorStands().forEach(a -> receiver.sendPacket(a.getTeleportPacket(receiver)));
					}
				});
			}
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() == 8 && MethodAPI.PacketPlayOutAttachEntity.isInstance(packet)) {
			//1.8.x mount detection
			if (PacketPlayOutAttachEntity_A.getInt(packet) == 0) {
				int passenger = PacketPlayOutAttachEntity_PASSENGER.getInt(packet);
				int vehicle = PacketPlayOutAttachEntity_VEHICLE.getInt(packet);
				if (vehicle != -1) {
					//attach
					nameTagX.vehicles.put(vehicle, Arrays.asList(passenger));
				} else {
					//detach
					for (Entry<Integer, List<Integer>> entry : nameTagX.vehicles.entrySet()) {
						if (entry.getValue().contains(passenger)) {
							nameTagX.vehicles.remove(entry.getKey());
						}
					}
				}
				ITabPlayer pass = Shared.entityIdMap.get(passenger);
				if (pass != null) Shared.featureCpu.runMeasuredTask("processing Mount", CPUFeature.NAMETAGX_PACKET_MOUNT, new Runnable() {
					public void run() {
						pass.getArmorStands().forEach(a -> receiver.sendPacket(a.getTeleportPacket(receiver)));
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
}
