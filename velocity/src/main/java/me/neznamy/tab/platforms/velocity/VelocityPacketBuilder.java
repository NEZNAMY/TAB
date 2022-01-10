package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.proxy.protocol.packet.ScoreboardDisplay;
import com.velocitypowered.proxy.protocol.packet.ScoreboardObjective;
import com.velocitypowered.proxy.protocol.packet.ScoreboardObjective.HealthDisplay;
import com.velocitypowered.proxy.protocol.packet.ScoreboardSetScore;
import com.velocitypowered.proxy.protocol.packet.ScoreboardTeam;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.api.protocol.PacketPlayOutBoss;
import me.neznamy.tab.api.protocol.PacketPlayOutChat;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerListHeaderFooter;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Packet builder for Velocity platform
 */
public class VelocityPacketBuilder extends PacketBuilder {

	private static Class<?> listItem_class;
	private static Method listItem_getAction;
	private static Method listItem_getItems;
	private static Class<?> item_class;
	private static Method item_getUuid;
	private static Method item_getName;
	private static Method item_setName;
	private static Method item_getProperties;
	private static Method item_setProperties;
	private static Method item_getGameMode;
	private static Method item_setGameMode;
	private static Method item_getLatency;
	private static Method item_setLatency;
	private static Method item_getDisplayName;
	private static Method item_setDisplayName;

	static {
		try {
			listItem_class = Class.forName("com.velocitypowered.proxy.protocol.packet.PlayerListItem");
			listItem_getAction = listItem_class.getMethod("getAction");
			listItem_getItems = listItem_class.getMethod("getItems");
			item_class = Class.forName("com.velocitypowered.proxy.protocol.packet.PlayerListItem$Item");
			item_getUuid = item_class.getMethod("getUuid");
			item_getName = item_class.getMethod("getName");
			item_setName = item_class.getMethod("setName", String.class);
			item_getProperties = item_class.getMethod("getProperties");
			item_setProperties = item_class.getMethod("setProperties", List.class);
			item_getGameMode = item_class.getMethod("getGameMode");
			item_setGameMode = item_class.getMethod("setGameMode", int.class);
			item_getLatency = item_class.getMethod("getLatency");
			item_setLatency = item_class.getMethod("setLatency", int.class);
			item_getDisplayName = item_class.getMethod("getDisplayName");
			item_setDisplayName = item_class.getMethod("setDisplayName", Component.class);
		} catch (ReflectiveOperationException e) {
			// Should never happen until velocity updates their packet system
		}
	}

	@Override
	public Object build(PacketPlayOutBoss packet, ProtocolVersion clientVersion) {
		return packet;
	}

	@Override
	public Object build(PacketPlayOutChat packet, ProtocolVersion clientVersion) {
		return packet;
	}

	@Override
	public Object build(PacketPlayOutPlayerInfo packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
		List<Object> items = new ArrayList<>();
		for (PlayerInfoData data : packet.getEntries()) {
			Object item = item_class.getConstructor(UUID.class).newInstance(data.getUniqueId());
			if (data.getDisplayName() != null) {
				if (clientVersion.getMinorVersion() >= 8) {
					item_setDisplayName.invoke(item, Main.convertComponent(data.getDisplayName(), clientVersion));
				} else {
					item_setDisplayName.invoke(item, LegacyComponentSerializer.legacySection().deserialize(data.getDisplayName().toLegacyText()));
				}
			} else if (clientVersion.getMinorVersion() < 8) {
				item_setDisplayName.invoke(item, LegacyComponentSerializer.legacySection().deserialize(data.getName()));
			}
			if (data.getGameMode() != null) item_setGameMode.invoke(item, data.getGameMode().ordinal()-1);
			item_setLatency.invoke(item, data.getLatency());
			if (data.getSkin() != null) {
				item_setProperties.invoke(item, data.getSkin());
			} else {
				item_setProperties.invoke(item, Collections.emptyList());
			}
			item_setName.invoke(item, data.getName());
			items.add(item);
		}
		return listItem_class.getConstructor(int.class, List.class).newInstance(packet.getAction().ordinal(), items);
	}

	@Override
	public Object build(PacketPlayOutPlayerListHeaderFooter packet, ProtocolVersion clientVersion) {
		return packet;
	}

	@Override
	public Object build(PacketPlayOutScoreboardDisplayObjective packet, ProtocolVersion clientVersion) {
		return new ScoreboardDisplay((byte)packet.getSlot(), packet.getObjectiveName());
	}

	@Override
	public Object build(PacketPlayOutScoreboardObjective packet, ProtocolVersion clientVersion) {
		return new ScoreboardObjective(packet.getObjectiveName(), jsonOrCut(packet.getDisplayName(), clientVersion, 32), packet.getRenderType() == null ? null : HealthDisplay.valueOf(packet.getRenderType().toString()), (byte)packet.getMethod());
	}

	@Override
	public Object build(PacketPlayOutScoreboardScore packet, ProtocolVersion clientVersion) {
		return new ScoreboardSetScore(packet.getPlayer(), (byte)packet.getAction().ordinal(), packet.getObjectiveName(), packet.getScore());
	}

	@Override
	public Object build(PacketPlayOutScoreboardTeam packet, ProtocolVersion clientVersion) {
		int color = 0;
		if (clientVersion.getMinorVersion() >= 13) {
			color = (packet.getColor() != null ? packet.getColor() : EnumChatFormat.lastColorsOf(packet.getPlayerPrefix())).ordinal();
		}
		return new ScoreboardTeam(packet.getName(), (byte)packet.getMethod(), jsonOrCut(packet.getName(), clientVersion, 16), jsonOrCut(packet.getPlayerPrefix(), clientVersion, 16), jsonOrCut(packet.getPlayerSuffix(), clientVersion, 16),
				packet.getNameTagVisibility(), packet.getCollisionRule(), color, (byte)packet.getOptions(), packet.getPlayers() instanceof List ? (List<String>)packet.getPlayers() : new ArrayList<>(packet.getPlayers()));
	}

	@Override
	public PacketPlayOutPlayerInfo readPlayerInfo(Object packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
		List<PlayerInfoData> listData = new ArrayList<>();
		for (Object i : (List<Object>) listItem_getItems.invoke(packet)) {
			Component displayNameComponent = (Component) item_getDisplayName.invoke(i);
			String displayName = displayNameComponent == null ? null : GsonComponentSerializer.gson().serialize(displayNameComponent);
			listData.add(new PlayerInfoData((String) item_getName.invoke(i), (UUID) item_getUuid.invoke(i), item_getProperties.invoke(i), (int) item_getLatency.invoke(i),
					EnumGamemode.values()[(int) item_getGameMode.invoke(i)+1], displayName == null ? null : IChatBaseComponent.deserialize(displayName)));
		}
		return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.values()[(int) listItem_getAction.invoke(packet)], listData);
	}

	@Override
	public PacketPlayOutScoreboardObjective readObjective(Object packet) {
		return new PacketPlayOutScoreboardObjective(((ScoreboardObjective) packet).getAction(), ((ScoreboardObjective) packet).getName(), null, null);
	}

	@Override
	public PacketPlayOutScoreboardDisplayObjective readDisplayObjective(Object packet) {
		return new PacketPlayOutScoreboardDisplayObjective(((ScoreboardDisplay) packet).getPosition(), ((ScoreboardDisplay) packet).getName());
	}
}