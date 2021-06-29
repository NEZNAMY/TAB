package me.neznamy.tab.platforms.bukkit;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.gson.JsonParser;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.packets.EnumChatFormat;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.IChatBaseComponent.ClickAction;
import me.neznamy.tab.shared.packets.PacketBuilder;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss.Action;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerListHeaderFooter;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.shared.rgb.TextColor;

@SuppressWarnings("unchecked")
public class BukkitPacketBuilder implements PacketBuilder {

	//nms storage
	private NMSStorage nms;

	//entity type ids
	private EnumMap<EntityType, Integer> entityIds = new EnumMap<>(EntityType.class);

	/**
	 * Constructs new instance with given parameter
	 * @param nms - nms storage
	 */
	public BukkitPacketBuilder(NMSStorage nms) {
		this.nms = nms;
		if (nms.getMinorVersion() >= 13) {
			entityIds.put(EntityType.ARMOR_STAND, 1);
			entityIds.put(EntityType.WITHER, 83);
		} else {
			entityIds.put(EntityType.WITHER, 64);
			if (nms.getMinorVersion() >= 8){
				entityIds.put(EntityType.ARMOR_STAND, 30);
			}
		}
	}

	@Override
	public Object build(PacketPlayOutBoss packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (nms.getMinorVersion() >= 9) {
			//1.9+ server, handled using bukkit api
			return packet;
		}
		if (clientVersion.getMinorVersion() >= 9 && Bukkit.getPluginManager().isPluginEnabled("ViaVersion")) {
			//1.9+ client on 1.8 server
			//technically redundant VV check as there is no other way to get 1.9 client on 1.8 server
			return buildBossPacketVia(packet, clientVersion);
		}

		//<1.9 client and server
		return buildBossPacketEntity(packet, clientVersion);
	}

	/**
	 * Builds 1.9 bossbar packet bytebuf using viaversion
	 * @param packet - packet to build
	 * @param clientVersion - client version
	 * @return bytebuf with 1.9 bossbar packet content
	 */
	private ByteBuf buildBossPacketVia(PacketPlayOutBoss packet, ProtocolVersion clientVersion) {
		if (clientVersion == ProtocolVersion.UNKNOWN) return null; //preventing disconnect if packet ID changes and users do not update
		try {
			ByteBuf buf = Unpooled.buffer();
			Type.VAR_INT.writePrimitive(buf, clientVersion.getMinorVersion() == 15 || clientVersion.getMinorVersion() >= 17 ? 0x0D : 0x0C);
			Type.UUID.write(buf, packet.getId());
			Type.VAR_INT.writePrimitive(buf, packet.getOperation().ordinal());
			switch (packet.getOperation()) {
			case ADD:
				Type.COMPONENT.write(buf, JsonParser.parseString(IChatBaseComponent.optimizedComponent(packet.getName()).toString(clientVersion)));
				Type.FLOAT.writePrimitive(buf, packet.getPct());
				Type.VAR_INT.writePrimitive(buf, packet.getColor().ordinal());
				Type.VAR_INT.writePrimitive(buf, packet.getOverlay().ordinal());
				Type.BYTE.write(buf, packet.getFlags());
				break;
			case REMOVE:
				break;
			case UPDATE_PCT:
				Type.FLOAT.writePrimitive(buf, packet.getPct());
				break;
			case UPDATE_NAME:
				Type.COMPONENT.write(buf, JsonParser.parseString(IChatBaseComponent.optimizedComponent(packet.getName()).toString(clientVersion)));
				break;
			case UPDATE_STYLE:
				Type.VAR_INT.writePrimitive(buf, packet.getColor().ordinal());
				Type.VAR_INT.writePrimitive(buf, packet.getOverlay().ordinal());
				break;
			case UPDATE_PROPERTIES:
				Type.BYTE.write(buf, packet.getFlags());
				break;
			default:
				break;
			}
			return buf;
		} catch (Exception t) {
			return TAB.getInstance().getErrorManager().printError(null, "Failed to create 1.9 bossbar packet using ViaVersion v" + Bukkit.getPluginManager().getPlugin("ViaVersion").getDescription().getVersion() + ". Is it the latest version?", t);
		}
	}

	/**
	 * Builds entity bossbar packet
	 * @param packet - packet to build
	 * @param clientVersion - client version
	 * @return entity bossbar packet
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	private Object buildBossPacketEntity(PacketPlayOutBoss packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (packet.getOperation() == Action.UPDATE_STYLE) return null; //nothing to do here

		int entityId = Math.abs(packet.getId().hashCode());
		if (packet.getOperation() == Action.REMOVE) {
			return buildEntityDestroyPacket(entityId);
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
			return buildEntitySpawnPacket(entityId, null, EntityType.WITHER, new Location(null, 0,0,0), w);
		} else {
			return buildEntityMetadataPacket(entityId, w);
		}
	}

	@Override
	public Object build(PacketPlayOutChat packet, ProtocolVersion clientVersion) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, NoSuchMethodException, SecurityException {
		Object component = stringToComponent(packet.getMessage().toString(clientVersion));
		if (nms.getMinorVersion() >= 16) {
			return nms.newPacketPlayOutChat.newInstance(component, ((Object[])nms.ChatMessageType.getMethod("values").invoke(null))[packet.getType().ordinal()], UUID.randomUUID());
		}
		if (nms.getMinorVersion() >= 12) {
			return nms.newPacketPlayOutChat.newInstance(component, Enum.valueOf(nms.ChatMessageType, packet.getType().toString()));
		}
		if (nms.getMinorVersion() >= 8) {
			return nms.newPacketPlayOutChat.newInstance(component, (byte) packet.getType().ordinal());
		}
		if (nms.getMinorVersion() == 7) {
			return nms.newPacketPlayOutChat.newInstance(component);
		}
		throw new IllegalStateException("Not supported on <1.7");
	}

	@Override
	public Object build(PacketPlayOutPlayerInfo packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NegativeArraySizeException {
		if (nms.getMinorVersion() < 8) return null;
		Object nmsPacket = nms.newPacketPlayOutPlayerInfo.newInstance(((Object[])nms.EnumPlayerInfoAction.getMethod("values").invoke(null))[packet.getAction().ordinal()], Array.newInstance(nms.EntityPlayer, 0));
		List<Object> items = new ArrayList<>();
		for (PlayerInfoData data : packet.getEntries()) {
			GameProfile profile = new GameProfile(data.uniqueId, data.name);
			
			if (data.skin != null) profile.getProperties().putAll((PropertyMap) data.skin);
			List<Object> parameters = new ArrayList<>();
			if (nms.newPlayerInfoData.getParameterCount() == 5) {
				parameters.add(nmsPacket);
			}
			parameters.add(profile);
			parameters.add(data.latency);
			Object[] values = (Object[]) nms.EnumGamemode.getMethod("values").invoke(null);
			parameters.add(data.gameMode == null ? null : values[values.length-EnumGamemode.values().length+data.gameMode.ordinal()]); //not_set was removed in 1.17
			parameters.add(data.displayName == null ? null : toNMSComponent(data.displayName, clientVersion));
			items.add(nms.newPlayerInfoData.newInstance(parameters.toArray()));
		}
		nms.PacketPlayOutPlayerInfo_PLAYERS.set(nmsPacket, items);
		return nmsPacket;
	}

	@Override
	public Object build(PacketPlayOutPlayerListHeaderFooter packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (nms.PacketPlayOutPlayerListHeaderFooter == null) return null;
		if (nms.getMinorVersion() >= 17) {
			return nms.newPacketPlayOutPlayerListHeaderFooter.newInstance(toNMSComponent(packet.getHeader(), clientVersion), toNMSComponent(packet.getFooter(), clientVersion));
		}
		Object nmsPacket = nms.newPacketPlayOutPlayerListHeaderFooter.newInstance();
		nms.PacketPlayOutPlayerListHeaderFooter_HEADER.set(nmsPacket, toNMSComponent(packet.getHeader(), clientVersion));
		nms.PacketPlayOutPlayerListHeaderFooter_FOOTER.set(nmsPacket, toNMSComponent(packet.getFooter(), clientVersion));
		return nmsPacket;
	}

	@Override
	public Object build(PacketPlayOutScoreboardDisplayObjective packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return nms.newPacketPlayOutScoreboardDisplayObjective.newInstance(packet.getSlot(), newScoreboardObjective(packet.getObjectiveName()));
	}

	@Override
	public Object build(PacketPlayOutScoreboardObjective packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		String displayName = clientVersion.getMinorVersion() < 13 ? cutTo(packet.getDisplayName(), 32) : packet.getDisplayName();
		if (nms.getMinorVersion() >= 13) {
			return nms.newPacketPlayOutScoreboardObjective.newInstance(nms.newScoreboardObjective.newInstance(null, packet.getObjectiveName(), null, 
					toNMSComponent(IChatBaseComponent.optimizedComponent(displayName), clientVersion), 
					packet.getRenderType() == null ? null : ((Object[])nms.EnumScoreboardHealthDisplay.getMethod("values").invoke(null))[packet.getRenderType().ordinal()]), 
					packet.getMethod());
		}
		
		Object nmsPacket = nms.newPacketPlayOutScoreboardObjective.newInstance();
		nms.PacketPlayOutScoreboardObjective_OBJECTIVENAME.set(nmsPacket, packet.getObjectiveName());
		nms.PacketPlayOutScoreboardObjective_DISPLAYNAME.set(nmsPacket, displayName);
		if (nms.PacketPlayOutScoreboardObjective_RENDERTYPE != null && packet.getRenderType() != null) {
			nms.PacketPlayOutScoreboardObjective_RENDERTYPE.set(nmsPacket, Enum.valueOf(nms.EnumScoreboardHealthDisplay, packet.getRenderType().toString()));
		}
		nms.PacketPlayOutScoreboardObjective_METHOD.set(nmsPacket, packet.getMethod());
		return nmsPacket;
	}

	@Override
	public Object build(PacketPlayOutScoreboardScore packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if (nms.getMinorVersion() >= 13) {
			return nms.newPacketPlayOutScoreboardScore_1_13.newInstance(((Object[])nms.EnumScoreboardAction.getMethod("values").invoke(null))[packet.getAction().ordinal()], packet.getObjectiveName(), packet.getPlayer(), packet.getScore());
		}
		if (packet.getAction() == PacketPlayOutScoreboardScore.Action.REMOVE) {
			return nms.newPacketPlayOutScoreboardScore_String.newInstance(packet.getPlayer());
		}
		Object score = nms.newScoreboardScore.newInstance(nms.newScoreboard.newInstance(), newScoreboardObjective(packet.getObjectiveName()), packet.getPlayer());
		nms.ScoreboardScore_setScore.invoke(score, packet.getScore());
		if (nms.getMinorVersion() >= 8) {
			return nms.newPacketPlayOutScoreboardScore.newInstance(score);
		}
		return nms.newPacketPlayOutScoreboardScore.newInstance(score, 0);
	}

	@Override
	public Object build(PacketPlayOutScoreboardTeam packet, ProtocolVersion clientVersion) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, NoSuchMethodException, SecurityException {
		Object team;
		if (nms.getMinorVersion() >= 13) {
			team = createTeamModern(packet, clientVersion);
		} else {
			team = createTeamLegacy(packet, clientVersion);
		}
		if (nms.getMinorVersion() >= 17) {
			switch (packet.getMethod()) {
			case 0:
				return nms.PacketPlayOutScoreboardTeam_ofBoolean.invoke(null, team, true);
			case 1:
				return nms.PacketPlayOutScoreboardTeam_of.invoke(null, team);
			case 2:
				return nms.PacketPlayOutScoreboardTeam_ofBoolean.invoke(null, team, false);
			case 3:
				return nms.PacketPlayOutScoreboardTeam_ofString.invoke(null, team, packet.getPlayers().toArray(new String[0])[0], ((Object[])nms.PacketPlayOutScoreboardTeam_a.getMethod("values").invoke(null))[0]);
			case 4:
				return nms.PacketPlayOutScoreboardTeam_ofString.invoke(null, team, packet.getPlayers().toArray(new String[0])[0], ((Object[])nms.PacketPlayOutScoreboardTeam_a.getMethod("values").invoke(null))[1]);
			default:
				throw new IllegalArgumentException("Invalid action: " + packet.getMethod());
			}
		}
		return nms.newPacketPlayOutScoreboardTeam.newInstance(team, packet.getMethod());
	}
	
	private Object createTeamModern(PacketPlayOutScoreboardTeam packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		String prefix = packet.getPlayerPrefix();
		String suffix = packet.getPlayerSuffix();
		if (clientVersion.getMinorVersion() < 13) {
			prefix = cutTo(prefix, 16);
			suffix = cutTo(suffix, 16);
		}
		Object team = nms.newScoreboardTeam.newInstance(nms.newScoreboard.newInstance(), packet.getName());
		((Collection<String>)nms.ScoreboardTeam_getPlayerNameSet.invoke(team)).addAll(packet.getPlayers());
		if (prefix != null && prefix.length() > 0) nms.ScoreboardTeam_setPrefix.invoke(team, toNMSComponent(IChatBaseComponent.optimizedComponent(prefix), clientVersion));
		if (suffix != null && suffix.length() > 0) nms.ScoreboardTeam_setSuffix.invoke(team, toNMSComponent(IChatBaseComponent.optimizedComponent(suffix), clientVersion));
		EnumChatFormat format = packet.getColor() != null ? packet.getColor() : EnumChatFormat.lastColorsOf(prefix);
		nms.ScoreboardTeam_setColor.invoke(team, ((Object[])nms.EnumChatFormat.getMethod("values").invoke(null))[format.ordinal()]);
		if (packet.getNametagVisibility() != null) nms.ScoreboardTeam_setNameTagVisibility.invoke(team, packet.getNametagVisibility().equals("always") ? ((Object[])nms.EnumNameTagVisibility.getMethod("values").invoke(null))[0] : ((Object[])nms.EnumNameTagVisibility.getMethod("values").invoke(null))[1]);
		if (packet.getCollisionRule() != null) nms.ScoreboardTeam_setCollisionRule.invoke(team, packet.getCollisionRule().equals("always") ? ((Object[])nms.EnumTeamPush.getMethod("values").invoke(null))[0] : ((Object[])nms.EnumTeamPush.getMethod("values").invoke(null))[1]);
		return team;
	}
	
	private Object createTeamLegacy(PacketPlayOutScoreboardTeam packet, ProtocolVersion clientVersion) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, NoSuchMethodException, SecurityException {
		String prefix = packet.getPlayerPrefix();
		String suffix = packet.getPlayerSuffix();
		if (clientVersion.getMinorVersion() < 13) {
			prefix = cutTo(prefix, 16);
			suffix = cutTo(suffix, 16);
		}
		Object team = nms.newScoreboardTeam.newInstance(nms.newScoreboard.newInstance(), packet.getName());
		((Collection<String>)nms.ScoreboardTeam_getPlayerNameSet.invoke(team)).addAll(packet.getPlayers());
		if (prefix != null) nms.ScoreboardTeam_setPrefix.invoke(team, prefix);
		if (suffix != null) nms.ScoreboardTeam_setSuffix.invoke(team, suffix);
		if (nms.EnumNameTagVisibility != null && packet.getNametagVisibility() != null) nms.ScoreboardTeam_setNameTagVisibility.invoke(team, packet.getNametagVisibility().equals("always") ? ((Object[])nms.EnumNameTagVisibility.getMethod("values").invoke(null))[0] : ((Object[])nms.EnumNameTagVisibility.getMethod("values").invoke(null))[1]);
		if (nms.EnumTeamPush != null && packet.getCollisionRule() != null) nms.ScoreboardTeam_setCollisionRule.invoke(team, packet.getCollisionRule().equals("always") ? ((Object[])nms.EnumTeamPush.getMethod("values").invoke(null))[0] : ((Object[])nms.EnumTeamPush.getMethod("values").invoke(null))[1]);
		return team;
	}

	/**
	 * Builds entity destroy packet with given parameter
	 * @param id - entity id to destroy
	 * @return destroy packet
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public Object buildEntityDestroyPacket(int id) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (nms.getMinorVersion() >= 17) {
			return nms.newPacketPlayOutEntityDestroy.newInstance(id);
		}
		return nms.newPacketPlayOutEntityDestroy.newInstance((int[]) new int[] {id});
	}

	/**
	 * Builds entity metadata packet with given parameters
	 * @param entityId - entity id
	 * @param dataWatcher - datawatcher
	 * @return metadata packet
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public Object buildEntityMetadataPacket(int entityId, DataWatcher dataWatcher) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return nms.newPacketPlayOutEntityMetadata.newInstance(entityId, dataWatcher.toNMS(), true);
	}

	/**
	 * Builds entity spawn packet with given parameters
	 * @param entityId - entity id
	 * @param uuid - entity uuid
	 * @param entityType - entity type
	 * @param loc - location to spawn at
	 * @param dataWatcher - datawatcher
	 * @return entity spawn packet
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws InvocationTargetException 
	 * @throws InstantiationException 
	 */
	public Object buildEntitySpawnPacket(int entityId, UUID uuid, EntityType entityType, Location loc, DataWatcher dataWatcher) throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException {
		Object nmsPacket = nms.newPacketPlayOutSpawnEntityLiving.newInstance(nms.getHandle.invoke(Bukkit.getOnlinePlayers().iterator().next()));
		nms.PacketPlayOutSpawnEntityLiving_ENTITYID.set(nmsPacket, entityId);
		nms.PacketPlayOutSpawnEntityLiving_ENTITYTYPE.set(nmsPacket, entityIds.get(entityType));
		nms.PacketPlayOutSpawnEntityLiving_YAW.set(nmsPacket, (byte)(loc.getYaw() * 256.0f / 360.0f));
		nms.PacketPlayOutSpawnEntityLiving_PITCH.set(nmsPacket, (byte)(loc.getPitch() * 256.0f / 360.0f));
		if (nms.PacketPlayOutSpawnEntityLiving_DATAWATCHER != null) {
			nms.PacketPlayOutSpawnEntityLiving_DATAWATCHER.set(nmsPacket, dataWatcher.toNMS());
		}
		if (nms.getMinorVersion() >= 9) {
			nms.PacketPlayOutSpawnEntityLiving_UUID.set(nmsPacket, uuid);
			nms.PacketPlayOutSpawnEntityLiving_X.set(nmsPacket, loc.getX());
			nms.PacketPlayOutSpawnEntityLiving_Y.set(nmsPacket, loc.getY());
			nms.PacketPlayOutSpawnEntityLiving_Z.set(nmsPacket, loc.getZ());
		} else {
			nms.PacketPlayOutSpawnEntityLiving_X.set(nmsPacket, floor(loc.getX()*32));
			nms.PacketPlayOutSpawnEntityLiving_Y.set(nmsPacket, floor(loc.getY()*32));
			nms.PacketPlayOutSpawnEntityLiving_Z.set(nmsPacket, floor(loc.getZ()*32));
		}
		return nmsPacket;
	}

	/**
	 * Builds entity teleport packet with given parameters
	 * @param entityId - entity id
	 * @param location - location to teleport to
	 * @return entity teleport packet
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws InvocationTargetException 
	 * @throws InstantiationException 
	 */
	public Object buildEntityTeleportPacket(int entityId, Location location) throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException {
		Object nmsPacket = nms.newPacketPlayOutEntityTeleport.newInstance(nms.getHandle.invoke(Bukkit.getOnlinePlayers().iterator().next()));
		nms.PacketPlayOutEntityTeleport_ENTITYID.set(nmsPacket, entityId);
		if (nms.getMinorVersion() >= 9) {
			nms.PacketPlayOutEntityTeleport_X.set(nmsPacket, location.getX());
			nms.PacketPlayOutEntityTeleport_Y.set(nmsPacket, location.getY());
			nms.PacketPlayOutEntityTeleport_Z.set(nmsPacket, location.getZ());
		} else {
			nms.PacketPlayOutEntityTeleport_X.set(nmsPacket, floor(location.getX()*32));
			nms.PacketPlayOutEntityTeleport_Y.set(nmsPacket, floor(location.getY()*32));
			nms.PacketPlayOutEntityTeleport_Z.set(nmsPacket, floor(location.getZ()*32));
		}
		nms.PacketPlayOutEntityTeleport_YAW.set(nmsPacket, (byte) (location.getYaw()/360*256));
		nms.PacketPlayOutEntityTeleport_PITCH.set(nmsPacket, (byte) (location.getPitch()/360*256));
		return nmsPacket;
	}

	@Override
	public PacketPlayOutPlayerInfo readPlayerInfo(Object nmsPacket, ProtocolVersion clientVersion) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (nms.getMinorVersion() < 8) return null;
		EnumPlayerInfoAction action = EnumPlayerInfoAction.valueOf(nms.PacketPlayOutPlayerInfo_ACTION.get(nmsPacket).toString());
		List<PlayerInfoData> listData = new ArrayList<>();
		for (Object nmsData : (List<?>) nms.PacketPlayOutPlayerInfo_PLAYERS.get(nmsPacket)) {
			Object nmsGamemode = nms.PlayerInfoData_getGamemode.invoke(nmsData);
			EnumGamemode gamemode = (nmsGamemode == null) ? null : EnumGamemode.valueOf(nmsGamemode.toString());
			GameProfile profile = (GameProfile) nms.PlayerInfoData_getProfile.invoke(nmsData);
			Object nmsComponent = nms.PlayerInfoData_getDisplayName.invoke(nmsData);
			IChatBaseComponent listName = nmsComponent == null ? null : fromNMSComponent(nmsComponent);
			listData.add(new PlayerInfoData(profile.getName(), profile.getId(), profile.getProperties(), (int)nms.PlayerInfoData_getLatency.invoke(nmsData), gamemode, listName));
		}
		return new PacketPlayOutPlayerInfo(action, listData);
	}

	@Override
	public PacketPlayOutScoreboardObjective readObjective(Object nmsPacket, ProtocolVersion clientVersion) throws IllegalArgumentException, IllegalAccessException {
		String objective = (String) nms.PacketPlayOutScoreboardObjective_OBJECTIVENAME.get(nmsPacket);
		String displayName;
		if (nms.getMinorVersion() >= 13) {
			Object component = nms.PacketPlayOutScoreboardObjective_DISPLAYNAME.get(nmsPacket);
			IChatBaseComponent c = component == null ? null : fromNMSComponent(component);
			displayName = c == null ? null : c.toLegacyText();
		} else {
			displayName = (String) nms.PacketPlayOutScoreboardObjective_DISPLAYNAME.get(nmsPacket);
		}
		EnumScoreboardHealthDisplay renderType = null;
		if (nms.PacketPlayOutScoreboardObjective_RENDERTYPE != null) {
			Object nmsRender = nms.PacketPlayOutScoreboardObjective_RENDERTYPE.get(nmsPacket);
			if (nmsRender != null) {
				renderType = EnumScoreboardHealthDisplay.valueOf(nmsRender.toString());
			}
		}
		int method = nms.PacketPlayOutScoreboardObjective_METHOD.getInt(nmsPacket);
		return new PacketPlayOutScoreboardObjective(method, objective, displayName, renderType);
	}

	@Override
	public PacketPlayOutScoreboardDisplayObjective readDisplayObjective(Object nmsPacket, ProtocolVersion clientVersion) throws IllegalArgumentException, IllegalAccessException {
		return new PacketPlayOutScoreboardDisplayObjective(
			nms.PacketPlayOutScoreboardDisplayObjective_POSITION.getInt(nmsPacket),
			(String) nms.PacketPlayOutScoreboardDisplayObjective_OBJECTIVENAME.get(nmsPacket)
		);
	}

	/**
	 * A method yoinked from minecraft code used to convert double to int
	 * @param paramDouble double value
	 * @return int value
	 */
	private int floor(double paramDouble){
		int i = (int)paramDouble;
		return paramDouble < i ? i - 1 : i;
	}

	/**
	 * Converts json string into a component
	 * @param json json as string
	 * @return NMS component
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	public Object stringToComponent(String json) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (json == null) return null;
		return nms.ChatSerializer_DESERIALIZE.invoke(null, json);
	}

	/**
	 * Converts minecraft IChatBaseComponent into TAB's component class. Currently does not support hover event.
	 * @param component - component to convert
	 * @return converted component
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public IChatBaseComponent fromNMSComponent(Object component) throws IllegalArgumentException, IllegalAccessException {
		long time = System.nanoTime();
		IChatBaseComponent obj = fromNMSComponent0(component);
		TAB.getInstance().getCPUManager().addMethodTime("fromNMSComponent", System.nanoTime()-time);
		return obj;
	}

	//separate method to prevent extras counting cpu again due to recursion and finally showing higher usage than real
	@SuppressWarnings("rawtypes")
	public IChatBaseComponent fromNMSComponent0(Object component) throws IllegalArgumentException, IllegalAccessException {
		if (!nms.ChatComponentText.isInstance(component)) return null; //paper
		IChatBaseComponent chat = new IChatBaseComponent((String) nms.ChatComponentText_text.get(component));
		Object modifier = nms.ChatBaseComponent_modifier.get(component);
		if (modifier != null) {
			Object color = nms.ChatModifier_color.get(modifier);
			if (color != null) {
				if (nms.getMinorVersion() >= 16) {
					String name = (String) nms.ChatHexColor_name.get(color);
					if (name != null) {
						//legacy code
						chat.setColor(new TextColor(EnumChatFormat.valueOf(name.toUpperCase())));
					} else {
						int rgb = (int) nms.ChatHexColor_rgb.get(color);
						chat.setColor(new TextColor((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF));
					}
				} else {
					chat.setColor(new TextColor(EnumChatFormat.valueOf(((Enum)color).name())));
				}
			}
			chat.setBold((Boolean) nms.ChatModifier_bold.get(modifier));
			chat.setItalic((Boolean) nms.ChatModifier_italic.get(modifier));
			chat.setObfuscated((Boolean) nms.ChatModifier_obfuscated.get(modifier));
			chat.setStrikethrough((Boolean) nms.ChatModifier_strikethrough.get(modifier));
			chat.setUnderlined((Boolean) nms.ChatModifier_underlined.get(modifier));
			Object clickEvent = nms.ChatModifier_clickEvent.get(modifier);
			if (clickEvent != null) {
				chat.onClick(ClickAction.valueOf(nms.ChatClickable_action.get(clickEvent).toString().toUpperCase()), (String) nms.ChatClickable_value.get(clickEvent));
			}
/*			Object hoverEvent = nms.ChatModifier_hoverEvent.get(modifier);
			if (hoverEvent != null) {
				chat.onHover(HoverAction.fromString(nms.ChatHoverable_action.get(hoverEvent).toString()), fromNMSComponent(nms.ChatHoverable_value.get(hoverEvent)));
			}*/
		}
		for (Object extra : (List<Object>) nms.ChatBaseComponent_extra.get(component)) {
			chat.addExtra(fromNMSComponent0(extra));
		}
		return chat;
	}

	/**
	 * Converts TAB's IChatBaseComponent into minecraft's component. Currently does not support hover event.
	 * @param component - component to convert
	 * @param clientVersion - client version used to decide RGB conversion
	 * @return converted component
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public Object toNMSComponent(IChatBaseComponent component, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		long time = System.nanoTime();
		Object obj = toNMSComponent0(component, clientVersion);
		TAB.getInstance().getCPUManager().addMethodTime("toNMSComponent", System.nanoTime()-time);
		return obj;
	}

	//separate method to prevent extras counting cpu again due to recursion and finally showing higher usage than real
	private Object toNMSComponent0(IChatBaseComponent component, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (component == null) return null;
		Object chat = nms.newChatComponentText.newInstance(component.getText());
		Object modifier;
		if (nms.getMinorVersion() >= 16) {
			Object color = null;
			if (component.getColor() != null) {
				if (clientVersion.getMinorVersion() >= 16) {
					color = nms.ChatHexColor_ofInt.invoke(null, (component.getColor().getRed() << 16) + (component.getColor().getGreen() << 8) + component.getColor().getBlue());
				} else {
					color = nms.ChatHexColor_ofString.invoke(null, component.getColor().getLegacyColor().toString().toLowerCase());
				}
			}
			modifier = nms.newChatModifier.newInstance(
				color,
				component.getBold(),
				component.getItalic(),
				component.getUnderlined(),
				component.getStrikethrough(),
				component.getObfuscated(),
				component.getClickAction() == null ? null : nms.newChatClickable.newInstance(nms.EnumClickAction_a.invoke(null, component.getClickAction().toString().toLowerCase()), component.getClickValue().toString()), 
				null, //component.getHoverAction() == null ? null : nms.newChatHoverable.newInstance(nms.EnumHoverAction_a.invoke(null, component.getHoverAction().toString().toLowerCase()), stringToComponent(component.getHoverValue().toString())), 
				null, null
					);
		} else {
			modifier = nms.newChatModifier.newInstance();
			if (component.getClickAction() != null){
				nms.ChatModifier_clickEvent.set(modifier, nms.newChatClickable.newInstance(nms.EnumClickAction_a.invoke(null, component.getClickAction().toString().toLowerCase()), component.getClickValue().toString()));
			}
/*			if (component.getHoverAction() != null) {
				nms.ChatModifier_hoverEvent.set(modifier, nms.newChatHoverable.newInstance(nms.EnumHoverAction_a.invoke(null, (component.getHoverAction().toString().toLowerCase())), stringToComponent(component.getHoverValue().toString())));
			}*/
			if (component.getColor() != null) nms.ChatModifier_color.set(modifier, Enum.valueOf(nms.EnumChatFormat, component.getColor().getLegacyColor().toString()));
			if (component.isBold()) nms.ChatModifier_bold.set(modifier, true);
			if (component.isItalic()) nms.ChatModifier_italic.set(modifier, true);
			if (component.isUnderlined()) nms.ChatModifier_underlined.set(modifier, true);
			if (component.isStrikethrough()) nms.ChatModifier_strikethrough.set(modifier, true);
			if (component.isObfuscated()) nms.ChatModifier_obfuscated.set(modifier, true);
		}
		nms.ChatBaseComponent_modifier.set(chat, modifier);
		for (IChatBaseComponent extra : component.getExtra()) {
			nms.ChatComponentText_addSibling.invoke(chat, toNMSComponent0(extra, clientVersion));
		}
		return chat;
	}

	private Object newScoreboardObjective(String objectiveName) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (nms.getMinorVersion() >= 13) {
			return nms.newScoreboardObjective.newInstance(null, objectiveName, null, stringToComponent("{\"text\":\"\"}"), null);
		}
		return nms.newScoreboardObjective.newInstance(null, objectiveName, nms.getFields(nms.IScoreboardCriteria, nms.IScoreboardCriteria).get(0).get(null));
	}
}