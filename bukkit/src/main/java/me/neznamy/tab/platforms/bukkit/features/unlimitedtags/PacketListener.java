package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.types.packet.RawPacketListener;

/**
 * The packet listening part for securing proper functionality of armor stands
 * Bukkit events are too unreliable and delayed/ahead which causes desync
 */
public class PacketListener implements RawPacketListener {

	//main feature
	private NameTagX nameTagX;
	
	//nms storage
	private NMSStorage nms;
	
	//tab instance
	private TAB tab;

	/**
	 * Constructs new instance with given parameters and loads config options
	 * @param nameTagX - main feature
	 * @param nms - nms storage
	 * @param tab - tab instance
	 */
	public PacketListener(NameTagX nameTagX, NMSStorage nms, TAB tab) {
		this.nameTagX = nameTagX;
		this.nms = nms;
		this.tab = tab;
	}

	@Override
	public Object onPacketReceive(TabPlayer sender, Object packet) throws IllegalAccessException {
		if (sender.getVersion().getMinorVersion() == 8 && nms.getClass("PacketPlayInUseEntity").isInstance(packet)) {
			int entityId = nms.getField("PacketPlayInUseEntity_ENTITY").getInt(packet);
			TabPlayer attacked = null;
			for (TabPlayer all : tab.getPlayers()) {
				if (all.isLoaded() && all.getArmorStandManager().hasArmorStandWithID(entityId)) {
					attacked = all;
					break;
				}
			}
			if (attacked != null && attacked != sender) {
				nms.setField(packet, "PacketPlayInUseEntity_ENTITY", ((Player) attacked.getPlayer()).getEntityId());
			}
		}
		return packet;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onPacketSend(TabPlayer receiver, Object packet) throws IllegalAccessException  {
		if (receiver.getVersion().getMinorVersion() < 8) return;
		//using bukkit player to check world due to old data on world change due to asynchronous processing & world name changing
		String world = ((Player)receiver.getPlayer()).getWorld().getName();
		if (!receiver.isLoaded() || nameTagX.getPlayersInDisabledWorlds().contains(receiver) || nameTagX.isDisabledWorld(nameTagX.disabledUnlimitedWorlds, world)) return;
		if (nms.getClass("PacketPlayOutEntity").isInstance(packet) && !nms.getClass("PacketPlayOutEntityLook").isInstance(packet)) { //ignoring head rotation only packets
			onEntityMove(receiver, nms.getField("PacketPlayOutEntity_ENTITYID").getInt(packet));
		} else if (nms.getClass("PacketPlayOutEntityTeleport").isInstance(packet)) {
			onEntityMove(receiver, nms.getField("PacketPlayOutEntityTeleport_ENTITYID").getInt(packet));
		} else if (nms.getClass("PacketPlayOutNamedEntitySpawn").isInstance(packet)) {
			onEntitySpawn(receiver, nms.getField("PacketPlayOutNamedEntitySpawn_ENTITYID").getInt(packet));
		} else if (nms.getClass("PacketPlayOutEntityDestroy").isInstance(packet)) {
			if (nms.getMinorVersion() >= 17) {
				try {
					onEntityDestroy(receiver, (List<Integer>) nms.getField("PacketPlayOutEntityDestroy_ENTITIES").get(packet));
				} catch (ClassCastException e) {
					//1.17.0
					onEntityDestroy(receiver, nms.getField("PacketPlayOutEntityDestroy_ENTITIES").getInt(packet));
				}
			} else {
				onEntityDestroy(receiver, (int[]) nms.getField("PacketPlayOutEntityDestroy_ENTITIES").get(packet));
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
			tab.getCPUManager().runMeasuredTask("processing EntityMove", getFeatureType(), UsageType.PACKET_ENTITY_MOVE, () -> pl.getArmorStandManager().teleport(receiver));
		} else if ((vehicleList = nameTagX.getVehicles().get(entityId)) != null){
			//a vehicle carrying something moved
			for (Entity entity : vehicleList) {
				TabPlayer passenger = nameTagX.getEntityIdMap().get(entity.getEntityId());
				if (passenger != null && passenger.getArmorStandManager() != null) {
					tab.getCPUManager().runMeasuredTask("processing EntityMove", getFeatureType(), UsageType.PACKET_ENTITY_MOVE, () -> passenger.getArmorStandManager().teleport(receiver));
				}
			}
		}
	}
	
	private void onEntitySpawn(TabPlayer receiver, int entityId) {
		TabPlayer spawnedPlayer = nameTagX.getEntityIdMap().get(entityId);
		if (spawnedPlayer != null && spawnedPlayer.isLoaded()) {
			tab.getCPUManager().runMeasuredTask("processing NamedEntitySpawn", getFeatureType(), UsageType.PACKET_NAMED_ENTITY_SPAWN, () -> spawnedPlayer.getArmorStandManager().spawn(receiver));
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
		if (despawnedPlayer != null && despawnedPlayer.isLoaded()) 
			tab.getCPUManager().runMeasuredTask("processing EntityDestroy", getFeatureType(), UsageType.PACKET_ENTITY_DESTROY, () -> despawnedPlayer.getArmorStandManager().destroy(receiver));
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.NAMETAGX;
	}
}