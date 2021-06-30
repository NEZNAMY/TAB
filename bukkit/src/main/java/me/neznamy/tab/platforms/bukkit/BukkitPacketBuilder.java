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

@SuppressWarnings({"unchecked", "rawtypes"})
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

		int entityId = packet.getId().hashCode();
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
			return nms.getConstructor("PacketPlayOutChat").newInstance(component, ((Object[])nms.getClass("ChatMessageType").getMethod("values").invoke(null))[packet.getType().ordinal()], UUID.randomUUID());
		}
		if (nms.getMinorVersion() >= 12) {
			return nms.getConstructor("PacketPlayOutChat").newInstance(component, Enum.valueOf((Class<Enum>) nms.getClass("ChatMessageType"), packet.getType().toString()));
		}
		if (nms.getMinorVersion() >= 8) {
			return nms.getConstructor("PacketPlayOutChat").newInstance(component, (byte) packet.getType().ordinal());
		}
		if (nms.getMinorVersion() == 7) {
			return nms.getConstructor("PacketPlayOutChat").newInstance(component);
		}
		throw new IllegalStateException("Not supported on <1.7");
	}

	@Override
	public Object build(PacketPlayOutPlayerInfo packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NegativeArraySizeException {
		if (nms.getMinorVersion() < 8) return null;
		Object nmsPacket = nms.getConstructor("PacketPlayOutPlayerInfo").newInstance(((Object[])nms.getClass("EnumPlayerInfoAction").getMethod("values").invoke(null))[packet.getAction().ordinal()], Array.newInstance(nms.getClass("EntityPlayer"), 0));
		List<Object> items = new ArrayList<>();
		for (PlayerInfoData data : packet.getEntries()) {
			GameProfile profile = new GameProfile(data.getUniqueId(), data.getName());
			
			if (data.getSkin() != null) profile.getProperties().putAll((PropertyMap) data.getSkin());
			List<Object> parameters = new ArrayList<>();
			if (nms.getConstructor("PlayerInfoData").getParameterCount() == 5) {
				parameters.add(nmsPacket);
			}
			parameters.add(profile);
			parameters.add(data.getLatency());
			Object[] values = (Object[]) nms.getClass("EnumGamemode").getMethod("values").invoke(null);
			parameters.add(data.getGameMode() == null ? null : values[values.length-EnumGamemode.values().length+data.getGameMode().ordinal()]); //not_set was removed in 1.17
			parameters.add(data.getDisplayName() == null ? null : toNMSComponent(data.getDisplayName(), clientVersion));
			items.add(nms.getConstructor("PlayerInfoData").newInstance(parameters.toArray()));
		}
		nms.getField("PacketPlayOutPlayerInfo_PLAYERS").set(nmsPacket, items);
		return nmsPacket;
	}

	@Override
	public Object build(PacketPlayOutPlayerListHeaderFooter packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (nms.getClass("PacketPlayOutPlayerListHeaderFooter") == null) return null;
		if (nms.getMinorVersion() >= 17) {
			return nms.getConstructor("PacketPlayOutPlayerListHeaderFooter").newInstance(toNMSComponent(packet.getHeader(), clientVersion), toNMSComponent(packet.getFooter(), clientVersion));
		}
		Object nmsPacket = nms.getConstructor("PacketPlayOutPlayerListHeaderFooter").newInstance();
		nms.getField("PacketPlayOutPlayerListHeaderFooter_HEADER").set(nmsPacket, toNMSComponent(packet.getHeader(), clientVersion));
		nms.getField("PacketPlayOutPlayerListHeaderFooter_FOOTER").set(nmsPacket, toNMSComponent(packet.getFooter(), clientVersion));
		return nmsPacket;
	}

	@Override
	public Object build(PacketPlayOutScoreboardDisplayObjective packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		return nms.getConstructor("PacketPlayOutScoreboardDisplayObjective").newInstance(packet.getSlot(), newScoreboardObjective(packet.getObjectiveName()));
	}

	@Override
	public Object build(PacketPlayOutScoreboardObjective packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		String displayName = clientVersion.getMinorVersion() < 13 ? cutTo(packet.getDisplayName(), 32) : packet.getDisplayName();
		if (nms.getMinorVersion() >= 13) {
			return nms.getConstructor("PacketPlayOutScoreboardObjective").newInstance(nms.getConstructor("ScoreboardObjective").newInstance(null, packet.getObjectiveName(), null, 
					toNMSComponent(IChatBaseComponent.optimizedComponent(displayName), clientVersion), 
					packet.getRenderType() == null ? null : ((Object[])nms.getClass("EnumScoreboardHealthDisplay").getMethod("values").invoke(null))[packet.getRenderType().ordinal()]), 
					packet.getMethod());
		}
		
		Object nmsPacket = nms.getConstructor("PacketPlayOutScoreboardObjective").newInstance();
		nms.getField("PacketPlayOutScoreboardObjective_OBJECTIVENAME").set(nmsPacket, packet.getObjectiveName());
		nms.getField("PacketPlayOutScoreboardObjective_DISPLAYNAME").set(nmsPacket, displayName);
		if (nms.getField("PacketPlayOutScoreboardObjective_RENDERTYPE") != null && packet.getRenderType() != null) {
			nms.getField("PacketPlayOutScoreboardObjective_RENDERTYPE").set(nmsPacket, Enum.valueOf((Class<Enum>) nms.getClass("EnumScoreboardHealthDisplay"), packet.getRenderType().toString()));
		}
		nms.getField("PacketPlayOutScoreboardObjective_METHOD").set(nmsPacket, packet.getMethod());
		return nmsPacket;
	}

	@Override
	public Object build(PacketPlayOutScoreboardScore packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if (nms.getMinorVersion() >= 13) {
			return nms.getConstructor("PacketPlayOutScoreboardScore_1_13").newInstance(((Object[])nms.getClass("EnumScoreboardAction").getMethod("values").invoke(null))[packet.getAction().ordinal()], packet.getObjectiveName(), packet.getPlayer(), packet.getScore());
		}
		if (packet.getAction() == PacketPlayOutScoreboardScore.Action.REMOVE) {
			return nms.getConstructor("PacketPlayOutScoreboardScore_String").newInstance(packet.getPlayer());
		}
		Object score = nms.getConstructor("ScoreboardScore").newInstance(nms.getConstructor("Scoreboard").newInstance(), newScoreboardObjective(packet.getObjectiveName()), packet.getPlayer());
		nms.getMethod("ScoreboardScore_setScore").invoke(score, packet.getScore());
		if (nms.getMinorVersion() >= 8) {
			return nms.getConstructor("PacketPlayOutScoreboardScore").newInstance(score);
		}
		return nms.getConstructor("PacketPlayOutScoreboardScore").newInstance(score, 0);
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
				return nms.getMethod("PacketPlayOutScoreboardTeam_ofBoolean").invoke(null, team, true);
			case 1:
				return nms.getMethod("PacketPlayOutScoreboardTeam_of").invoke(null, team);
			case 2:
				return nms.getMethod("PacketPlayOutScoreboardTeam_ofBoolean").invoke(null, team, false);
			case 3:
				return nms.getMethod("PacketPlayOutScoreboardTeam_ofString").invoke(null, team, packet.getPlayers().toArray(new String[0])[0], ((Object[])nms.getClass("PacketPlayOutScoreboardTeam_a").getMethod("values").invoke(null))[0]);
			case 4:
				return nms.getMethod("PacketPlayOutScoreboardTeam_ofString").invoke(null, team, packet.getPlayers().toArray(new String[0])[0], ((Object[])nms.getClass("PacketPlayOutScoreboardTeam_a").getMethod("values").invoke(null))[1]);
			default:
				throw new IllegalArgumentException("Invalid action: " + packet.getMethod());
			}
		}
		return nms.getConstructor("PacketPlayOutScoreboardTeam").newInstance(team, packet.getMethod());
	}
	
	private Object createTeamModern(PacketPlayOutScoreboardTeam packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		String prefix = packet.getPlayerPrefix();
		String suffix = packet.getPlayerSuffix();
		if (clientVersion.getMinorVersion() < 13) {
			prefix = cutTo(prefix, 16);
			suffix = cutTo(suffix, 16);
		}
		Object team = nms.getConstructor("ScoreboardTeam").newInstance(nms.getConstructor("Scoreboard").newInstance(), packet.getName());
		((Collection<String>)nms.getMethod("ScoreboardTeam_getPlayerNameSet").invoke(team)).addAll(packet.getPlayers());
		if (prefix != null && prefix.length() > 0) nms.getMethod("ScoreboardTeam_setPrefix").invoke(team, toNMSComponent(IChatBaseComponent.optimizedComponent(prefix), clientVersion));
		if (suffix != null && suffix.length() > 0) nms.getMethod("ScoreboardTeam_setSuffix").invoke(team, toNMSComponent(IChatBaseComponent.optimizedComponent(suffix), clientVersion));
		EnumChatFormat format = packet.getColor() != null ? packet.getColor() : EnumChatFormat.lastColorsOf(prefix);
		nms.getMethod("ScoreboardTeam_setColor").invoke(team, ((Object[])nms.getClass("EnumChatFormat").getMethod("values").invoke(null))[format.ordinal()]);
		if (packet.getNametagVisibility() != null) nms.getMethod("ScoreboardTeam_setNameTagVisibility").invoke(team, packet.getNametagVisibility().equals("always") ? ((Object[])nms.getClass("EnumNameTagVisibility").getMethod("values").invoke(null))[0] : ((Object[])nms.getClass("EnumNameTagVisibility").getMethod("values").invoke(null))[1]);
		if (packet.getCollisionRule() != null) nms.getMethod("ScoreboardTeam_setCollisionRule").invoke(team, packet.getCollisionRule().equals("always") ? ((Object[])nms.getClass("EnumTeamPush").getMethod("values").invoke(null))[0] : ((Object[])nms.getClass("EnumTeamPush").getMethod("values").invoke(null))[1]);
		return team;
	}
	
	private Object createTeamLegacy(PacketPlayOutScoreboardTeam packet, ProtocolVersion clientVersion) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, NoSuchMethodException, SecurityException {
		String prefix = packet.getPlayerPrefix();
		String suffix = packet.getPlayerSuffix();
		if (clientVersion.getMinorVersion() < 13) {
			prefix = cutTo(prefix, 16);
			suffix = cutTo(suffix, 16);
		}
		Object team = nms.getConstructor("ScoreboardTeam").newInstance(nms.getConstructor("Scoreboard").newInstance(), packet.getName());
		((Collection<String>)nms.getMethod("ScoreboardTeam_getPlayerNameSet").invoke(team)).addAll(packet.getPlayers());
		if (prefix != null) nms.getMethod("ScoreboardTeam_setPrefix").invoke(team, prefix);
		if (suffix != null) nms.getMethod("ScoreboardTeam_setSuffix").invoke(team, suffix);
		if (nms.getClass("EnumNameTagVisibility") != null && packet.getNametagVisibility() != null) nms.getMethod("ScoreboardTeam_setNameTagVisibility").invoke(team, packet.getNametagVisibility().equals("always") ? ((Object[])nms.getClass("EnumNameTagVisibility").getMethod("values").invoke(null))[0] : ((Object[])nms.getClass("EnumNameTagVisibility").getMethod("values").invoke(null))[1]);
		if (nms.getClass("EnumTeamPush") != null && packet.getCollisionRule() != null) nms.getMethod("ScoreboardTeam_setCollisionRule").invoke(team, packet.getCollisionRule().equals("always") ? ((Object[])nms.getClass("EnumTeamPush").getMethod("values").invoke(null))[0] : ((Object[])nms.getClass("EnumTeamPush").getMethod("values").invoke(null))[1]);
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
			return nms.getConstructor("PacketPlayOutEntityDestroy").newInstance(id);
		}
		return nms.getConstructor("PacketPlayOutEntityDestroy").newInstance(new int[] {id});
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
		return nms.getConstructor("PacketPlayOutEntityMetadata").newInstance(entityId, dataWatcher.toNMS(), true);
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
		Object nmsPacket = nms.getConstructor("PacketPlayOutSpawnEntityLiving").newInstance(nms.getMethod("getHandle").invoke(Bukkit.getOnlinePlayers().iterator().next()));
		nms.getField("PacketPlayOutSpawnEntityLiving_ENTITYID").set(nmsPacket, entityId);
		nms.getField("PacketPlayOutSpawnEntityLiving_ENTITYTYPE").set(nmsPacket, entityIds.get(entityType));
		nms.getField("PacketPlayOutSpawnEntityLiving_YAW").set(nmsPacket, (byte)(loc.getYaw() * 256.0f / 360.0f));
		nms.getField("PacketPlayOutSpawnEntityLiving_PITCH").set(nmsPacket, (byte)(loc.getPitch() * 256.0f / 360.0f));
		if (nms.getField("PacketPlayOutSpawnEntityLiving_DATAWATCHER") != null) {
			nms.getField("PacketPlayOutSpawnEntityLiving_DATAWATCHER").set(nmsPacket, dataWatcher.toNMS());
		}
		if (nms.getMinorVersion() >= 9) {
			nms.getField("PacketPlayOutSpawnEntityLiving_UUID").set(nmsPacket, uuid);
			nms.getField("PacketPlayOutSpawnEntityLiving_X").set(nmsPacket, loc.getX());
			nms.getField("PacketPlayOutSpawnEntityLiving_Y").set(nmsPacket, loc.getY());
			nms.getField("PacketPlayOutSpawnEntityLiving_Z").set(nmsPacket, loc.getZ());
		} else {
			nms.getField("PacketPlayOutSpawnEntityLiving_X").set(nmsPacket, floor(loc.getX()*32));
			nms.getField("PacketPlayOutSpawnEntityLiving_Y").set(nmsPacket, floor(loc.getY()*32));
			nms.getField("PacketPlayOutSpawnEntityLiving_Z").set(nmsPacket, floor(loc.getZ()*32));
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
		Object nmsPacket = nms.getConstructor("PacketPlayOutEntityTeleport").newInstance(nms.getMethod("getHandle").invoke(Bukkit.getOnlinePlayers().iterator().next()));
		nms.getField("PacketPlayOutEntityTeleport_ENTITYID").set(nmsPacket, entityId);
		if (nms.getMinorVersion() >= 9) {
			nms.getField("PacketPlayOutEntityTeleport_X").set(nmsPacket, location.getX());
			nms.getField("PacketPlayOutEntityTeleport_Y").set(nmsPacket, location.getY());
			nms.getField("PacketPlayOutEntityTeleport_Z").set(nmsPacket, location.getZ());
		} else {
			nms.getField("PacketPlayOutEntityTeleport_X").set(nmsPacket, floor(location.getX()*32));
			nms.getField("PacketPlayOutEntityTeleport_Y").set(nmsPacket, floor(location.getY()*32));
			nms.getField("PacketPlayOutEntityTeleport_Z").set(nmsPacket, floor(location.getZ()*32));
		}
		nms.getField("PacketPlayOutEntityTeleport_YAW").set(nmsPacket, (byte) (location.getYaw()/360*256));
		nms.getField("PacketPlayOutEntityTeleport_PITCH").set(nmsPacket, (byte) (location.getPitch()/360*256));
		return nmsPacket;
	}

	@Override
	public PacketPlayOutPlayerInfo readPlayerInfo(Object nmsPacket, ProtocolVersion clientVersion) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (nms.getMinorVersion() < 8) return null;
		EnumPlayerInfoAction action = EnumPlayerInfoAction.valueOf(nms.getField("PacketPlayOutPlayerInfo_ACTION").get(nmsPacket).toString());
		List<PlayerInfoData> listData = new ArrayList<>();
		for (Object nmsData : (List<?>) nms.getField("PacketPlayOutPlayerInfo_PLAYERS").get(nmsPacket)) {
			Object nmsGamemode = nms.getMethod("PlayerInfoData_getGamemode").invoke(nmsData);
			EnumGamemode gamemode = (nmsGamemode == null) ? null : EnumGamemode.valueOf(nmsGamemode.toString());
			GameProfile profile = (GameProfile) nms.getMethod("PlayerInfoData_getProfile").invoke(nmsData);
			Object nmsComponent = nms.getMethod("PlayerInfoData_getDisplayName").invoke(nmsData);
			IChatBaseComponent listName = nmsComponent == null ? null : fromNMSComponent(nmsComponent);
			listData.add(new PlayerInfoData(profile.getName(), profile.getId(), profile.getProperties(), (int)nms.getMethod("PlayerInfoData_getLatency").invoke(nmsData), gamemode, listName));
		}
		return new PacketPlayOutPlayerInfo(action, listData);
	}

	@Override
	public PacketPlayOutScoreboardObjective readObjective(Object nmsPacket, ProtocolVersion clientVersion) throws IllegalArgumentException, IllegalAccessException {
		String objective = (String) nms.getField("PacketPlayOutScoreboardObjective_OBJECTIVENAME").get(nmsPacket);
		String displayName;
		if (nms.getMinorVersion() >= 13) {
			Object component = nms.getField("PacketPlayOutScoreboardObjective_DISPLAYNAME").get(nmsPacket);
			IChatBaseComponent c = component == null ? null : fromNMSComponent(component);
			displayName = c == null ? null : c.toLegacyText();
		} else {
			displayName = (String) nms.getField("PacketPlayOutScoreboardObjective_DISPLAYNAME").get(nmsPacket);
		}
		EnumScoreboardHealthDisplay renderType = null;
		if (nms.getField("PacketPlayOutScoreboardObjective_RENDERTYPE") != null) {
			Object nmsRender = nms.getField("PacketPlayOutScoreboardObjective_RENDERTYPE").get(nmsPacket);
			if (nmsRender != null) {
				renderType = EnumScoreboardHealthDisplay.valueOf(nmsRender.toString());
			}
		}
		int method = nms.getField("PacketPlayOutScoreboardObjective_METHOD").getInt(nmsPacket);
		return new PacketPlayOutScoreboardObjective(method, objective, displayName, renderType);
	}

	@Override
	public PacketPlayOutScoreboardDisplayObjective readDisplayObjective(Object nmsPacket, ProtocolVersion clientVersion) throws IllegalArgumentException, IllegalAccessException {
		return new PacketPlayOutScoreboardDisplayObjective(
			nms.getField("PacketPlayOutScoreboardDisplayObjective_POSITION").getInt(nmsPacket),
			(String) nms.getField("PacketPlayOutScoreboardDisplayObjective_OBJECTIVENAME").get(nmsPacket)
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
		return nms.getMethod("ChatSerializer_DESERIALIZE").invoke(null, json);
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
	public IChatBaseComponent fromNMSComponent0(Object component) throws IllegalArgumentException, IllegalAccessException {
		if (!nms.getClass("ChatComponentText").isInstance(component)) return null; //paper
		IChatBaseComponent chat = new IChatBaseComponent((String) nms.getField("ChatComponentText_text").get(component));
		Object modifier = nms.getField("ChatBaseComponent_modifier").get(component);
		if (modifier != null) {
			Object color = nms.getField("ChatModifier_color").get(modifier);
			if (color != null) {
				if (nms.getMinorVersion() >= 16) {
					String name = (String) nms.getField("ChatHexColor_name").get(color);
					if (name != null) {
						//legacy code
						chat.setColor(new TextColor(EnumChatFormat.valueOf(name.toUpperCase())));
					} else {
						int rgb = (int) nms.getField("ChatHexColor_rgb").get(color);
						chat.setColor(new TextColor((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF));
					}
				} else {
					chat.setColor(new TextColor(EnumChatFormat.valueOf(((Enum)color).name())));
				}
			}
			chat.setBold((Boolean) nms.getField("ChatModifier_bold").get(modifier));
			chat.setItalic((Boolean) nms.getField("ChatModifier_italic").get(modifier));
			chat.setObfuscated((Boolean) nms.getField("ChatModifier_obfuscated").get(modifier));
			chat.setStrikethrough((Boolean) nms.getField("ChatModifier_strikethrough").get(modifier));
			chat.setUnderlined((Boolean) nms.getField("ChatModifier_underlined").get(modifier));
			Object clickEvent = nms.getField("ChatModifier_clickEvent").get(modifier);
			if (clickEvent != null) {
				chat.onClick(ClickAction.valueOf(nms.getField("ChatClickable_action").get(clickEvent).toString().toUpperCase()), (String) nms.getField("ChatClickable_value").get(clickEvent));
			}
/*			Object hoverEvent = nms.ChatModifier_hoverEvent.get(modifier);
			if (hoverEvent != null) {
				chat.onHover(HoverAction.fromString(nms.ChatHoverable_action.get(hoverEvent).toString()), fromNMSComponent(nms.ChatHoverable_value.get(hoverEvent)));
			}*/
		}
		for (Object extra : (List<Object>) nms.getField("ChatBaseComponent_extra").get(component)) {
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
		Object chat = nms.getConstructor("ChatComponentText").newInstance(component.getText());
		Object modifier;
		if (nms.getMinorVersion() >= 16) {
			Object color = null;
			if (component.getColor() != null) {
				if (clientVersion.getMinorVersion() >= 16) {
					color = nms.getMethod("ChatHexColor_ofInt").invoke(null, (component.getColor().getRed() << 16) + (component.getColor().getGreen() << 8) + component.getColor().getBlue());
				} else {
					color = nms.getMethod("ChatHexColor_ofString").invoke(null, component.getColor().getLegacyColor().toString().toLowerCase());
				}
			}
			modifier = nms.getConstructor("ChatModifier").newInstance(
				color,
				component.getBold(),
				component.getItalic(),
				component.getUnderlined(),
				component.getStrikethrough(),
				component.getObfuscated(),
				component.getClickAction() == null ? null : nms.getConstructor("ChatClickable").newInstance(nms.getMethod("EnumClickAction_a").invoke(null, component.getClickAction().toString().toLowerCase()), component.getClickValue().toString()), 
				null, //component.getHoverAction() == null ? null : nms.getConstructor("ChatHoverable.newInstance(nms.EnumHoverAction_a.invoke(null, component.getHoverAction().toString().toLowerCase()), stringToComponent(component.getHoverValue().toString())), 
				null, null
					);
		} else {
			modifier = nms.getConstructor("ChatModifier").newInstance();
			if (component.getClickAction() != null){
				nms.getField("ChatModifier_clickEvent").set(modifier, nms.getConstructor("ChatClickable").newInstance(nms.getMethod("EnumClickAction_a").invoke(null, component.getClickAction().toString().toLowerCase()), component.getClickValue().toString()));
			}
/*			if (component.getHoverAction() != null) {
				nms.ChatModifier_hoverEvent.set(modifier, nms.getConstructor("ChatHoverable").newInstance(nms.EnumHoverAction_a.invoke(null, (component.getHoverAction().toString().toLowerCase())), stringToComponent(component.getHoverValue().toString())));
			}*/
			if (component.getColor() != null) nms.getField("ChatModifier_color").set(modifier, Enum.valueOf((Class<Enum>) nms.getClass("EnumChatFormat"), component.getColor().getLegacyColor().toString()));
			if (component.isBold()) nms.getField("ChatModifier_bold").set(modifier, true);
			if (component.isItalic()) nms.getField("ChatModifier_italic").set(modifier, true);
			if (component.isUnderlined()) nms.getField("ChatModifier_underlined").set(modifier, true);
			if (component.isStrikethrough()) nms.getField("ChatModifier_strikethrough").set(modifier, true);
			if (component.isObfuscated()) nms.getField("ChatModifier_obfuscated").set(modifier, true);
		}
		nms.getField("ChatBaseComponent_modifier").set(chat, modifier);
		for (IChatBaseComponent extra : component.getExtra()) {
			nms.getMethod("ChatComponentText_addSibling").invoke(chat, toNMSComponent0(extra, clientVersion));
		}
		return chat;
	}

	private Object newScoreboardObjective(String objectiveName) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (nms.getMinorVersion() >= 13) {
			return nms.getConstructor("ScoreboardObjective").newInstance(null, objectiveName, null, stringToComponent("{\"text\":\"\"}"), null);
		}
		return nms.getConstructor("ScoreboardObjective").newInstance(null, objectiveName, nms.getFields(nms.getClass("IScoreboardCriteria"), nms.getClass("IScoreboardCriteria")).get(0).get(null));
	}
}