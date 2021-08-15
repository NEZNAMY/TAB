package me.neznamy.tab.platforms.bukkit;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.json.simple.parser.ParseException;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.gson.JsonParser;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.chat.ChatClickable.EnumClickAction;
import me.neznamy.tab.api.chat.ChatComponentEntity;
import me.neznamy.tab.api.chat.ChatHoverable.EnumHoverAction;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.chat.TextColor;
import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.api.protocol.PacketPlayOutBoss;
import me.neznamy.tab.api.protocol.PacketPlayOutBoss.Action;
import me.neznamy.tab.api.protocol.PacketPlayOutChat;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerListHeaderFooter;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityDestroy;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityMetadata;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityTeleport;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutSpawnEntityLiving;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.shared.TAB;

@SuppressWarnings({"unchecked", "rawtypes"})
public class BukkitPacketBuilder extends PacketBuilder {

	//nms storage
	private NMSStorage nms;

	//entity type ids
	private EnumMap<EntityType, Integer> entityIds = new EnumMap<>(EntityType.class);
	
	private Object emptyScoreboard;

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
		try {
			emptyScoreboard = nms.getConstructor("Scoreboard").newInstance();
		} catch (Exception e) {
			Bukkit.getConsoleSender().sendMessage("\u00a7c[TAB] Failed to create instance of \"Scoreboard\"");
		}
		buildMap.put(PacketPlayOutEntityMetadata.class, (packet, version) -> build((PacketPlayOutEntityMetadata)packet));
		buildMap.put(PacketPlayOutEntityTeleport.class, (packet, version) -> build((PacketPlayOutEntityTeleport)packet));
		buildMap.put(PacketPlayOutEntityDestroy.class, (packet, version) -> build((PacketPlayOutEntityDestroy)packet));
		buildMap.put(PacketPlayOutSpawnEntityLiving.class, (packet, version) -> build((PacketPlayOutSpawnEntityLiving)packet));
	}

	@Override
	public Object build(PacketPlayOutBoss packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException {
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
			TAB.getInstance().getErrorManager().printError("Failed to create 1.9 bossbar packet using ViaVersion v" + Bukkit.getPluginManager().getPlugin("ViaVersion").getDescription().getVersion() + ". Is it the latest version?", t);
			return null;
		}
	}

	/**
	 * Builds entity bossbar packet
	 * @param packet - packet to build
	 * @param clientVersion - client version
	 * @return entity bossbar packet
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	private Object buildBossPacketEntity(PacketPlayOutBoss packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException {
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

	@Override
	public Object build(PacketPlayOutChat packet, ProtocolVersion clientVersion) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		Object component = toNMSComponent(packet.getMessage(), clientVersion);
		Constructor<?> c = nms.getConstructor("PacketPlayOutChat");
		if (nms.getMinorVersion() >= 16) {
			return c.newInstance(component, nms.getEnum("ChatMessageType")[packet.getType().ordinal()], UUID.randomUUID());
		}
		if (nms.getMinorVersion() >= 12) {
			return c.newInstance(component, Enum.valueOf((Class<Enum>) nms.getClass("ChatMessageType"), packet.getType().toString()));
		}
		if (nms.getMinorVersion() >= 8) {
			return c.newInstance(component, (byte) packet.getType().ordinal());
		}
		if (nms.getMinorVersion() == 7) {
			return c.newInstance(component);
		}
		throw new IllegalStateException("Not supported on <1.7");
	}

	@Override
	public Object build(PacketPlayOutPlayerInfo packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		if (nms.getMinorVersion() < 8) return null;
		Object nmsPacket = nms.getConstructor("PacketPlayOutPlayerInfo").newInstance(nms.getEnum("EnumPlayerInfoAction")[packet.getAction().ordinal()], Array.newInstance(nms.getClass("EntityPlayer"), 0));
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
			parameters.add(data.getGameMode() == null ? null : nms.getEnum("EnumGamemode")[nms.getEnum("EnumGamemode").length-EnumGamemode.values().length+data.getGameMode().ordinal()]); //not_set was removed in 1.17
			parameters.add(data.getDisplayName() == null ? null : toNMSComponent(data.getDisplayName(), clientVersion));
			items.add(nms.getConstructor("PlayerInfoData").newInstance(parameters.toArray()));
		}
		nms.setField(nmsPacket, "PacketPlayOutPlayerInfo_PLAYERS", items);
		return nmsPacket;
	}

	@Override
	public Object build(PacketPlayOutPlayerListHeaderFooter packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		if (nms.getMinorVersion() < 8) return null;
		if (nms.getMinorVersion() >= 17) {
			return nms.getConstructor("PacketPlayOutPlayerListHeaderFooter").newInstance(toNMSComponent(packet.getHeader(), clientVersion), toNMSComponent(packet.getFooter(), clientVersion));
		}
		Object nmsPacket = nms.getConstructor("PacketPlayOutPlayerListHeaderFooter").newInstance();
		nms.setField(nmsPacket, "PacketPlayOutPlayerListHeaderFooter_HEADER", toNMSComponent(packet.getHeader(), clientVersion));
		nms.setField(nmsPacket, "PacketPlayOutPlayerListHeaderFooter_FOOTER", toNMSComponent(packet.getFooter(), clientVersion));
		return nmsPacket;
	}

	@Override
	public Object build(PacketPlayOutScoreboardDisplayObjective packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		return nms.getConstructor("PacketPlayOutScoreboardDisplayObjective").newInstance(packet.getSlot(), newScoreboardObjective(packet.getObjectiveName()));
	}

	@Override
	public Object build(PacketPlayOutScoreboardObjective packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		String displayName = clientVersion.getMinorVersion() < 13 ? cutTo(packet.getDisplayName(), 32) : packet.getDisplayName();
		if (nms.getMinorVersion() >= 13) {
			return nms.getConstructor("PacketPlayOutScoreboardObjective").newInstance(nms.getConstructor("ScoreboardObjective").newInstance(null, packet.getObjectiveName(), null, 
					toNMSComponent(IChatBaseComponent.optimizedComponent(displayName), clientVersion), 
					packet.getRenderType() == null ? null : nms.getEnum("EnumScoreboardHealthDisplay")[packet.getRenderType().ordinal()]), 
					packet.getMethod());
		}
		
		Object nmsPacket = nms.getConstructor("PacketPlayOutScoreboardObjective").newInstance();
		nms.setField(nmsPacket, "PacketPlayOutScoreboardObjective_OBJECTIVENAME", packet.getObjectiveName());
		nms.setField(nmsPacket, "PacketPlayOutScoreboardObjective_DISPLAYNAME", displayName);
		if (nms.getMinorVersion() >= 8 && packet.getRenderType() != null) {
			nms.setField(nmsPacket, "PacketPlayOutScoreboardObjective_RENDERTYPE", Enum.valueOf((Class<Enum>) nms.getClass("EnumScoreboardHealthDisplay"), packet.getRenderType().toString()));
		}
		nms.setField(nmsPacket, "PacketPlayOutScoreboardObjective_METHOD", packet.getMethod());
		return nmsPacket;
	}

	@Override
	public Object build(PacketPlayOutScoreboardScore packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		if (nms.getMinorVersion() >= 13) {
			return nms.getConstructor("PacketPlayOutScoreboardScore_1_13").newInstance(nms.getEnum("EnumScoreboardAction")[packet.getAction().ordinal()], packet.getObjectiveName(), packet.getPlayer(), packet.getScore());
		}
		if (packet.getAction() == PacketPlayOutScoreboardScore.Action.REMOVE) {
			return nms.getConstructor("PacketPlayOutScoreboardScore_String").newInstance(packet.getPlayer());
		}
		Object score = nms.getConstructor("ScoreboardScore").newInstance(emptyScoreboard, newScoreboardObjective(packet.getObjectiveName()), packet.getPlayer());
		nms.getMethod("ScoreboardScore_setScore").invoke(score, packet.getScore());
		if (nms.getMinorVersion() >= 8) {
			return nms.getConstructor("PacketPlayOutScoreboardScore").newInstance(score);
		}
		return nms.getConstructor("PacketPlayOutScoreboardScore").newInstance(score, 0);
	}

	@Override
	public Object build(PacketPlayOutScoreboardTeam packet, ProtocolVersion clientVersion) throws IllegalAccessException, InvocationTargetException, InstantiationException {
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
				return nms.getMethod("PacketPlayOutScoreboardTeam_ofString").invoke(null, team, packet.getPlayers().toArray(new String[0])[0], nms.getEnum("PacketPlayOutScoreboardTeam_a")[0]);
			case 4:
				return nms.getMethod("PacketPlayOutScoreboardTeam_ofString").invoke(null, team, packet.getPlayers().toArray(new String[0])[0], nms.getEnum("PacketPlayOutScoreboardTeam_a")[1]);
			default:
				throw new IllegalArgumentException("Invalid action: " + packet.getMethod());
			}
		}
		return nms.getConstructor("PacketPlayOutScoreboardTeam").newInstance(team, packet.getMethod());
	}
	
	private Object createTeamModern(PacketPlayOutScoreboardTeam packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		String prefix = packet.getPlayerPrefix();
		String suffix = packet.getPlayerSuffix();
		if (clientVersion.getMinorVersion() < 13) {
			prefix = cutTo(prefix, 16);
			suffix = cutTo(suffix, 16);
		}
		Object team = nms.getConstructor("ScoreboardTeam").newInstance(emptyScoreboard, packet.getName());
		((Collection<String>)nms.getMethod("ScoreboardTeam_getPlayerNameSet").invoke(team)).addAll(packet.getPlayers());
		if (prefix != null && prefix.length() > 0) nms.getMethod("ScoreboardTeam_setPrefix").invoke(team, toNMSComponent(IChatBaseComponent.optimizedComponent(prefix), clientVersion));
		if (suffix != null && suffix.length() > 0) nms.getMethod("ScoreboardTeam_setSuffix").invoke(team, toNMSComponent(IChatBaseComponent.optimizedComponent(suffix), clientVersion));
		EnumChatFormat format = packet.getColor() != null ? packet.getColor() : EnumChatFormat.lastColorsOf(prefix);
		nms.getMethod("ScoreboardTeam_setColor").invoke(team, nms.getEnum("EnumChatFormat")[format.ordinal()]);
		nms.getMethod("ScoreboardTeam_setNameTagVisibility").invoke(team, String.valueOf(packet.getNametagVisibility()).equals("always") ? nms.getEnum("EnumNameTagVisibility")[0] : nms.getEnum("EnumNameTagVisibility")[1]);
		nms.getMethod("ScoreboardTeam_setCollisionRule").invoke(team, String.valueOf(packet.getCollisionRule()).equals("always") ? nms.getEnum("EnumTeamPush")[0] : nms.getEnum("EnumTeamPush")[1]);
		return team;
	}
	
	private Object createTeamLegacy(PacketPlayOutScoreboardTeam packet, ProtocolVersion clientVersion) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		String prefix = packet.getPlayerPrefix();
		String suffix = packet.getPlayerSuffix();
		if (clientVersion.getMinorVersion() < 13) {
			prefix = cutTo(prefix, 16);
			suffix = cutTo(suffix, 16);
		}
		Object team = nms.getConstructor("ScoreboardTeam").newInstance(emptyScoreboard, packet.getName());
		((Collection<String>)nms.getMethod("ScoreboardTeam_getPlayerNameSet").invoke(team)).addAll(packet.getPlayers());
		if (prefix != null) nms.getMethod("ScoreboardTeam_setPrefix").invoke(team, prefix);
		if (suffix != null) nms.getMethod("ScoreboardTeam_setSuffix").invoke(team, suffix);
		if (nms.getMinorVersion() >= 8) nms.getMethod("ScoreboardTeam_setNameTagVisibility").invoke(team, String.valueOf(packet.getNametagVisibility()).equals("always") ? nms.getEnum("EnumNameTagVisibility")[0] : nms.getEnum("EnumNameTagVisibility")[1]);
		if (nms.getMinorVersion() >= 9) nms.getMethod("ScoreboardTeam_setCollisionRule").invoke(team, String.valueOf(packet.getCollisionRule()).equals("always") ? nms.getEnum("EnumTeamPush")[0] : nms.getEnum("EnumTeamPush")[1]);
		return team;
	}

	/**
	 * Builds entity destroy packet with given parameter
	 * @param id - entity id to destroy
	 * @return destroy packet
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public Object build(PacketPlayOutEntityDestroy packet) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		try {
			return nms.getConstructor("PacketPlayOutEntityDestroy").newInstance(packet.getEntities());
		} catch (IllegalArgumentException e) {
			//1.17.0
			return nms.getConstructor("PacketPlayOutEntityDestroy").newInstance(packet.getEntities()[0]);
		}
	}

	public Object build(PacketPlayOutEntityMetadata packet) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		return nms.getConstructor("PacketPlayOutEntityMetadata").newInstance(packet.getEntityId(), packet.getDataWatcher().toNMS(), true);
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
	 * @throws InvocationTargetException 
	 * @throws InstantiationException 
	 */
	public Object build(PacketPlayOutSpawnEntityLiving packet) throws IllegalAccessException, InstantiationException, InvocationTargetException {
		Object nmsPacket = nms.getConstructor("PacketPlayOutSpawnEntityLiving").newInstance(nms.getMethod("getHandle").invoke(Bukkit.getOnlinePlayers().iterator().next()));
		nms.setField(nmsPacket, "PacketPlayOutSpawnEntityLiving_ENTITYID", packet.getEntityId());
		nms.setField(nmsPacket, "PacketPlayOutSpawnEntityLiving_ENTITYTYPE", entityIds.get(packet.getEntityType()));
		nms.setField(nmsPacket, "PacketPlayOutSpawnEntityLiving_YAW", (byte)(packet.getLocation().getYaw() * 256.0f / 360.0f));
		nms.setField(nmsPacket, "PacketPlayOutSpawnEntityLiving_PITCH", (byte)(packet.getLocation().getPitch() * 256.0f / 360.0f));
		if (nms.getMinorVersion() <= 14) {
			nms.setField(nmsPacket, "PacketPlayOutSpawnEntityLiving_DATAWATCHER", packet.getDataWatcher().toNMS());
		}
		if (nms.getMinorVersion() >= 9) {
			nms.setField(nmsPacket, "PacketPlayOutSpawnEntityLiving_UUID", packet.getUniqueId());
			nms.setField(nmsPacket, "PacketPlayOutSpawnEntityLiving_X", packet.getLocation().getX());
			nms.setField(nmsPacket, "PacketPlayOutSpawnEntityLiving_Y", packet.getLocation().getY());
			nms.setField(nmsPacket, "PacketPlayOutSpawnEntityLiving_Z", packet.getLocation().getZ());
		} else {
			nms.setField(nmsPacket, "PacketPlayOutSpawnEntityLiving_X", floor(packet.getLocation().getX()*32));
			nms.setField(nmsPacket, "PacketPlayOutSpawnEntityLiving_Y", floor(packet.getLocation().getY()*32));
			nms.setField(nmsPacket, "PacketPlayOutSpawnEntityLiving_Z", floor(packet.getLocation().getZ()*32));
		}
		return nmsPacket;
	}

	/**
	 * Builds entity teleport packet with given parameters
	 * @param entityId - entity id
	 * @param location - location to teleport to
	 * @return entity teleport packet
	 * @throws IllegalAccessException 
	 * @throws InvocationTargetException 
	 * @throws InstantiationException 
	 */
	public Object build(PacketPlayOutEntityTeleport packet) throws IllegalAccessException, InstantiationException, InvocationTargetException {
		Object nmsPacket = nms.getConstructor("PacketPlayOutEntityTeleport").newInstance(nms.getMethod("getHandle").invoke(Bukkit.getOnlinePlayers().iterator().next()));
		nms.setField(nmsPacket, "PacketPlayOutEntityTeleport_ENTITYID", packet.getEntityId());
		if (nms.getMinorVersion() >= 9) {
			nms.setField(nmsPacket, "PacketPlayOutEntityTeleport_X", packet.getLocation().getX());
			nms.setField(nmsPacket, "PacketPlayOutEntityTeleport_Y", packet.getLocation().getY());
			nms.setField(nmsPacket, "PacketPlayOutEntityTeleport_Z", packet.getLocation().getZ());
		} else {
			nms.setField(nmsPacket, "PacketPlayOutEntityTeleport_X", floor(packet.getLocation().getX()*32));
			nms.setField(nmsPacket, "PacketPlayOutEntityTeleport_Y", floor(packet.getLocation().getY()*32));
			nms.setField(nmsPacket, "PacketPlayOutEntityTeleport_Z", floor(packet.getLocation().getZ()*32));
		}
		nms.setField(nmsPacket, "PacketPlayOutEntityTeleport_YAW", (byte) (packet.getLocation().getYaw()/360*256));
		nms.setField(nmsPacket, "PacketPlayOutEntityTeleport_PITCH", (byte) (packet.getLocation().getPitch()/360*256));
		return nmsPacket;
	}

	@Override
	public PacketPlayOutPlayerInfo readPlayerInfo(Object nmsPacket, ProtocolVersion clientVersion) throws IllegalAccessException, InvocationTargetException, IllegalArgumentException, ParseException {
		if (nms.getMinorVersion() < 8) return null;
		EnumPlayerInfoAction action = EnumPlayerInfoAction.valueOf(nms.getField("PacketPlayOutPlayerInfo_ACTION").get(nmsPacket).toString());
		List<PlayerInfoData> listData = new ArrayList<>();
		for (Object nmsData : (List<?>) nms.getField("PacketPlayOutPlayerInfo_PLAYERS").get(nmsPacket)) {
			Object nmsGamemode = nms.getMethod("PlayerInfoData_getGamemode").invoke(nmsData);
			EnumGamemode gamemode = (nmsGamemode == null) ? null : EnumGamemode.valueOf(nmsGamemode.toString());
			GameProfile profile = (GameProfile) nms.getMethod("PlayerInfoData_getProfile").invoke(nmsData);
			Object nmsComponent = nms.getMethod("PlayerInfoData_getDisplayName").invoke(nmsData);
			IChatBaseComponent listName = nmsComponent == null ? null : fromNMSComponent(nmsComponent);
			listData.add(new PlayerInfoData(profile.getName(), profile.getId(), profile.getProperties(), (int) nms.getMethod("PlayerInfoData_getLatency").invoke(nmsData), gamemode, listName));
		}
		return new PacketPlayOutPlayerInfo(action, listData);
	}

	@Override
	public PacketPlayOutScoreboardObjective readObjective(Object nmsPacket, ProtocolVersion clientVersion) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParseException {
		String objective = (String) nms.getField("PacketPlayOutScoreboardObjective_OBJECTIVENAME").get(nmsPacket);
		String displayName;
		Object component = nms.getField("PacketPlayOutScoreboardObjective_DISPLAYNAME").get(nmsPacket);
		if (nms.getMinorVersion() >= 13) {
			IChatBaseComponent c = component == null ? null : fromNMSComponent(component);
			displayName = c == null ? null : c.toLegacyText();
		} else {
			displayName = (String) component;
		}
		EnumScoreboardHealthDisplay renderType = null;
		if (nms.getMinorVersion() >= 8) {
			Object nmsRender = nms.getField("PacketPlayOutScoreboardObjective_RENDERTYPE").get(nmsPacket);
			if (nmsRender != null) {
				renderType = EnumScoreboardHealthDisplay.valueOf(nmsRender.toString());
			}
		}
		int method = nms.getField("PacketPlayOutScoreboardObjective_METHOD").getInt(nmsPacket);
		return new PacketPlayOutScoreboardObjective(method, objective, displayName, renderType);
	}

	@Override
	public PacketPlayOutScoreboardDisplayObjective readDisplayObjective(Object nmsPacket, ProtocolVersion clientVersion) throws IllegalAccessException {
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
	 * Converts minecraft IChatBaseComponent into TAB's component class. Currently does not support hover event.
	 * @param component - component to convert
	 * @return converted component
	 * @throws IllegalAccessException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws ParseException 
	 */
	public IChatBaseComponent fromNMSComponent(Object component) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParseException {
		long time = System.nanoTime();
		IChatBaseComponent obj = fromNMSComponent0(component);
		TAB.getInstance().getCPUManager().addMethodTime("fromNMSComponent", System.nanoTime()-time);
		return obj;
	}

	//separate method to prevent extras counting cpu again due to recursion and finally showing higher usage than real
	private IChatBaseComponent fromNMSComponent0(Object component) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, ParseException {
		if (!nms.getClass("ChatComponentText").isInstance(component)) return null; //paper
		IChatBaseComponent chat = new IChatBaseComponent((String) nms.getField("ChatComponentText_text").get(component));
		Object modifier = nms.getField("ChatBaseComponent_modifier").get(component);
		if (modifier != null) {
			chat.getModifier().setColor(fromNMSColor(nms.getField("ChatModifier_color").get(modifier)));
			chat.getModifier().setBold((Boolean) nms.getField("ChatModifier_bold").get(modifier));
			chat.getModifier().setItalic((Boolean) nms.getField("ChatModifier_italic").get(modifier));
			chat.getModifier().setObfuscated((Boolean) nms.getField("ChatModifier_obfuscated").get(modifier));
			chat.getModifier().setStrikethrough((Boolean) nms.getField("ChatModifier_strikethrough").get(modifier));
			chat.getModifier().setUnderlined((Boolean) nms.getField("ChatModifier_underlined").get(modifier));
			Object clickEvent = nms.getField("ChatModifier_clickEvent").get(modifier);
			if (clickEvent != null) {
				chat.getModifier().onClick(EnumClickAction.valueOf(nms.getField("ChatClickable_action").get(clickEvent).toString().toUpperCase()), (String) nms.getField("ChatClickable_value").get(clickEvent));
			}
			Object hoverEvent = nms.getField("ChatModifier_hoverEvent").get(modifier);
			if (hoverEvent != null) {
				EnumHoverAction action;
				IChatBaseComponent value;
				if (nms.getMinorVersion() >= 16) {
					//does not support show_item on 1.16+
					JsonObject json = (JsonObject) nms.getMethod("ChatHoverable_serialize").invoke(hoverEvent);
					action = EnumHoverAction.valueOf(json.get("action").getAsString().toUpperCase());
					value = IChatBaseComponent.deserialize(json.get("contents").getAsJsonObject().toString());
				} else {
					action = EnumHoverAction.valueOf(nms.getMethod("ChatHoverable_getAction").invoke(hoverEvent).toString().toUpperCase());
					value = fromNMSComponent0(nms.getMethod("ChatHoverable_getValue").invoke(hoverEvent));
				}
				chat.getModifier().onHover(action, value);
			}
		}
		for (Object extra : (List<Object>) nms.getField("ChatBaseComponent_extra").get(component)) {
			chat.addExtra(fromNMSComponent0(extra));
		}
		return chat;
	}

	private TextColor fromNMSColor(Object color) throws IllegalArgumentException, IllegalAccessException {
		if (color == null) return null;
		if (nms.getMinorVersion() >= 16) {
			String name = (String) nms.getField("ChatHexColor_name").get(color);
			if (name != null) {
				//legacy code
				return new TextColor(EnumChatFormat.valueOf(name.toUpperCase(Locale.US)));
			} else {
				int rgb = (int) nms.getField("ChatHexColor_rgb").get(color);
				return new TextColor((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
			}
		} else {
			return new TextColor(EnumChatFormat.valueOf(((Enum)color).name()));
		}
	}

	/**
	 * Converts TAB's IChatBaseComponent into minecraft's component. Currently does not support hover event.
	 * @param component - component to convert
	 * @param clientVersion - client version used to decide RGB conversion
	 * @return converted component
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public Object toNMSComponent(IChatBaseComponent component, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		long time = System.nanoTime();
		Object obj = toNMSComponent0(component, clientVersion);
		TAB.getInstance().getCPUManager().addMethodTime("toNMSComponent", System.nanoTime()-time);
		return obj;
	}

	//separate method to prevent extras counting cpu again due to recursion and finally showing higher usage than real
	private Object toNMSComponent0(IChatBaseComponent component, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		if (component == null) return null;
		Object chat = nms.getConstructor("ChatComponentText").newInstance(component.getText());
		Object modifier;
		Object clickEvent = component.getModifier().getClickEvent() == null ? null : nms.getConstructor("ChatClickable").newInstance(Enum.valueOf((Class<Enum>) nms.getClass("EnumClickAction"), 
				component.getModifier().getClickEvent().getAction().toString().toUpperCase()), component.getModifier().getClickEvent().getValue());
		if (nms.getMinorVersion() >= 16) {
			Object color = null;
			if (component.getModifier().getColor() != null) {
				if (clientVersion.getMinorVersion() >= 16) {
					color = nms.getMethod("ChatHexColor_ofInt").invoke(null, (component.getModifier().getColor().getRed() << 16) + (component.getModifier().getColor().getGreen() << 8) + component.getModifier().getColor().getBlue());
				} else {
					color = nms.getMethod("ChatHexColor_ofString").invoke(null, component.getModifier().getColor().getLegacyColor().toString().toLowerCase());
				}
			}
			Object hoverEvent = null;
			if (component.getModifier().getHoverEvent() != null) {
				Object nmsAction = nms.getMethod("EnumHoverAction_a").invoke(null, component.getModifier().getHoverEvent().getAction().toString().toLowerCase());
				switch (component.getModifier().getHoverEvent().getAction()) {
				case SHOW_TEXT:
					hoverEvent = nms.getConstructor("ChatHoverable").newInstance(nmsAction, toNMSComponent0(component.getModifier().getHoverEvent().getValue(), clientVersion));
					break;
				case SHOW_ENTITY:
					hoverEvent = nms.getMethod("EnumHoverAction_fromJson").invoke(nmsAction, ((ChatComponentEntity) component.getModifier().getHoverEvent().getValue()).toJson());
					break;
				case SHOW_ITEM:
					hoverEvent = nms.getMethod("EnumHoverAction_fromLegacyComponent").invoke(nmsAction, toNMSComponent0(component.getModifier().getHoverEvent().getValue(), clientVersion));
					break;
				default:
					break;
				}
			}
			modifier = nms.getConstructor("ChatModifier").newInstance(
				color,
				component.getModifier().getBold(),
				component.getModifier().getItalic(),
				component.getModifier().getUnderlined(),
				component.getModifier().getStrikethrough(),
				component.getModifier().getObfuscated(),
				clickEvent, hoverEvent, null, null);
		} else {
			modifier = nms.getConstructor("ChatModifier").newInstance();
			if (component.getModifier().getColor() != null) nms.setField(modifier, "ChatModifier_color", Enum.valueOf((Class<Enum>) nms.getClass("EnumChatFormat"), component.getModifier().getColor().getLegacyColor().toString()));
			nms.setField(modifier, "ChatModifier_bold", component.getModifier().getBold());
			nms.setField(modifier, "ChatModifier_italic", component.getModifier().getItalic());
			nms.setField(modifier, "ChatModifier_underlined", component.getModifier().getUnderlined());
			nms.setField(modifier, "ChatModifier_strikethrough", component.getModifier().getStrikethrough());
			nms.setField(modifier, "ChatModifier_obfuscated", component.getModifier().getObfuscated());
			if (clickEvent != null) nms.setField(modifier, "ChatModifier_clickEvent", clickEvent);
			if (component.getModifier().getHoverEvent() != null) {
				nms.setField(modifier, "ChatModifier_hoverEvent", nms.getConstructor("ChatHoverable").newInstance(nms.getMethod("EnumHoverAction_a").invoke(null, 
						component.getModifier().getHoverEvent().getAction().toString().toLowerCase()), toNMSComponent0(component.getModifier().getHoverEvent().getValue(), clientVersion)));
			}
		}
		nms.setField(chat, "ChatBaseComponent_modifier", modifier);
		for (IChatBaseComponent extra : component.getExtra()) {
			nms.getMethod("ChatComponentText_addSibling").invoke(chat, toNMSComponent0(extra, clientVersion));
		}
		return chat;
	}

	private Object newScoreboardObjective(String objectiveName) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		if (nms.getMinorVersion() >= 13) {
			return nms.getConstructor("ScoreboardObjective").newInstance(null, objectiveName, null, nms.getConstructor("ChatComponentText").newInstance(""), null);
		}
		return nms.getConstructor("ScoreboardObjective").newInstance(null, objectiveName, nms.getField("IScoreboardCriteria").get(null));
	}
}