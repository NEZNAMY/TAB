package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;

/**
 * The packet listening part for securing proper functionality of armor stands
 * Bukkit events are too unreliable and delayed/ahead which causes de-sync
 */
public class PacketListener extends TabFeature {

	//main feature
	private final NameTagX nameTagX;
	
	//nms storage
	private final NMSStorage nms = NMSStorage.getInstance();

	/**
	 * Constructs new instance with given parameters and loads config options
	 * @param nameTagX - main feature
	 */
	public PacketListener(NameTagX nameTagX) {
		super(nameTagX.getFeatureName(), null);
		this.nameTagX = nameTagX;
	}

	@Override
	public boolean onPacketReceive(TabPlayer sender, Object packet) throws ReflectiveOperationException {
		if (sender.getVersion().getMinorVersion() == 8 && nms.PacketPlayInUseEntity.isInstance(packet)) {
			int entityId = nms.PacketPlayInUseEntity_ENTITY.getInt(packet);
			TabPlayer attacked = null;
			for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
				if (all.isLoaded() && all.getArmorStandManager().hasArmorStandWithID(entityId)) {
					attacked = all;
					break;
				}
			}
			if (attacked != null && attacked != sender) {
				nms.setField(packet, nms.PacketPlayInUseEntity_ENTITY, ((Player) attacked.getPlayer()).getEntityId());
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onPacketSend(TabPlayer receiver, Object packet) throws ReflectiveOperationException {
		if (receiver.getVersion().getMinorVersion() < 8) return;
		if (!receiver.isLoaded() || nameTagX.isDisabledPlayer(receiver) || nameTagX.getPlayersInDisabledUnlimitedWorlds().contains(receiver)) return;
		if (nms.PacketPlayOutEntity.isInstance(packet) && !nms.PacketPlayOutEntityLook.isInstance(packet)) { //ignoring head rotation only packets
			onEntityMove(receiver, nms.PacketPlayOutEntity_ENTITYID.getInt(packet));
		} else if (nms.PacketPlayOutEntityTeleport.isInstance(packet)) {
			onEntityMove(receiver, nms.PacketPlayOutEntityTeleport_ENTITYID.getInt(packet));
		} else if (nms.PacketPlayOutNamedEntitySpawn.isInstance(packet)) {
			onEntitySpawn(receiver, nms.PacketPlayOutNamedEntitySpawn_ENTITYID.getInt(packet));
		} else if (nms.PacketPlayOutEntityDestroy.isInstance(packet)) {
			if (nms.getMinorVersion() >= 17) {
				Object entities = nms.PacketPlayOutEntityDestroy_ENTITIES.get(packet);
				if (entities instanceof List) {
					onEntityDestroy(receiver, (List<Integer>) entities);
				} else {
					//1.17.0
					onEntityDestroy(receiver, (int) entities);
				}
			} else {
				onEntityDestroy(receiver, (int[]) nms.PacketPlayOutEntityDestroy_ENTITIES.get(packet));
			}
		}
	}

	/**
	 * Processes entity move packet
	 * @param receiver - packet receiver
	 * @param entityId - entity that moved
	 */
	private void onEntityMove(TabPlayer receiver, int entityId) {
		TabPlayer pl = nameTagX.getEntityIdMap().get(entityId);
		List<Entity> vehicleList;
		if (pl != null) {
			//player moved
			if (nameTagX.isPlayerDisabled(pl)) return;
			TAB.getInstance().getCPUManager().runMeasuredTask("processing EntityMove", nameTagX, TabConstants.CpuUsageCategory.PACKET_ENTITY_MOVE, () -> pl.getArmorStandManager().teleport(receiver));
		} else if ((vehicleList = nameTagX.getVehicleManager().getVehicles().get(entityId)) != null){
			//a vehicle carrying something moved
			for (Entity entity : vehicleList) {
				TabPlayer passenger = nameTagX.getEntityIdMap().get(entity.getEntityId());
				if (passenger != null && passenger.getArmorStandManager() != null) {
					TAB.getInstance().getCPUManager().runMeasuredTask("processing EntityMove", nameTagX, TabConstants.CpuUsageCategory.PACKET_ENTITY_MOVE_PASSENGER, () -> passenger.getArmorStandManager().teleport(receiver));
				}
			}
		}
	}
	
	private void onEntitySpawn(TabPlayer receiver, int entityId) {
		TabPlayer spawnedPlayer = nameTagX.getEntityIdMap().get(entityId);
		if (spawnedPlayer != null && spawnedPlayer.isLoaded() && !nameTagX.isPlayerDisabled(spawnedPlayer)) {
			TAB.getInstance().getCPUManager().runMeasuredTask("processing NamedEntitySpawn", nameTagX, TabConstants.CpuUsageCategory.PACKET_ENTITY_SPAWN, () -> spawnedPlayer.getArmorStandManager().spawn(receiver));
		}
	}

	private void onEntityDestroy(TabPlayer receiver, List<Integer> entities) {
		for (int entity : entities) {
			onEntityDestroy(receiver, entity);
		}
	}
	
	private void onEntityDestroy(TabPlayer receiver, int... entities) {
		for (int entity : entities) {
			onEntityDestroy(receiver, entity);
		}
	}
	
	private void onEntityDestroy(TabPlayer receiver, int entity) {
		TabPlayer deSpawnedPlayer = nameTagX.getEntityIdMap().get(entity);
		if (deSpawnedPlayer != null && deSpawnedPlayer.isLoaded() && !nameTagX.isPlayerDisabled(deSpawnedPlayer))
			TAB.getInstance().getCPUManager().runMeasuredTask("processing EntityDestroy", nameTagX, TabConstants.CpuUsageCategory.PACKET_ENTITY_DESTROY, () -> deSpawnedPlayer.getArmorStandManager().destroy(receiver));
	}
}