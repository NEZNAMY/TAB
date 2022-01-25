package me.neznamy.tab.platforms.bukkit;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;

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
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityDestroy;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityMetadata;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityTeleport;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutSpawnEntityLiving;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;

@SuppressWarnings({"unchecked", "rawtypes"})
public class BukkitPacketBuilder extends PacketBuilder {

	//nms storage
	private final NMSStorage nms = NMSStorage.getInstance();

	//entity type ids
	private final EnumMap<EntityType, Integer> entityIds = new EnumMap<>(EntityType.class);

	private final Map<IChatBaseComponent, Object> componentCacheModern = new HashMap<>();
	private final Map<IChatBaseComponent, Object> componentCacheLegacy = new HashMap<>();

	private Object emptyScoreboard;
	private Object dummyEntity;

	/**
	 * Constructs new instance
	 */
	public BukkitPacketBuilder() {
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
			emptyScoreboard = nms.newScoreboard.newInstance();
		} catch (ReflectiveOperationException e) {
			Bukkit.getConsoleSender().sendMessage(EnumChatFormat.color("&c[TAB] Failed to create instance of \"Scoreboard\""));
		}
		if (nms.getMinorVersion() >= 8) {
			try {
				dummyEntity = nms.newEntityArmorStand.newInstance(nms.World_getHandle.invoke(Bukkit.getWorlds().get(0)), 0, 0, 0);
			} catch (ReflectiveOperationException e) {
				Bukkit.getConsoleSender().sendMessage(EnumChatFormat.color("&c[TAB] Failed to create instance of \"EntityArmorStand\""));
			}
		}
		buildMap.put(PacketPlayOutEntityMetadata.class, (packet, version) -> build((PacketPlayOutEntityMetadata)packet));
		buildMap.put(PacketPlayOutEntityTeleport.class, (packet, version) -> build((PacketPlayOutEntityTeleport)packet));
		buildMap.put(PacketPlayOutEntityDestroy.class, (packet, version) -> build((PacketPlayOutEntityDestroy)packet));
		buildMap.put(PacketPlayOutSpawnEntityLiving.class, (packet, version) -> build((PacketPlayOutSpawnEntityLiving)packet));
	}

	@Override
	public Object build(PacketPlayOutBoss packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
		if (nms.getMinorVersion() >= 9 || clientVersion.getMinorVersion() >= 9) {
			//1.9+ server or client, handled by bukkit api or ViaVersion
			return packet;
		}
		//<1.9 client and server
		return buildBossPacketEntity(packet, clientVersion);
	}

	@Override
	public Object build(PacketPlayOutChat packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
		Object component = toNMSComponent(packet.getMessage(), clientVersion);
		if (nms.getMinorVersion() >= 16) {
			return nms.newPacketPlayOutChat.newInstance(component, nms.ChatMessageType_values[packet.getType().ordinal()], UUID.randomUUID());
		}
		if (nms.getMinorVersion() >= 12) {
			return nms.newPacketPlayOutChat.newInstance(component, Enum.valueOf((Class<Enum>) nms.ChatMessageType, packet.getType().toString()));
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
	public Object build(PacketPlayOutPlayerInfo packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
		if (nms.getMinorVersion() < 8) return null;
		Object nmsPacket = nms.newPacketPlayOutPlayerInfo.newInstance(nms.EnumPlayerInfoAction_values[packet.getAction().ordinal()], Array.newInstance(nms.EntityPlayer, 0));
		List<Object> items = new ArrayList<>();
		for (PlayerInfoData data : packet.getEntries()) {
			GameProfile profile = new GameProfile(data.getUniqueId(), data.getName());
			if (data.getSkin() != null) profile.getProperties().putAll((PropertyMap) data.getSkin());
			List<Object> parameters = new ArrayList<>();
			if (nms.newPlayerInfoData.getParameterCount() == 5) {
				parameters.add(nmsPacket);
			}
			parameters.add(profile);
			parameters.add(data.getLatency());
			parameters.add(data.getGameMode() == null ? null : nms.EnumGamemode_values[nms.EnumGamemode_values.length-EnumGamemode.values().length+data.getGameMode().ordinal()]); //not_set was removed in 1.17
			parameters.add(data.getDisplayName() == null ? null : toNMSComponent(data.getDisplayName(), clientVersion));
			items.add(nms.newPlayerInfoData.newInstance(parameters.toArray()));
		}
		nms.setField(nmsPacket, nms.PacketPlayOutPlayerInfo_PLAYERS, items);
		return nmsPacket;
	}

	@Override
	public Object build(PacketPlayOutPlayerListHeaderFooter packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
		if (nms.getMinorVersion() < 8) return null;
		if (nms.newPacketPlayOutPlayerListHeaderFooter.getParameterCount() == 2) {
			return nms.newPacketPlayOutPlayerListHeaderFooter.newInstance(toNMSComponent(packet.getHeader(), clientVersion), toNMSComponent(packet.getFooter(), clientVersion));
		}
		Object nmsPacket = nms.newPacketPlayOutPlayerListHeaderFooter.newInstance();
		nms.setField(nmsPacket, nms.PacketPlayOutPlayerListHeaderFooter_HEADER, toNMSComponent(packet.getHeader(), clientVersion));
		nms.setField(nmsPacket, nms.PacketPlayOutPlayerListHeaderFooter_FOOTER, toNMSComponent(packet.getFooter(), clientVersion));
		return nmsPacket;
	}

	@Override
	public Object build(PacketPlayOutScoreboardDisplayObjective packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
		return nms.newPacketPlayOutScoreboardDisplayObjective.newInstance(packet.getSlot(), newScoreboardObjective(packet.getObjectiveName()));
	}

	@Override
	public Object build(PacketPlayOutScoreboardObjective packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
		String displayName = clientVersion.getMinorVersion() < 13 ? cutTo(packet.getDisplayName(), 32) : packet.getDisplayName();
		if (nms.getMinorVersion() >= 13) {
			return nms.newPacketPlayOutScoreboardObjective.newInstance(nms.newScoreboardObjective.newInstance(null, packet.getObjectiveName(), null, 
					toNMSComponent(IChatBaseComponent.optimizedComponent(displayName), clientVersion), 
					packet.getRenderType() == null ? null : nms.EnumScoreboardHealthDisplay_values[packet.getRenderType().ordinal()]), 
					packet.getMethod());
		}

		Object nmsPacket = nms.newPacketPlayOutScoreboardObjective.newInstance();
		nms.setField(nmsPacket, nms.PacketPlayOutScoreboardObjective_OBJECTIVENAME, packet.getObjectiveName());
		nms.setField(nmsPacket, nms.PacketPlayOutScoreboardObjective_DISPLAYNAME, displayName);
		if (nms.getMinorVersion() >= 8 && packet.getRenderType() != null) {
			nms.setField(nmsPacket, nms.PacketPlayOutScoreboardObjective_RENDERTYPE, Enum.valueOf((Class<Enum>) nms.EnumScoreboardHealthDisplay, packet.getRenderType().toString()));
		}
		nms.setField(nmsPacket, nms.PacketPlayOutScoreboardObjective_METHOD, packet.getMethod());
		return nmsPacket;
	}

	@Override
	public Object build(PacketPlayOutScoreboardScore packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
		if (nms.getMinorVersion() >= 13) {
			return nms.newPacketPlayOutScoreboardScore_1_13.newInstance(nms.EnumScoreboardAction_values[packet.getAction().ordinal()], packet.getObjectiveName(), packet.getPlayer(), packet.getScore());
		}
		if (packet.getAction() == PacketPlayOutScoreboardScore.Action.REMOVE) {
			return nms.newPacketPlayOutScoreboardScore_String.newInstance(packet.getPlayer());
		}
		Object score = nms.newScoreboardScore.newInstance(emptyScoreboard, newScoreboardObjective(packet.getObjectiveName()), packet.getPlayer());
		nms.ScoreboardScore_setScore.invoke(score, packet.getScore());
		if (nms.getMinorVersion() >= 8) {
			return nms.newPacketPlayOutScoreboardScore.newInstance(score);
		}
		return nms.newPacketPlayOutScoreboardScore.newInstance(score, 0);
	}

	@Override
	public Object build(PacketPlayOutScoreboardTeam packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
		if (nms.PacketPlayOutScoreboardTeam == null) return null; //fabric
		Object team = nms.newScoreboardTeam.newInstance(emptyScoreboard, packet.getName());
		String prefix = packet.getPlayerPrefix();
		String suffix = packet.getPlayerSuffix();
		if (clientVersion.getMinorVersion() < 13) {
			prefix = cutTo(prefix, 16);
			suffix = cutTo(suffix, 16);
		}
		((Collection<String>)nms.ScoreboardTeam_getPlayerNameSet.invoke(team)).addAll(packet.getPlayers());
		nms.ScoreboardTeam_setAllowFriendlyFire.invoke(team, (packet.getOptions() & 0x1) > 0);
		nms.ScoreboardTeam_setCanSeeFriendlyInvisibles.invoke(team, (packet.getOptions() & 0x2) > 0);
		if (nms.getMinorVersion() >= 13) {
			createTeamModern(packet, clientVersion, team, prefix, suffix);
		} else {
			createTeamLegacy(packet, team, prefix, suffix);
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
				return nms.PacketPlayOutScoreboardTeam_ofString.invoke(null, team, packet.getPlayers().toArray(new String[0])[0], nms.PacketPlayOutScoreboardTeam_PlayerAction_values[0]);
			case 4:
				return nms.PacketPlayOutScoreboardTeam_ofString.invoke(null, team, packet.getPlayers().toArray(new String[0])[0], nms.PacketPlayOutScoreboardTeam_PlayerAction_values[1]);
			default:
				throw new IllegalArgumentException("Invalid action: " + packet.getMethod());
			}
		}
		return nms.newPacketPlayOutScoreboardTeam.newInstance(team, packet.getMethod());
	}

	/**
	 * Builds entity destroy packet with given parameter
	 * @return destroy packet
	 * @throws	ReflectiveOperationException
	 * 			if thrown by reflective operation
	 */
	public Object build(PacketPlayOutEntityDestroy packet) throws ReflectiveOperationException {
		try {
			return nms.newPacketPlayOutEntityDestroy.newInstance(new Object[]{packet.getEntities()});
		} catch (IllegalArgumentException e) {
			//1.17.0
			return nms.newPacketPlayOutEntityDestroy.newInstance(packet.getEntities()[0]);
		}
	}

	public Object build(PacketPlayOutEntityMetadata packet) throws ReflectiveOperationException {
		return nms.newPacketPlayOutEntityMetadata.newInstance(packet.getEntityId(), packet.getDataWatcher().toNMS(), true);
	}

	/**
	 * Builds entity spawn packet with given parameters
	 *
	 * @return entity spawn packet
	 * @throws	ReflectiveOperationException
	 * 			if thrown by reflective operation
	 */
	public Object build(PacketPlayOutSpawnEntityLiving packet) throws ReflectiveOperationException {
		Object nmsPacket;
		if (nms.getMinorVersion() >= 17) {
			nmsPacket = nms.newPacketPlayOutSpawnEntityLiving.newInstance(dummyEntity);
		} else {
			nmsPacket = nms.newPacketPlayOutSpawnEntityLiving.newInstance();
		}
		nms.setField(nmsPacket, nms.PacketPlayOutSpawnEntityLiving_ENTITYID, packet.getEntityId());
		nms.setField(nmsPacket, nms.PacketPlayOutSpawnEntityLiving_ENTITYTYPE, entityIds.get(packet.getEntityType()));
		nms.setField(nmsPacket, nms.PacketPlayOutSpawnEntityLiving_YAW, (byte)(packet.getLocation().getYaw() * 256.0f / 360.0f));
		nms.setField(nmsPacket, nms.PacketPlayOutSpawnEntityLiving_PITCH, (byte)(packet.getLocation().getPitch() * 256.0f / 360.0f));
		if (nms.getMinorVersion() <= 14) {
			nms.setField(nmsPacket, nms.PacketPlayOutSpawnEntityLiving_DATAWATCHER, packet.getDataWatcher().toNMS());
		}
		if (nms.getMinorVersion() >= 9) {
			nms.setField(nmsPacket, nms.PacketPlayOutSpawnEntityLiving_UUID, packet.getUniqueId());
			nms.setField(nmsPacket, nms.PacketPlayOutSpawnEntityLiving_X, packet.getLocation().getX());
			nms.setField(nmsPacket, nms.PacketPlayOutSpawnEntityLiving_Y, packet.getLocation().getY());
			nms.setField(nmsPacket, nms.PacketPlayOutSpawnEntityLiving_Z, packet.getLocation().getZ());
		} else {
			nms.setField(nmsPacket, nms.PacketPlayOutSpawnEntityLiving_X, floor(packet.getLocation().getX()*32));
			nms.setField(nmsPacket, nms.PacketPlayOutSpawnEntityLiving_Y, floor(packet.getLocation().getY()*32));
			nms.setField(nmsPacket, nms.PacketPlayOutSpawnEntityLiving_Z, floor(packet.getLocation().getZ()*32));
		}
		return nmsPacket;
	}

	/**
	 * Builds entity teleport packet with given parameters
	 * @return entity teleport packet
	 * @throws	ReflectiveOperationException
	 * 			if thrown by reflective operation
	 */
	public Object build(PacketPlayOutEntityTeleport packet) throws ReflectiveOperationException {
		Object nmsPacket;
		if (nms.getMinorVersion() >= 17) {
			nmsPacket = nms.newPacketPlayOutEntityTeleport.newInstance(dummyEntity);
		} else {
			nmsPacket = nms.newPacketPlayOutEntityTeleport.newInstance();
		}
		nms.setField(nmsPacket, nms.PacketPlayOutEntityTeleport_ENTITYID, packet.getEntityId());
		if (nms.getMinorVersion() >= 9) {
			nms.setField(nmsPacket, nms.PacketPlayOutEntityTeleport_X, packet.getLocation().getX());
			nms.setField(nmsPacket, nms.PacketPlayOutEntityTeleport_Y, packet.getLocation().getY());
			nms.setField(nmsPacket, nms.PacketPlayOutEntityTeleport_Z, packet.getLocation().getZ());
		} else {
			nms.setField(nmsPacket, nms.PacketPlayOutEntityTeleport_X, floor(packet.getLocation().getX()*32));
			nms.setField(nmsPacket, nms.PacketPlayOutEntityTeleport_Y, floor(packet.getLocation().getY()*32));
			nms.setField(nmsPacket, nms.PacketPlayOutEntityTeleport_Z, floor(packet.getLocation().getZ()*32));
		}
		nms.setField(nmsPacket, nms.PacketPlayOutEntityTeleport_YAW, (byte) (packet.getLocation().getYaw()/360*256));
		nms.setField(nmsPacket, nms.PacketPlayOutEntityTeleport_PITCH, (byte) (packet.getLocation().getPitch()/360*256));
		return nmsPacket;
	}
	
	private void createTeamModern(PacketPlayOutScoreboardTeam packet, ProtocolVersion clientVersion, Object team, String prefix, String suffix) throws ReflectiveOperationException {
		if (prefix != null) nms.ScoreboardTeam_setPrefix.invoke(team, toNMSComponent(IChatBaseComponent.optimizedComponent(prefix), clientVersion));
		if (suffix != null) nms.ScoreboardTeam_setSuffix.invoke(team, toNMSComponent(IChatBaseComponent.optimizedComponent(suffix), clientVersion));
		EnumChatFormat format = packet.getColor() != null ? packet.getColor() : EnumChatFormat.lastColorsOf(prefix);
		nms.ScoreboardTeam_setColor.invoke(team, nms.EnumChatFormat_values[format.ordinal()]);
		nms.ScoreboardTeam_setNameTagVisibility.invoke(team, String.valueOf(packet.getNameTagVisibility()).equals("always") ? nms.EnumNameTagVisibility_values[0] : nms.EnumNameTagVisibility_values[1]);
		nms.ScoreboardTeam_setCollisionRule.invoke(team, String.valueOf(packet.getCollisionRule()).equals("always") ? nms.EnumTeamPush_values[0] : nms.EnumTeamPush_values[1]);
	}

	private void createTeamLegacy(PacketPlayOutScoreboardTeam packet, Object team, String prefix, String suffix) throws ReflectiveOperationException {
		if (prefix != null) nms.ScoreboardTeam_setPrefix.invoke(team, prefix);
		if (suffix != null) nms.ScoreboardTeam_setSuffix.invoke(team, suffix);
		if (nms.getMinorVersion() >= 8) nms.ScoreboardTeam_setNameTagVisibility.invoke(team, String.valueOf(packet.getNameTagVisibility()).equals("always") ? nms.EnumNameTagVisibility_values[0] : nms.EnumNameTagVisibility_values[1]);
		if (nms.getMinorVersion() >= 9) nms.ScoreboardTeam_setCollisionRule.invoke(team, String.valueOf(packet.getCollisionRule()).equals("always") ? nms.EnumTeamPush_values[0] : nms.EnumTeamPush_values[1]);
	}
	
	@Override
	public PacketPlayOutPlayerInfo readPlayerInfo(Object nmsPacket, ProtocolVersion clientVersion) throws ReflectiveOperationException {
		if (nms.getMinorVersion() < 8) return null;
		EnumPlayerInfoAction action = EnumPlayerInfoAction.valueOf(nms.PacketPlayOutPlayerInfo_ACTION.get(nmsPacket).toString());
		List<PlayerInfoData> listData = new ArrayList<>();
		for (Object nmsData : (List<?>) nms.PacketPlayOutPlayerInfo_PLAYERS.get(nmsPacket)) {
			Object nmsGameMode = nms.PlayerInfoData_getGamemode.invoke(nmsData);
			EnumGamemode gameMode = (nmsGameMode == null) ? null : EnumGamemode.valueOf(nmsGameMode.toString());
			GameProfile profile = (GameProfile) nms.PlayerInfoData_getProfile.invoke(nmsData);
			Object nmsComponent = nms.PlayerInfoData_getDisplayName.invoke(nmsData);
			IChatBaseComponent listName = nmsComponent == null ? null : fromNMSComponent(nmsComponent);
			PropertyMap map = new PropertyMap();
			map.putAll(profile.getProperties());
			listData.add(new PlayerInfoData(profile.getName(), profile.getId(), map, (int) nms.PlayerInfoData_getLatency.invoke(nmsData), gameMode, listName));
		}
		return new PacketPlayOutPlayerInfo(action, listData);
	}

	@Override
	public PacketPlayOutScoreboardObjective readObjective(Object nmsPacket) throws ReflectiveOperationException {
		return new PacketPlayOutScoreboardObjective(nms.PacketPlayOutScoreboardObjective_METHOD.getInt(nmsPacket),
				(String) nms.PacketPlayOutScoreboardObjective_OBJECTIVENAME.get(nmsPacket), null, null
		);
	}

	@Override
	public PacketPlayOutScoreboardDisplayObjective readDisplayObjective(Object nmsPacket) throws ReflectiveOperationException {
		return new PacketPlayOutScoreboardDisplayObjective(
			nms.PacketPlayOutScoreboardDisplayObjective_POSITION.getInt(nmsPacket),
			(String) nms.PacketPlayOutScoreboardDisplayObjective_OBJECTIVENAME.get(nmsPacket)
		);
	}

	/**
	 * Builds entity BossBar packet
	 * @param packet - packet to build
	 * @param clientVersion - client version
	 * @return entity BossBar packet
	 * @throws	ReflectiveOperationException
	 * 			if thrown by reflective operation
	 */
	private Object buildBossPacketEntity(PacketPlayOutBoss packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
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

	/**
	 * A method taken from minecraft code used to convert double to int
	 * @param paramDouble double value
	 * @return int value
	 */
	private int floor(double paramDouble){
		int i = (int)paramDouble;
		return paramDouble < i ? i - 1 : i;
	}

	/**
	 * Converts minecraft IChatBaseComponent into TAB's component class. Currently, does not support show_item hover event on 1.16+.
	 * @param component - component to convert
	 * @return converted component
	 * @throws	ReflectiveOperationException
	 * 			if thrown by reflective operation
	 */
	private IChatBaseComponent fromNMSComponent(Object component) throws ReflectiveOperationException {
		if (!nms.ChatComponentText.isInstance(component)) return null; //paper
		IChatBaseComponent chat = new IChatBaseComponent((String) nms.ChatComponentText_text.get(component));
		Object modifier = nms.ChatBaseComponent_modifier.get(component);
		if (modifier != null) {
			chat.getModifier().setColor(fromNMSColor(nms.ChatModifier_color.get(modifier)));
			chat.getModifier().setBold((Boolean) nms.ChatModifier_bold.get(modifier));
			chat.getModifier().setItalic((Boolean) nms.ChatModifier_italic.get(modifier));
			chat.getModifier().setObfuscated((Boolean) nms.ChatModifier_obfuscated.get(modifier));
			chat.getModifier().setStrikethrough((Boolean) nms.ChatModifier_strikethrough.get(modifier));
			chat.getModifier().setUnderlined((Boolean) nms.ChatModifier_underlined.get(modifier));
			Object clickEvent = nms.ChatModifier_clickEvent.get(modifier);
			if (clickEvent != null) {
				chat.getModifier().onClick(EnumClickAction.valueOf(nms.ChatClickable_action.get(clickEvent).toString().toUpperCase()), (String) nms.ChatClickable_value.get(clickEvent));
			}
			Object hoverEvent = nms.ChatModifier_hoverEvent.get(modifier);
			if (hoverEvent != null) {
				EnumHoverAction action;
				IChatBaseComponent value;
				if (nms.getMinorVersion() >= 16) {
					//does not support show_item on 1.16+
					JsonObject json = (JsonObject) nms.ChatHoverable_serialize.invoke(hoverEvent);
					action = EnumHoverAction.valueOf(json.get("action").getAsString().toUpperCase());
					value = IChatBaseComponent.deserialize(json.get("contents").getAsJsonObject().toString());
				} else {
					action = EnumHoverAction.valueOf(nms.ChatHoverable_getAction.invoke(hoverEvent).toString().toUpperCase());
					value = fromNMSComponent(nms.ChatHoverable_getValue.invoke(hoverEvent));
				}
				chat.getModifier().onHover(action, value);
			}
		}
		for (Object extra : (List<Object>) nms.ChatBaseComponent_extra.get(component)) {
			chat.addExtra(fromNMSComponent(extra));
		}
		return chat;
	}

	private TextColor fromNMSColor(Object color) throws ReflectiveOperationException {
		if (color == null) return null;
		if (nms.getMinorVersion() >= 16) {
			String name = (String) nms.ChatHexColor_name.get(color);
			if (name != null) {
				//legacy code
				return new TextColor(EnumChatFormat.valueOf(name.toUpperCase(Locale.US)));
			} else {
				int rgb = (int) nms.ChatHexColor_rgb.get(color);
				return new TextColor((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
			}
		} else {
			return new TextColor(EnumChatFormat.valueOf(((Enum)color).name()));
		}
	}

	/**
	 * Converts TAB's IChatBaseComponent into minecraft's component. Currently, does not support hover event.
	 * @param component - component to convert
	 * @param clientVersion - client version used to decide RGB conversion
	 * @return converted component
	 * @throws	ReflectiveOperationException
	 * 			if thrown by reflective operation
	 */
	public Object toNMSComponent(IChatBaseComponent component, ProtocolVersion clientVersion) throws ReflectiveOperationException {
		Object obj;
		if (clientVersion.getMinorVersion() >= 16) {
			if (componentCacheModern.containsKey(component)) return componentCacheModern.get(component);
			obj = toNMSComponent0(component, clientVersion);
			if (componentCacheModern.size() > 10000) componentCacheModern.clear();
			componentCacheModern.put(component, obj);
		} else {
			if (componentCacheLegacy.containsKey(component)) return componentCacheLegacy.get(component);
			obj = toNMSComponent0(component, clientVersion);
			if (componentCacheLegacy.size() > 10000) componentCacheLegacy.clear();
			componentCacheLegacy.put(component, obj);
		}
		return obj;
	}

	//separate method to prevent extras counting cpu again due to recursion and finally showing higher usage than real
	private Object toNMSComponent0(IChatBaseComponent component, ProtocolVersion clientVersion) throws ReflectiveOperationException {
		if (component == null) return null;
		Object chat = nms.newChatComponentText.newInstance(component.getText());
		Object modifier;
		Object clickEvent = component.getModifier().getClickEvent() == null ? null : nms.newChatClickable.newInstance(Enum.valueOf((Class<Enum>) nms.EnumClickAction, 
				component.getModifier().getClickEvent().getAction().toString().toUpperCase()), component.getModifier().getClickEvent().getValue());
		if (nms.getMinorVersion() >= 16) {
			Object color = null;
			if (component.getModifier().getColor() != null) {
				if (clientVersion.getMinorVersion() >= 16) {
					color = nms.ChatHexColor_ofInt.invoke(null, (component.getModifier().getColor().getRed() << 16) + (component.getModifier().getColor().getGreen() << 8) + component.getModifier().getColor().getBlue());
				} else {
					color = nms.ChatHexColor_ofString.invoke(null, component.getModifier().getColor().getLegacyColor().toString().toLowerCase());
				}
			}
			Object hoverEvent = null;
			if (component.getModifier().getHoverEvent() != null) {
				Object nmsAction = nms.EnumHoverAction_a.invoke(null, component.getModifier().getHoverEvent().getAction().toString().toLowerCase());
				switch (component.getModifier().getHoverEvent().getAction()) {
				case SHOW_TEXT:
					hoverEvent = nms.newChatHoverable.newInstance(nmsAction, toNMSComponent0(component.getModifier().getHoverEvent().getValue(), clientVersion));
					break;
				case SHOW_ENTITY:
					hoverEvent = nms.EnumHoverAction_fromJson.invoke(nmsAction, ((ChatComponentEntity) component.getModifier().getHoverEvent().getValue()).toJson());
					break;
				case SHOW_ITEM:
					hoverEvent = nms.EnumHoverAction_fromLegacyComponent.invoke(nmsAction, toNMSComponent0(component.getModifier().getHoverEvent().getValue(), clientVersion));
					break;
				default:
					break;
				}
			}
			modifier = nms.newChatModifier.newInstance(
					color,
					component.getModifier().getBold(),
					component.getModifier().getItalic(),
					component.getModifier().getUnderlined(),
					component.getModifier().getStrikethrough(),
					component.getModifier().getObfuscated(),
					clickEvent, hoverEvent, null, null);
		} else {
			modifier = nms.newChatModifier.newInstance();
			if (component.getModifier().getColor() != null) nms.setField(modifier, nms.ChatModifier_color, Enum.valueOf((Class<Enum>) nms.EnumChatFormat, component.getModifier().getColor().getLegacyColor().toString()));
			nms.setField(modifier, nms.ChatModifier_bold, component.getModifier().getBold());
			nms.setField(modifier, nms.ChatModifier_italic, component.getModifier().getItalic());
			nms.setField(modifier, nms.ChatModifier_underlined, component.getModifier().getUnderlined());
			nms.setField(modifier, nms.ChatModifier_strikethrough, component.getModifier().getStrikethrough());
			nms.setField(modifier, nms.ChatModifier_obfuscated, component.getModifier().getObfuscated());
			if (clickEvent != null) nms.setField(modifier, nms.ChatModifier_clickEvent, clickEvent);
			if (component.getModifier().getHoverEvent() != null) {
				nms.setField(modifier, nms.ChatModifier_hoverEvent, nms.newChatHoverable.newInstance(nms.EnumHoverAction_a.invoke(null, 
						component.getModifier().getHoverEvent().getAction().toString().toLowerCase()), toNMSComponent0(component.getModifier().getHoverEvent().getValue(), clientVersion)));
			}
		}
		nms.setField(chat, nms.ChatBaseComponent_modifier, modifier);
		for (IChatBaseComponent extra : component.getExtra()) {
			nms.ChatComponentText_addSibling.invoke(chat, toNMSComponent0(extra, clientVersion));
		}
		return chat;
	}

	private Object newScoreboardObjective(String objectiveName) throws ReflectiveOperationException {
		if (nms.getMinorVersion() >= 13) {
			return nms.newScoreboardObjective.newInstance(null, objectiveName, null, nms.newChatComponentText.newInstance(""), null);
		}
		return nms.newScoreboardObjective.newInstance(null, objectiveName, nms.IScoreboardCriteria_self.get(null));
	}
}