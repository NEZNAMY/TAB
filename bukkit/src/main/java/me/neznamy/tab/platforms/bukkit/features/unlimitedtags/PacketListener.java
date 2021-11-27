package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.List;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.AdapterProvider;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * The packet listening part for securing proper functionality of armor stands
 * Bukkit events are too unreliable and delayed/ahead which causes desync
 */
public class PacketListener extends TabFeature {

	//main feature
	private final NameTagX nameTagX;

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
		if (sender.getVersion().getMinorVersion() == 8 && AdapterProvider.get().isInteractPacket(packet)) {
			int entityId = AdapterProvider.get().getInteractEntityId(packet);
			TabPlayer attacked = null;
			for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
				if (all.isLoaded() && all.getArmorStandManager().hasArmorStandWithID(entityId)) {
					attacked = all;
					break;
				}
			}
			if (attacked != null && attacked != sender) {
				AdapterProvider.get().setInteractEntityId(packet, ((Player) attacked.getPlayer()).getEntityId());
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onPacketSend(TabPlayer receiver, Object packet) throws ReflectiveOperationException {
		if (receiver.getVersion().getMinorVersion() < 8) return;
		if (!receiver.isLoaded() || nameTagX.isDisabledPlayer(receiver) || nameTagX.getPlayersInDisabledUnlimitedWorlds().contains(receiver)) return;
		if (AdapterProvider.get().isMovePacket(packet) && !AdapterProvider.get().isHeadLookPacket(packet)) { //ignoring head rotation only packets
			onEntityMove(receiver, AdapterProvider.get().getMoveEntityId(packet));
		} else if (AdapterProvider.get().isTeleportPacket(packet)) {
			onEntityMove(receiver, AdapterProvider.get().getTeleportEntityId(packet));
		} else if (AdapterProvider.get().isSpawnPlayerPacket(packet)) {
			onEntitySpawn(receiver, AdapterProvider.get().getPlayerSpawnId(packet));
		} else if (AdapterProvider.get().isDestroyPacket(packet)) {
			onEntityDestroy(receiver, AdapterProvider.get().getDestroyEntities(packet));
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
		TabPlayer despawnedPlayer = nameTagX.getEntityIdMap().get(entity);
		if (despawnedPlayer != null && despawnedPlayer.isLoaded() && !nameTagX.isPlayerDisabled(despawnedPlayer)) 
			TAB.getInstance().getCPUManager().runMeasuredTask("processing EntityDestroy", nameTagX, TabConstants.CpuUsageCategory.PACKET_ENTITY_DESTROY, () -> despawnedPlayer.getArmorStandManager().destroy(receiver));
	}
}
