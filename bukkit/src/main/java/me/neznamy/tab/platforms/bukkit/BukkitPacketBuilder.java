package me.neznamy.tab.platforms.bukkit;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	private Map<EntityType, Integer> entityIds = new HashMap<EntityType, Integer>();

	/**
	 * Constructs new instance with given parameter
	 * @param nms - nms storage
	 */
	public BukkitPacketBuilder(NMSStorage nms) {
		this.nms = nms;
		if (nms.minorVersion >= 13) {
			entityIds.put(EntityType.ARMOR_STAND, 1);
			entityIds.put(EntityType.WITHER, 83);
		} else {
			entityIds.put(EntityType.WITHER, 64);
			if (nms.minorVersion >= 8){
				entityIds.put(EntityType.ARMOR_STAND, 30);
			}
		}
	}

	@Override
	public Object build(PacketPlayOutBoss packet, ProtocolVersion clientVersion) throws Exception {
		if (nms.minorVersion >= 9) {
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
	 * @throws Exception - if something fails
	 */
	private ByteBuf buildBossPacketVia(PacketPlayOutBoss packet, ProtocolVersion clientVersion) {
		if (clientVersion == ProtocolVersion.UNKNOWN) return null; //preventing disconnect if packet ID changes and users do not update
		try {
			ByteBuf buf = Unpooled.buffer();
			Type.VAR_INT.writePrimitive(buf, clientVersion.getMinorVersion() == 15 || clientVersion.getMinorVersion() >= 17 ? 0x0D : 0x0C);
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

	/**
	 * Builds entity bossbar packet
	 * @param packet - packet to build
	 * @param clientVersion - client version
	 * @return entity bossbar packet
	 * @throws Exception - if something fails
	 */
	private Object buildBossPacketEntity(PacketPlayOutBoss packet, ProtocolVersion clientVersion) throws Exception {
		if (packet.operation == Action.UPDATE_STYLE) return null; //nothing to do here

		int entityId = Math.abs(packet.id.hashCode());
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
		Object component = stringToComponent(packet.message.toString(clientVersion));
		if (nms.minorVersion >= 16) {
			return nms.newPacketPlayOutChat.newInstance(component, ((Object[])nms.ChatMessageType.getMethod("values").invoke(null))[packet.type.ordinal()], UUID.randomUUID());
		}
		if (nms.minorVersion >= 12) {
			return nms.newPacketPlayOutChat.newInstance(component, Enum.valueOf(nms.ChatMessageType, packet.type.toString()));
		}
		if (nms.minorVersion >= 8) {
			return nms.newPacketPlayOutChat.newInstance(component, (byte) packet.type.ordinal());
		}
		if (nms.minorVersion == 7) {
			return nms.newPacketPlayOutChat.newInstance(component);
		}
		throw new IllegalStateException("Not supported on <1.7");
	}

	@Override
	public Object build(PacketPlayOutPlayerInfo packet, ProtocolVersion clientVersion) throws Exception {
		if (nms.minorVersion < 8) return null;
		Object nmsPacket = nms.newPacketPlayOutPlayerInfo.newInstance(((Object[])nms.EnumPlayerInfoAction.getMethod("values").invoke(null))[packet.action.ordinal()], Array.newInstance(nms.EntityPlayer, 0));
		List<Object> items = new ArrayList<Object>();
		for (PlayerInfoData data : packet.entries) {
			GameProfile profile = new GameProfile(data.uniqueId, data.name);
			
			if (data.skin != null) profile.getProperties().putAll((PropertyMap) data.skin);
			List<Object> parameters = new ArrayList<Object>();
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
	public Object build(PacketPlayOutPlayerListHeaderFooter packet, ProtocolVersion clientVersion) throws Exception {
		if (nms.PacketPlayOutPlayerListHeaderFooter == null) return null;
		if (nms.minorVersion >= 17) {
			return nms.newPacketPlayOutPlayerListHeaderFooter.newInstance(toNMSComponent(packet.header, clientVersion), toNMSComponent(packet.footer, clientVersion));
		}
		Object nmsPacket = nms.newPacketPlayOutPlayerListHeaderFooter.newInstance();
		nms.PacketPlayOutPlayerListHeaderFooter_HEADER.set(nmsPacket, toNMSComponent(packet.header, clientVersion));
		nms.PacketPlayOutPlayerListHeaderFooter_FOOTER.set(nmsPacket, toNMSComponent(packet.footer, clientVersion));
		return nmsPacket;
	}

	@Override
	public Object build(PacketPlayOutScoreboardDisplayObjective packet, ProtocolVersion clientVersion) throws Exception {
		return nms.newPacketPlayOutScoreboardDisplayObjective.newInstance(packet.slot, newScoreboardObjective(packet.objectiveName));
	}

	@Override
	public Object build(PacketPlayOutScoreboardObjective packet, ProtocolVersion clientVersion) throws Exception {
		String displayName = clientVersion.getMinorVersion() < 13 ? cutTo(packet.displayName, 32) : packet.displayName;
		if (nms.minorVersion >= 13) {
			return nms.newPacketPlayOutScoreboardObjective.newInstance(nms.newScoreboardObjective.newInstance(null, packet.objectiveName, null, 
					toNMSComponent(IChatBaseComponent.optimizedComponent(displayName), clientVersion), 
					packet.renderType == null ? null : ((Object[])nms.EnumScoreboardHealthDisplay.getMethod("values").invoke(null))[packet.renderType.ordinal()]), 
					packet.method);
		}
		
		Object nmsPacket = nms.newPacketPlayOutScoreboardObjective.newInstance();
		nms.PacketPlayOutScoreboardObjective_OBJECTIVENAME.set(nmsPacket, packet.objectiveName);
		nms.PacketPlayOutScoreboardObjective_DISPLAYNAME.set(nmsPacket, displayName);
		if (nms.PacketPlayOutScoreboardObjective_RENDERTYPE != null && packet.renderType != null) {
			nms.PacketPlayOutScoreboardObjective_RENDERTYPE.set(nmsPacket, Enum.valueOf(nms.EnumScoreboardHealthDisplay, packet.renderType.toString()));
		}
		nms.PacketPlayOutScoreboardObjective_METHOD.set(nmsPacket, packet.method);
		return nmsPacket;
	}

	@Override
	public Object build(PacketPlayOutScoreboardScore packet, ProtocolVersion clientVersion) throws Exception {
		if (nms.minorVersion >= 13) {
			return nms.newPacketPlayOutScoreboardScore_1_13.newInstance(((Object[])nms.EnumScoreboardAction.getMethod("values").invoke(null))[packet.action.ordinal()], packet.objectiveName, packet.player, packet.score);
		}
		if (packet.action == PacketPlayOutScoreboardScore.Action.REMOVE) {
			return nms.newPacketPlayOutScoreboardScore_String.newInstance(packet.player);
		}
		Object score = nms.newScoreboardScore.newInstance(nms.newScoreboard.newInstance(), newScoreboardObjective(packet.objectiveName), packet.player);
		nms.ScoreboardScore_setScore.invoke(score, packet.score);
		if (nms.minorVersion >= 8) {
			return nms.newPacketPlayOutScoreboardScore.newInstance(score);
		}
		return nms.newPacketPlayOutScoreboardScore.newInstance(score, 0);
	}

	@Override
	public Object build(PacketPlayOutScoreboardTeam packet, ProtocolVersion clientVersion) throws Exception {
		String prefix = packet.playerPrefix;
		String suffix = packet.playerSuffix;
		if (clientVersion.getMinorVersion() < 13) {
			prefix = cutTo(prefix, 16);
			suffix = cutTo(suffix, 16);
		}
		Object team = nms.newScoreboardTeam.newInstance(nms.newScoreboard.newInstance(), packet.name);
		((Collection<String>)nms.ScoreboardTeam_getPlayerNameSet.invoke(team)).addAll(packet.players);
		if (nms.minorVersion >= 13) {
			if (prefix != null && prefix.length() > 0) nms.ScoreboardTeam_setPrefix.invoke(team, toNMSComponent(IChatBaseComponent.optimizedComponent(prefix), clientVersion));
			if (suffix != null && suffix.length() > 0) nms.ScoreboardTeam_setSuffix.invoke(team, toNMSComponent(IChatBaseComponent.optimizedComponent(suffix), clientVersion));
			EnumChatFormat format = packet.color != null ? packet.color : EnumChatFormat.lastColorsOf(prefix);
			nms.ScoreboardTeam_setColor.invoke(team, ((Object[])nms.EnumChatFormat.getMethod("values").invoke(null))[format.ordinal()]);
		} else {
			if (prefix != null) nms.ScoreboardTeam_setPrefix.invoke(team, prefix);
			if (suffix != null) nms.ScoreboardTeam_setSuffix.invoke(team, suffix);
		}
		if (nms.EnumNameTagVisibility != null && packet.nametagVisibility != null) nms.ScoreboardTeam_setNameTagVisibility.invoke(team, packet.nametagVisibility.equals("always") ? ((Object[])nms.EnumNameTagVisibility.getMethod("values").invoke(null))[0] : ((Object[])nms.EnumNameTagVisibility.getMethod("values").invoke(null))[1]);
		if (nms.EnumTeamPush != null && packet.collisionRule != null) nms.ScoreboardTeam_setCollisionRule.invoke(team, packet.collisionRule.equals("always") ? ((Object[])nms.EnumTeamPush.getMethod("values").invoke(null))[0] : ((Object[])nms.EnumTeamPush.getMethod("values").invoke(null))[1]);
		if (nms.minorVersion >= 17) {
			switch (packet.method) {
			case 0:
				return nms.PacketPlayOutScoreboardTeam_ofBoolean.invoke(null, team, true);
			case 1:
				return nms.PacketPlayOutScoreboardTeam_of.invoke(null, team);
			case 2:
				return nms.PacketPlayOutScoreboardTeam_ofBoolean.invoke(null, team, false);
			case 3:
				return nms.PacketPlayOutScoreboardTeam_ofString.invoke(null, team, packet.players.toArray(new String[0])[0], ((Object[])nms.PacketPlayOutScoreboardTeam_a.getMethod("values").invoke(null))[0]);
			case 4:
				return nms.PacketPlayOutScoreboardTeam_ofString.invoke(null, team, packet.players.toArray(new String[0])[0], ((Object[])nms.PacketPlayOutScoreboardTeam_a.getMethod("values").invoke(null))[1]);
			default:
				throw new IllegalArgumentException("Invalid action: " + packet.method);
			}
		}
		
		return nms.newPacketPlayOutScoreboardTeam.newInstance(team, packet.method);
	}

	/**
	 * Builds entity destroy packet with given parameter
	 * @param id - entity id to destroy
	 * @return destroy packet
	 * @throws Exception - if reflection fails
	 */
	public Object buildEntityDestroyPacket(int id) throws Exception {
		if (nms.minorVersion >= 17) {
			return nms.newPacketPlayOutEntityDestroy.newInstance(id);
		}
		return nms.newPacketPlayOutEntityDestroy.newInstance(new int[] {id});
	}

	/**
	 * Builds entity metadata packet with given parameters
	 * @param entityId - entity id
	 * @param dataWatcher - datawatcher
	 * @return metadata packet
	 * @throws Exception - if reflection fails
	 */
	public Object buildEntityMetadataPacket(int entityId, DataWatcher dataWatcher) throws Exception {
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
	 * @throws Exception - if reflection fails
	 */
	public Object buildEntitySpawnPacket(int entityId, UUID uuid, EntityType entityType, Location loc, DataWatcher dataWatcher) throws Exception {
		Object nmsPacket = nms.newPacketPlayOutSpawnEntityLiving.newInstance(nms.getHandle.invoke(Bukkit.getOnlinePlayers().iterator().next()));
		nms.PacketPlayOutSpawnEntityLiving_ENTITYID.set(nmsPacket, entityId);
		nms.PacketPlayOutSpawnEntityLiving_ENTITYTYPE.set(nmsPacket, entityIds.get(entityType));
		nms.PacketPlayOutSpawnEntityLiving_YAW.set(nmsPacket, (byte)(loc.getYaw() * 256.0f / 360.0f));
		nms.PacketPlayOutSpawnEntityLiving_PITCH.set(nmsPacket, (byte)(loc.getPitch() * 256.0f / 360.0f));
		if (nms.PacketPlayOutSpawnEntityLiving_DATAWATCHER != null) {
			nms.PacketPlayOutSpawnEntityLiving_DATAWATCHER.set(nmsPacket, dataWatcher.toNMS());
		}
		if (nms.minorVersion >= 9) {
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

	/**
	 * Builds entity teleport packet with given parameters
	 * @param entityId - entity id
	 * @param location - location to teleport to
	 * @return entity teleport packet
	 * @throws Exception - if reflection fails
	 */
	public Object buildEntityTeleportPacket(int entityId, Location location) throws Exception {
		Object nmsPacket = nms.newPacketPlayOutEntityTeleport.newInstance(nms.getHandle.invoke(Bukkit.getOnlinePlayers().iterator().next()));
		nms.PacketPlayOutEntityTeleport_ENTITYID.set(nmsPacket, entityId);
		if (nms.minorVersion >= 9) {
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
			GameProfile profile = (GameProfile) nms.PlayerInfoData_PROFILE.get(nmsData);
			Object nmsComponent = nms.PlayerInfoData_LISTNAME.get(nmsData);
			IChatBaseComponent listName = fromNMSComponent(nmsComponent);
			listData.add(new PlayerInfoData(profile.getName(), profile.getId(), profile.getProperties(), nms.PlayerInfoData_PING.getInt(nmsData), gamemode, listName));
		}
		return new PacketPlayOutPlayerInfo(action, listData);
	}

	@Override
	public PacketPlayOutScoreboardObjective readObjective(Object nmsPacket, ProtocolVersion clientVersion) throws Exception {
		String objective = (String) nms.PacketPlayOutScoreboardObjective_OBJECTIVENAME.get(nmsPacket);
		String displayName;
		if (nms.minorVersion >= 13) {
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
	 * Converts minecraft IChatBaseComponent into TAB's component class. Currently does not support hover event.
	 * @param component - component to convert
	 * @return converted component
	 * @throws Exception if something fails
	 */
	public IChatBaseComponent fromNMSComponent(Object component) throws Exception {
		long time = System.nanoTime();
		IChatBaseComponent obj = fromNMSComponent0(component);
		TAB.getInstance().getCPUManager().addMethodTime("fromNMSComponent", System.nanoTime()-time);
		return obj;
	}

	//separate method to prevent extras counting cpu again due to recursion and finally showing higher usage than real
	@SuppressWarnings("rawtypes")
	public IChatBaseComponent fromNMSComponent0(Object component) throws Exception {
		if (!nms.ChatComponentText.isInstance(component)) return null; //paper
		IChatBaseComponent chat = new IChatBaseComponent((String) nms.ChatComponentText_text.get(component));
		Object modifier = nms.ChatBaseComponent_modifier.get(component);
		if (modifier != null) {
			Object color = nms.ChatModifier_color.get(modifier);
			if (color != null) {
				if (nms.minorVersion >= 16) {
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
	 * @throws Exception if something fails
	 */
	public Object toNMSComponent(IChatBaseComponent component, ProtocolVersion clientVersion) throws Exception {
		long time = System.nanoTime();
		Object obj = toNMSComponent0(component, clientVersion);
		TAB.getInstance().getCPUManager().addMethodTime("toNMSComponent", System.nanoTime()-time);
		return obj;
	}

	//separate method to prevent extras counting cpu again due to recursion and finally showing higher usage than real
	private Object toNMSComponent0(IChatBaseComponent component, ProtocolVersion clientVersion) throws Exception {
		if (component == null) return null;
		Object chat = nms.newChatComponentText.newInstance(component.getText());
		Object modifier;
		if (nms.minorVersion >= 16) {
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
				component.isBold() ? true : null,
				component.isItalic() ? true : null,
				component.isUnderlined() ? true : null,
				component.isStrikethrough() ? true : null,
				component.isObfuscated() ? true : null,
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

	private Object newScoreboardObjective(String objectiveName) throws Exception {
		if (nms.minorVersion >= 13) {
			return nms.newScoreboardObjective.newInstance(null, objectiveName, null, stringToComponent("{\"text\":\"\"}"), null);
		}
		return nms.newScoreboardObjective.newInstance(null, objectiveName, nms.getFields(nms.IScoreboardCriteria, nms.IScoreboardCriteria).get(0).get(null));
	}
}