package me.neznamy.tab.platforms.bukkit;

import java.util.EnumMap;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.api.protocol.PacketPlayOutBoss;
import me.neznamy.tab.api.protocol.PacketPlayOutBoss.Action;
import me.neznamy.tab.api.protocol.PacketPlayOutChat;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerListHeaderFooter;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.platforms.bukkit.nms.AdapterProvider;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.platforms.bukkit.nms.packet.PacketPlayOutEntityDestroy;
import me.neznamy.tab.platforms.bukkit.nms.packet.PacketPlayOutEntityMetadata;
import me.neznamy.tab.platforms.bukkit.nms.packet.PacketPlayOutEntityTeleport;
import me.neznamy.tab.platforms.bukkit.nms.packet.PacketPlayOutSpawnEntityLiving;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

@SuppressWarnings({"unchecked", "rawtypes"})
public class BukkitPacketBuilder extends PacketBuilder {

	//entity type ids
	private final EnumMap<EntityType, Integer> entityIds = new EnumMap<>(EntityType.class);

	/**
	 * Constructs new instance
	 */
	public BukkitPacketBuilder() {
		if (AdapterProvider.getMinorVersion() >= 13) {
			entityIds.put(EntityType.ARMOR_STAND, 1);
			entityIds.put(EntityType.WITHER, 83);
		} else {
			entityIds.put(EntityType.WITHER, 64);
			if (AdapterProvider.getMinorVersion() >= 8){
				entityIds.put(EntityType.ARMOR_STAND, 30);
			}
		}
		buildMap.put(PacketPlayOutEntityMetadata.class, (packet, version) -> build((PacketPlayOutEntityMetadata)packet));
		buildMap.put(PacketPlayOutEntityTeleport.class, (packet, version) -> build((PacketPlayOutEntityTeleport)packet));
		buildMap.put(PacketPlayOutEntityDestroy.class, (packet, version) -> build((PacketPlayOutEntityDestroy)packet));
		buildMap.put(PacketPlayOutSpawnEntityLiving.class, (packet, version) -> build((PacketPlayOutSpawnEntityLiving)packet));
	}

	@Override
	public Object build(PacketPlayOutBoss packet, ProtocolVersion clientVersion) {
		if (AdapterProvider.getMinorVersion() >= 9 || clientVersion.getMinorVersion() >= 9) {
			//1.9+ server or client, handled by bukkit api or viaversion
			return packet;
		}
		//<1.9 client and server
		return buildBossPacketEntity(packet, clientVersion);
	}

	@Override
	public Object build(PacketPlayOutChat packet, ProtocolVersion clientVersion) {
		return AdapterProvider.get().createChatPacket(
				AdapterProvider.get().adaptComponent(packet.getMessage(), clientVersion),
				packet.getType()
		);
	}

	@Override
	public Object build(PacketPlayOutPlayerInfo packet, ProtocolVersion clientVersion) {
		return AdapterProvider.get().createPlayerInfoPacket(packet.getAction(), packet.getEntries());
	}

	@Override
	public Object build(PacketPlayOutPlayerListHeaderFooter packet, ProtocolVersion clientVersion) {
		return AdapterProvider.get().createPlayerListHeaderFooterPacket(packet.getHeader(), packet.getFooter());
	}

	@Override
	public Object build(PacketPlayOutScoreboardDisplayObjective packet, ProtocolVersion clientVersion) {
		return AdapterProvider.get().createDisplayObjectivePacket(packet.getSlot(), packet.getObjectiveName());
	}

	@Override
	public Object build(PacketPlayOutScoreboardObjective packet, ProtocolVersion clientVersion) {
		String displayName = clientVersion.getMinorVersion() < 13 ? cutTo(packet.getDisplayName(), 32) : packet.getDisplayName();
		return AdapterProvider.get().createObjectivePacket(
				packet.getMethod(),
				packet.getObjectiveName(),
				AdapterProvider.get().adaptComponent(IChatBaseComponent.optimizedComponent(displayName), clientVersion),
				packet.getRenderType()
		);
	}

	@Override
	public Object build(PacketPlayOutScoreboardScore packet, ProtocolVersion clientVersion) {
		return AdapterProvider.get().createScorePacket(packet.getAction(), packet.getObjectiveName(), packet.getPlayer(), packet.getScore());
	}

	@Override
	public Object build(PacketPlayOutScoreboardTeam packet, ProtocolVersion clientVersion) {
		String prefix = packet.getPlayerPrefix();
		String suffix = packet.getPlayerSuffix();
		if (clientVersion.getMinorVersion() < 13) {
			prefix = cutTo(prefix, 16);
			suffix = cutTo(suffix, 16);
		}
		return AdapterProvider.get().createTeamPacket(
				packet.getName(),
				prefix,
				suffix,
				packet.getNametagVisibility(),
				packet.getCollisionRule(),
				packet.getColor(),
				packet.getPlayers(),
				packet.getMethod(),
				packet.getOptions()
		);
	}

	/**
	 * Builds entity destroy packet with given parameter
	 * @param id - entity id to destroy
	 * @return destroy packet
	 */
	public Object build(PacketPlayOutEntityDestroy packet) {
		return AdapterProvider.get().createEntityDestroyPacket(packet.getEntities());
	}

	public Object build(PacketPlayOutEntityMetadata packet) {
		return AdapterProvider.get().createMetadataPacket(packet.getEntityId(), packet.getDataWatcher());
	}

	/**
	 * Builds entity spawn packet with given parameters
	 * @param entityId - entity id
	 * @param uuid - entity uuid
	 * @param entityType - entity type
	 * @param loc - location to spawn at
	 * @param dataWatcher - datawatcher
	 * @return entity spawn packet
	 */
	public Object build(PacketPlayOutSpawnEntityLiving packet) {
		return AdapterProvider.get().createSpawnLivingEntityPacket(
				packet.getEntityId(),
				packet.getUniqueId(),
				packet.getEntityType(),
				packet.getLocation(),
				packet.getDataWatcher()
		);
	}

	/**
	 * Builds entity teleport packet with given parameters
	 * @param entityId - entity id
	 * @param location - location to teleport to
	 * @return entity teleport packet
	 */
	public Object build(PacketPlayOutEntityTeleport packet) {
		return AdapterProvider.get().createTeleportPacket(packet.getEntityId(), packet.getLocation());
	}
	
	@Override
	public PacketPlayOutPlayerInfo readPlayerInfo(Object nmsPacket, ProtocolVersion clientVersion) {
		return AdapterProvider.get().createPlayerInfoPacket(nmsPacket);
	}

	@Override
	public PacketPlayOutScoreboardObjective readObjective(Object nmsPacket, ProtocolVersion clientVersion) {
		return AdapterProvider.get().createObjectivePacket(nmsPacket);
	}

	@Override
	public PacketPlayOutScoreboardDisplayObjective readDisplayObjective(Object nmsPacket, ProtocolVersion clientVersion) {
		return AdapterProvider.get().createDisplayObjectivePacket(nmsPacket);
	}

	/**
	 * Builds entity bossbar packet
	 * @param packet - packet to build
	 * @param clientVersion - client version
	 * @return entity bossbar packet
	 */
	private Object buildBossPacketEntity(PacketPlayOutBoss packet, ProtocolVersion clientVersion) {
		if (packet.getOperation() == Action.UPDATE_STYLE) return null; //nothing to do here

		int entityId = packet.getId().hashCode();
		if (packet.getOperation() == Action.REMOVE) {
			return build(new PacketPlayOutEntityDestroy(entityId));
		}
		DataWatcher w = new DataWatcher();
		if (packet.getOperation() == Action.UPDATE_PCT || packet.getOperation() == Action.ADD) {
			float health = 300*packet.getPct();
			if (health == 0) health = 1;
			w.helper().setHealth(health);
		}
		if (packet.getOperation() == Action.UPDATE_NAME || packet.getOperation() == Action.ADD) {
			w.helper().setCustomName(packet.getName(), clientVersion);
		}
		if (packet.getOperation() == Action.ADD) {
			w.helper().setEntityFlags((byte) 32);
			return build(new PacketPlayOutSpawnEntityLiving(entityId, null, EntityType.WITHER, new Location(null, 0,0,0), w));
		} else {
			return build(new PacketPlayOutEntityMetadata(entityId, w));
		}
	}
}
