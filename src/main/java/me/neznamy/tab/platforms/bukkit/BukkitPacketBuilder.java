package me.neznamy.tab.platforms.bukkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.bossbar.BossBar;
import me.neznamy.tab.shared.packets.EnumChatFormat;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
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
import me.neznamy.tab.shared.packets.PacketPlayOutTitle;
import us.myles.ViaVersion.api.type.Type;
import us.myles.viaversion.libs.gson.JsonParser;

@SuppressWarnings("unchecked")
public class BukkitPacketBuilder implements PacketBuilder {

	private NMSStorage nms;
	private Map<EntityType, Integer> entityIds = new HashMap<EntityType, Integer>();

	public BukkitPacketBuilder(NMSStorage nms) {
		this.nms = nms;
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
			entityIds.put(EntityType.ARMOR_STAND, 1);
			entityIds.put(EntityType.WITHER, 83);
		} else {
			entityIds.put(EntityType.WITHER, 64);
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8){
				entityIds.put(EntityType.ARMOR_STAND, 30);
			}
		}
	}
	
	@Override
	public Object build(PacketPlayOutBoss packet, ProtocolVersion clientVersion) throws Exception {
		if (nms.minorVersion >= 9) {
			//1.9+ server
			return buildBossPacket19(packet, clientVersion);
		}
		if (clientVersion.getMinorVersion() >= 9 && Bukkit.getPluginManager().isPluginEnabled("ViaVersion")) {
			//1.9+ client on 1.8 server
			//technically redundant VV check as there is no other way to get 1.9 client on 1.8 server
			return buildBossPacketVia(packet, clientVersion);
		}

		//<1.9 client and server
		return buildBossPacketEntity(packet, clientVersion);
	}

	private Object buildBossPacket19(PacketPlayOutBoss packet, ProtocolVersion clientVersion) throws Exception {
		Object nmsPacket = nms.newPacketPlayOutBoss.newInstance();
		nms.PacketPlayOutBoss_UUID.set(nmsPacket, packet.id);
		nms.PacketPlayOutBoss_ACTION.set(nmsPacket, Enum.valueOf(nms.PacketPlayOutBoss_Action, packet.operation.toString()));
		nms.PacketPlayOutBoss_PROGRESS.set(nmsPacket, packet.pct);
		nms.PacketPlayOutBoss_NAME.set(nmsPacket, packet.name == null ? null : stringToComponent(IChatBaseComponent.optimizedComponent(packet.name).toString(clientVersion)));
		nms.PacketPlayOutBoss_COLOR.set(nmsPacket, packet.color == null ? null : Enum.valueOf(nms.BarColor, packet.color.toString()));
		nms.PacketPlayOutBoss_STYLE.set(nmsPacket, packet.overlay == null ? null : Enum.valueOf(nms.BarStyle, packet.overlay.toString()));
		nms.PacketPlayOutBoss_DARKEN_SKY.set(nmsPacket, packet.darkenScreen);
		nms.PacketPlayOutBoss_PLAY_MUSIC.set(nmsPacket, packet.playMusic);
		nms.PacketPlayOutBoss_CREATE_FOG.set(nmsPacket, packet.createWorldFog);
		return nmsPacket;
	}

	private Object buildBossPacketVia(PacketPlayOutBoss packet, ProtocolVersion clientVersion) throws Exception {
		if (clientVersion == ProtocolVersion.UNKNOWN) return null; //preventing disconnect if packet ID changes and users do not update
		try {
			ByteBuf buf = Unpooled.buffer();
			Type.VAR_INT.writePrimitive(buf, clientVersion.getMinorVersion() == 15 ? 0x0D : 0x0C);
			Type.UUID.write(buf, packet.id);
			Type.VAR_INT.writePrimitive(buf, packet.operation.ordinal());
			switch (packet.operation) {
			case ADD:
				Type.COMPONENT.write(buf, JsonParser.parseString(IChatBaseComponent.optimizedComponent(packet.name).toString(clientVersion)));
				Type.FLOAT.writePrimitive(buf, packet.pct);
				Type.VAR_INT.writePrimitive(buf, packet.color.ordinal());
				Type.VAR_INT.writePrimitive(buf, packet.overlay.ordinal());
				Type.BYTE.write(buf, packet.getFlags());
				break;
			case REMOVE:
				break;
			case UPDATE_PCT:
				Type.FLOAT.writePrimitive(buf, packet.pct);
				break;
			case UPDATE_NAME:
				Type.COMPONENT.write(buf, JsonParser.parseString(IChatBaseComponent.optimizedComponent(packet.name).toString(clientVersion)));
				break;
			case UPDATE_STYLE:
				Type.VAR_INT.writePrimitive(buf, packet.color.ordinal());
				Type.VAR_INT.writePrimitive(buf, packet.overlay.ordinal());
				break;
			case UPDATE_PROPERTIES:
				Type.BYTE.write(buf, packet.getFlags());
				break;
			default:
				break;
			}
			return buf;
		} catch (Throwable t) {
			return TAB.getInstance().getErrorManager().printError(null, "Failed to create 1.9 bossbar packet using ViaVersion v" + Bukkit.getPluginManager().getPlugin("ViaVersion").getDescription().getVersion() + ". Is it the latest version?", t);
		}
	}

	private Object buildBossPacketEntity(PacketPlayOutBoss packet, ProtocolVersion clientVersion) throws Exception {
		if (packet.operation == Action.UPDATE_STYLE) return null; //nothing to do here

		int entityId = ((BossBar)TAB.getInstance().getFeatureManager().getFeature("bossbar")).getLine(packet.id).entityId;
		if (packet.operation == Action.REMOVE) {
			return buildEntityDestroyPacket(entityId);
		}
		DataWatcher w = new DataWatcher();
		if (packet.operation == Action.UPDATE_PCT || packet.operation == Action.ADD) {
			float health = (float)300*packet.pct;
			if (health == 0) health = 1;
			w.helper().setHealth(health);
		}
		if (packet.operation == Action.UPDATE_NAME || packet.operation == Action.ADD) {
			w.helper().setCustomName(packet.name, clientVersion);
		}
		if (packet.operation == Action.ADD) {
			w.helper().setEntityFlags((byte) 32);
			return buildEntitySpawnPacket(entityId, null, EntityType.WITHER, new Location(null, 0,0,0), w);
		} else {
			return buildEntityMetadataPacket(entityId, w);
		}
	}

	@Override
	public Object build(PacketPlayOutChat packet, ProtocolVersion clientVersion) throws Exception {
		Object nmsPacket = nms.newPacketPlayOutChat.newInstance();
		nms.PacketPlayOutChat_MESSAGE.set(nmsPacket, stringToComponent(packet.message.toString(clientVersion)));
		if (nms.minorVersion >= 12) {
			nms.PacketPlayOutChat_POSITION.set(nmsPacket, Enum.valueOf(nms.ChatMessageType, packet.type.toString()));
		} else if (nms.minorVersion >= 8) {
			nms.PacketPlayOutChat_POSITION.set(nmsPacket, (byte)packet.type.ordinal());
		}
		if (nms.minorVersion >= 16) {
			nms.PacketPlayOutChat_SENDER.set(nmsPacket, UUID.randomUUID());
		}
		return nmsPacket;
	}

	@Override
	public Object build(PacketPlayOutPlayerInfo packet, ProtocolVersion clientVersion) throws Exception {
		if (nms.minorVersion < 8) return null;
		Object nmsPacket = nms.newPacketPlayOutPlayerInfo2.newInstance(Enum.valueOf(nms.EnumPlayerInfoAction, packet.action.toString()), Collections.EMPTY_LIST);
		List<Object> items = new ArrayList<Object>();
		for (PlayerInfoData data : packet.entries) {
			Object profile = nms.newGameProfile.newInstance(data.uniqueId, data.name);
			if (data.skin != null) nms.PropertyMap_putAll.invoke(nms.GameProfile_PROPERTIES.get(profile), data.skin);
			if (nms.newPlayerInfoData.getParameterCount() == 5) {
				items.add(nms.newPlayerInfoData.newInstance(nms.newPacketPlayOutPlayerInfo2.newInstance(null, Collections.EMPTY_LIST), profile, data.latency, data.gameMode == null ? null : Enum.valueOf(nms.EnumGamemode, data.gameMode.toString()), 
						data.displayName == null ? null : stringToComponent(data.displayName.toString(clientVersion))));
			} else {
				//1.8.8 paper
				items.add(nms.newPlayerInfoData.newInstance(profile, data.latency, data.gameMode == null ? null : Enum.valueOf(nms.EnumGamemode, data.gameMode.toString()), 
						data.displayName == null ? null : stringToComponent(data.displayName.toString(clientVersion))));
			}
		}
		nms.PacketPlayOutPlayerInfo_PLAYERS.set(nmsPacket, items);
		return nmsPacket;
	}

	@Override
	public Object build(PacketPlayOutPlayerListHeaderFooter packet, ProtocolVersion clientVersion) throws Exception {
		Object nmsPacket = nms.newPacketPlayOutPlayerListHeaderFooter.newInstance();
		nms.PacketPlayOutPlayerListHeaderFooter_HEADER.set(nmsPacket, stringToComponent(packet.header.toString(clientVersion, true)));
		nms.PacketPlayOutPlayerListHeaderFooter_FOOTER.set(nmsPacket, stringToComponent(packet.footer.toString(clientVersion, true)));
		return nmsPacket;
	}

	@Override
	public Object build(PacketPlayOutScoreboardDisplayObjective packet, ProtocolVersion clientVersion) throws Exception {
		Object nmsPacket = nms.newPacketPlayOutScoreboardDisplayObjective.newInstance();
		nms.PacketPlayOutScoreboardDisplayObjective_POSITION.set(nmsPacket, packet.slot);
		nms.PacketPlayOutScoreboardDisplayObjective_OBJECTIVENAME.set(nmsPacket, packet.objectiveName);
		return nmsPacket;
	}

	@Override
	public Object build(PacketPlayOutScoreboardObjective packet, ProtocolVersion clientVersion) throws Exception {
		String displayName = packet.displayName;
		if (clientVersion.getMinorVersion() < 13) {
			displayName = cutTo(displayName, 32);
		}
		Object nmsPacket = nms.newPacketPlayOutScoreboardObjective.newInstance();
		nms.PacketPlayOutScoreboardObjective_OBJECTIVENAME.set(nmsPacket, packet.objectiveName);
		if (nms.minorVersion >= 13) {
			nms.PacketPlayOutScoreboardObjective_DISPLAYNAME.set(nmsPacket, stringToComponent(IChatBaseComponent.optimizedComponent(displayName).toString(clientVersion)));
		} else {
			nms.PacketPlayOutScoreboardObjective_DISPLAYNAME.set(nmsPacket, displayName);
		}
		if (nms.PacketPlayOutScoreboardObjective_RENDERTYPE != null && packet.renderType != null) {
			if (nms.minorVersion >= 8) {
				nms.PacketPlayOutScoreboardObjective_RENDERTYPE.set(nmsPacket, Enum.valueOf(nms.EnumScoreboardHealthDisplay, packet.renderType.toString()));
			} else {
				nms.PacketPlayOutScoreboardObjective_RENDERTYPE.set(nmsPacket, packet.renderType.ordinal());
			}
		}
		nms.PacketPlayOutScoreboardObjective_METHOD.set(nmsPacket, packet.method);
		return nmsPacket;
	}

	@Override
	public Object build(PacketPlayOutScoreboardScore packet, ProtocolVersion clientVersion) throws Exception {
		if (nms.minorVersion >= 13) {
			return nms.newPacketPlayOutScoreboardScore_1_13.newInstance(Enum.valueOf(nms.EnumScoreboardAction, packet.action.toString()), packet.objectiveName, packet.player, packet.score);
		}
		if (packet.action == PacketPlayOutScoreboardScore.Action.REMOVE) {
			return nms.newPacketPlayOutScoreboardScore_String.newInstance(packet.player);
		}
		Object nmsPacket = nms.newPacketPlayOutScoreboardScore0.newInstance();
		nms.PacketPlayOutScoreboardScore_PLAYER.set(nmsPacket, packet.player);
		nms.PacketPlayOutScoreboardScore_OBJECTIVENAME.set(nmsPacket, packet.objectiveName);
		nms.PacketPlayOutScoreboardScore_SCORE.set(nmsPacket, packet.score);
		if (nms.minorVersion >= 8) {
			nms.PacketPlayOutScoreboardScore_ACTION.set(nmsPacket, Enum.valueOf(nms.EnumScoreboardAction, packet.action.toString()));
		} else {
			nms.PacketPlayOutScoreboardScore_ACTION.set(nmsPacket, packet.action.ordinal());
		}
		return nmsPacket;
	}

	@Override
	public Object build(PacketPlayOutScoreboardTeam packet, ProtocolVersion clientVersion) throws Exception {
		String prefix = packet.playerPrefix;
		String suffix = packet.playerSuffix;
		if (clientVersion.getMinorVersion() < 13) {
			prefix = cutTo(prefix, 16);
			suffix = cutTo(suffix, 16);
		}
		Object nmsPacket = nms.newPacketPlayOutScoreboardTeam.newInstance();
		nms.PacketPlayOutScoreboardTeam_NAME.set(nmsPacket, packet.name);
		if (nms.minorVersion >= 13) {
			nms.PacketPlayOutScoreboardTeam_DISPLAYNAME.set(nmsPacket, stringToComponent(IChatBaseComponent.optimizedComponent(packet.name).toString(clientVersion)));
			if (prefix != null && prefix.length() > 0) nms.PacketPlayOutScoreboardTeam_PREFIX.set(nmsPacket, stringToComponent(IChatBaseComponent.optimizedComponent(prefix).toString(clientVersion)));
			if (suffix != null && suffix.length() > 0) nms.PacketPlayOutScoreboardTeam_SUFFIX.set(nmsPacket, stringToComponent(IChatBaseComponent.optimizedComponent(suffix).toString(clientVersion)));
			EnumChatFormat format = packet.color != null ? packet.color : EnumChatFormat.lastColorsOf(prefix);
			nms.PacketPlayOutScoreboardTeam_CHATFORMAT.set(nmsPacket, Enum.valueOf(nms.EnumChatFormat, format.toString()));
		} else {
			nms.PacketPlayOutScoreboardTeam_DISPLAYNAME.set(nmsPacket, packet.name);
			nms.PacketPlayOutScoreboardTeam_PREFIX.set(nmsPacket, prefix);
			nms.PacketPlayOutScoreboardTeam_SUFFIX.set(nmsPacket, suffix);
		}
		if (nms.PacketPlayOutScoreboardTeam_VISIBILITY != null) nms.PacketPlayOutScoreboardTeam_VISIBILITY.set(nmsPacket, packet.nametagVisibility);
		if (nms.PacketPlayOutScoreboardTeam_COLLISION != null) nms.PacketPlayOutScoreboardTeam_COLLISION.set(nmsPacket, packet.collisionRule);
		nms.PacketPlayOutScoreboardTeam_PLAYERS.set(nmsPacket, packet.players);
		nms.PacketPlayOutScoreboardTeam_ACTION.set(nmsPacket, packet.method);
		nms.PacketPlayOutScoreboardTeam_SIGNATURE.set(nmsPacket, packet.options);
		return nmsPacket;
	}

	@Override
	public Object build(PacketPlayOutTitle packet, ProtocolVersion clientVersion) throws Exception {
		if (nms.minorVersion < 8) return null;
		return nms.newPacketPlayOutTitle.newInstance(Enum.valueOf(nms.EnumTitleAction, packet.action.toString()), 
				packet.text == null ? null : stringToComponent(IChatBaseComponent.optimizedComponent(packet.text).toString(clientVersion)), packet.fadeIn, packet.stay, packet.fadeOut);
	}
	
	public Object buildEntityDestroyPacket(int... ids) throws Exception {
		return nms.newPacketPlayOutEntityDestroy.newInstance(ids);
	}
	
	public Object buildEntityMetadataPacket(int entityId, DataWatcher dataWatcher) throws Exception {
		return nms.newPacketPlayOutEntityMetadata.newInstance(entityId, dataWatcher.toNMS(), true);
	}
	
	public Object buildEntitySpawnPacket(int entityId, UUID uuid, EntityType entityType, Location loc, DataWatcher dataWatcher) throws Exception {
		Object nmsPacket = nms.newPacketPlayOutSpawnEntityLiving.newInstance();
		nms.PacketPlayOutSpawnEntityLiving_ENTITYID.set(nmsPacket, entityId);
		nms.PacketPlayOutSpawnEntityLiving_ENTITYTYPE.set(nmsPacket, entityIds.get(entityType));
		nms.PacketPlayOutSpawnEntityLiving_YAW.set(nmsPacket, (byte)(loc.getYaw() * 256.0f / 360.0f));
		nms.PacketPlayOutSpawnEntityLiving_PITCH.set(nmsPacket, (byte)(loc.getPitch() * 256.0f / 360.0f));
		if (nms.PacketPlayOutSpawnEntityLiving_DATAWATCHER != null) {
			nms.PacketPlayOutSpawnEntityLiving_DATAWATCHER.set(nmsPacket, dataWatcher.toNMS());
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			nms.PacketPlayOutSpawnEntityLiving_UUID.set(nmsPacket, uuid);
			nms.PacketPlayOutSpawnEntityLiving_X.set(nmsPacket, loc.getX());
			nms.PacketPlayOutSpawnEntityLiving_Y.set(nmsPacket, loc.getY());
			nms.PacketPlayOutSpawnEntityLiving_Z.set(nmsPacket, loc.getZ());
		} else {
			nms.PacketPlayOutSpawnEntityLiving_X.set(nmsPacket, floor((double)loc.getX()*32));
			nms.PacketPlayOutSpawnEntityLiving_Y.set(nmsPacket, floor((double)loc.getY()*32));
			nms.PacketPlayOutSpawnEntityLiving_Z.set(nmsPacket, floor((double)loc.getZ()*32));
		}
		return nmsPacket;
	}
	
	public Object buildEntityTeleportPacket(int entityId, Location location) throws Exception {
		Object nmsPacket = nms.newPacketPlayOutEntityTeleport.newInstance();
		nms.PacketPlayOutEntityTeleport_ENTITYID.set(nmsPacket, entityId);
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			nms.PacketPlayOutEntityTeleport_X.set(nmsPacket, location.getX());
			nms.PacketPlayOutEntityTeleport_Y.set(nmsPacket, location.getY());
			nms.PacketPlayOutEntityTeleport_Z.set(nmsPacket, location.getZ());
		} else {
			nms.PacketPlayOutEntityTeleport_X.set(nmsPacket, floor((double)location.getX()*32));
			nms.PacketPlayOutEntityTeleport_Y.set(nmsPacket, floor((double)location.getY()*32));
			nms.PacketPlayOutEntityTeleport_Z.set(nmsPacket, floor((double)location.getZ()*32));
		}
		nms.PacketPlayOutEntityTeleport_YAW.set(nmsPacket, (byte)((float)location.getYaw()/360*256));
		nms.PacketPlayOutEntityTeleport_PITCH.set(nmsPacket, (byte)((float)location.getPitch()/360*256));
		return nmsPacket;
	}

	@Override
	public PacketPlayOutPlayerInfo readPlayerInfo(Object nmsPacket, ProtocolVersion clientVersion) throws Exception {
		if (nms.minorVersion < 8) return null;
		EnumPlayerInfoAction action = EnumPlayerInfoAction.valueOf(nms.PacketPlayOutPlayerInfo_ACTION.get(nmsPacket).toString());
		List<PlayerInfoData> listData = new ArrayList<PlayerInfoData>();
		for (Object nmsData : (List<?>) nms.PacketPlayOutPlayerInfo_PLAYERS.get(nmsPacket)) {
			Object nmsGamemode = nms.PlayerInfoData_GAMEMODE.get(nmsData);
			EnumGamemode gamemode = nmsGamemode == null ? null : EnumGamemode.valueOf(nmsGamemode.toString());
			Object profile = nms.PlayerInfoData_PROFILE.get(nmsData);
			Object nmsComponent = nms.PlayerInfoData_LISTNAME.get(nmsData);
			IChatBaseComponent listName = IChatBaseComponent.fromString(componentToString(nmsComponent));
			listData.add(new PlayerInfoData((String) nms.GameProfile_NAME.get(profile), (UUID) nms.GameProfile_ID.get(profile), nms.GameProfile_PROPERTIES.get(profile), nms.PlayerInfoData_PING.getInt(nmsData), gamemode, listName));
		}
		return new PacketPlayOutPlayerInfo(action, listData);
	}

	@Override
	public PacketPlayOutScoreboardObjective readObjective(Object nmsPacket, ProtocolVersion clientVersion) throws Exception {
		String objective = (String) nms.PacketPlayOutScoreboardObjective_OBJECTIVENAME.get(nmsPacket);
		String displayName;
		if (nms.minorVersion >= 13) {
			displayName = IChatBaseComponent.fromString(componentToString(nms.PacketPlayOutScoreboardObjective_DISPLAYNAME.get(nmsPacket))).toLegacyText();
		} else {
			displayName = (String) nms.PacketPlayOutScoreboardObjective_DISPLAYNAME.get(nmsPacket);
		}
		EnumScoreboardHealthDisplay renderType = null;
		if (nms.PacketPlayOutScoreboardObjective_RENDERTYPE != null) {
			Object nmsRender = nms.PacketPlayOutScoreboardObjective_RENDERTYPE.get(nmsPacket);
			if (nmsRender != null) {
				if (nms.minorVersion >= 8) {
					renderType = EnumScoreboardHealthDisplay.valueOf(nmsRender.toString());
				} else {
					renderType = EnumScoreboardHealthDisplay.values()[(int)nmsRender];
				}
			}
		}
		int method = nms.PacketPlayOutScoreboardObjective_METHOD.getInt(nmsPacket);
		return new PacketPlayOutScoreboardObjective(method, objective, displayName, renderType);
	}

	@Override
	public PacketPlayOutScoreboardDisplayObjective readDisplayObjective(Object nmsPacket, ProtocolVersion clientVersion) throws Exception {
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
	 * @throws Exception if something fails
	 */
	public Object stringToComponent(String json) throws Exception {
		if (json == null) return null;
		return nms.ChatSerializer_DESERIALIZE.invoke(null, json);
	}
	
	/**
	 * Converts NMS component into a string
	 * @param component component to convert
	 * @return json in string format
	 * @throws Exception if something fails
	 */
	public String componentToString(Object component) throws Exception {
		if (component == null) return null;
		return (String) nms.ChatSerializer_SERIALIZE.invoke(null, component);
	}
}