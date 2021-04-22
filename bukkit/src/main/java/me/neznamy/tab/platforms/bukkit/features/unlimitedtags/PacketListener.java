package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
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

	//main feature
	private NameTagX nameTagX;
	
	//nms storage
	private NMSStorage nms;
	
	//tab instance
	private TAB tab;
	
	//if modifying npc names or not
	private boolean modifyNPCnames;

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
		if (receiver.getVersion().getMinorVersion() < 8) return;
		//using bukkit player to check world due to old data on world change due to asynchronous processing & world name changing
		String world = ((Player)receiver.getPlayer()).getWorld().getName();
		if (!receiver.isLoaded() || nameTagX.isDisabledWorld(world) || nameTagX.isDisabledWorld(nameTagX.disabledUnlimitedWorlds, world)) return;
		if (nms.PacketPlayOutEntity.isInstance(packet) && !packet.getClass().getSimpleName().equals("PacketPlayOutEntityLook")) {
			onEntityMove(receiver, nms.PacketPlayOutEntity_ENTITYID.getInt(packet));
		}
		if (nms.PacketPlayOutNamedEntitySpawn.isInstance(packet)) {
			onEntitySpawn(receiver, nms.PacketPlayOutNamedEntitySpawn_ENTITYID.getInt(packet));
		}
		if (nms.PacketPlayOutEntityDestroy.isInstance(packet)) {
			onEntityDestroy(receiver, (int[]) nms.PacketPlayOutEntityDestroy_ENTITIES.get(packet));
		}
	}

	/**
	 * Processes entity move packet
	 * @param receiver - packet receiver
	 * @param entityId - entity that moved
	 */
	public void onEntityMove(TabPlayer receiver, int entityId) {
		TabPlayer pl = nameTagX.entityIdMap.get(entityId);
		List<Entity> vehicleList;
		if (pl != null) {
			//player moved
			tab.getCPUManager().runMeasuredTask("processing EntityMove", getFeatureType(), UsageType.PACKET_ENTITY_MOVE, () -> pl.getArmorStandManager().teleport(receiver));
		} else if ((vehicleList = nameTagX.vehicles.get(entityId)) != null){
			//a vehicle carrying something moved
			for (Entity entity : vehicleList) {
				TabPlayer passenger = nameTagX.entityIdMap.get(entity.getEntityId());
				if (passenger != null && passenger.getArmorStandManager() != null) {
					tab.getCPUManager().runMeasuredTask("processing EntityMove", getFeatureType(), UsageType.PACKET_ENTITY_MOVE, () -> passenger.getArmorStandManager().teleport(receiver));
				}
			}
		}
	}
	
	public void onEntitySpawn(TabPlayer receiver, int entityId) {
		TabPlayer spawnedPlayer = nameTagX.entityIdMap.get(entityId);
		if (spawnedPlayer != null && spawnedPlayer.isLoaded()) {
			tab.getCPUManager().runMeasuredTask("processing NamedEntitySpawn", getFeatureType(), UsageType.PACKET_NAMED_ENTITY_SPAWN, () -> spawnedPlayer.getArmorStandManager().spawn(receiver));
		}
	}

	public void onEntityDestroy(TabPlayer receiver, int[] entities) {
		for (int id : entities) {
			TabPlayer despawnedPlayer = nameTagX.entityIdMap.get(id);
			if (despawnedPlayer != null && despawnedPlayer.isLoaded()) 
				tab.getCPUManager().runMeasuredTask("processing EntityDestroy", getFeatureType(), UsageType.PACKET_ENTITY_DESTROY, () -> despawnedPlayer.getArmorStandManager().destroy(receiver));
		}
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