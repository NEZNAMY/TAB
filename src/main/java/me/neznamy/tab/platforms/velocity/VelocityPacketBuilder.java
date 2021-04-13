package me.neznamy.tab.platforms.velocity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.velocitypowered.proxy.protocol.packet.ScoreboardDisplay;
import com.velocitypowered.proxy.protocol.packet.ScoreboardObjective;
import com.velocitypowered.proxy.protocol.packet.ScoreboardObjective.HealthDisplay;
import com.velocitypowered.proxy.protocol.packet.ScoreboardScore;
import com.velocitypowered.proxy.protocol.packet.Team;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.packets.EnumChatFormat;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketBuilder;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

/**
 * Packet builder for Velocity platform
 */
public class VelocityPacketBuilder implements PacketBuilder {

	@Override
	public Object build(PacketPlayOutBoss packet, ProtocolVersion clientVersion) {
		return packet;
	}

	@Override
	public Object build(PacketPlayOutChat packet, ProtocolVersion clientVersion) {
		return packet;
	}

	@Override
	public Object build(PacketPlayOutPlayerInfo packet, ProtocolVersion clientVersion) throws Exception {
		List<Object> items = new ArrayList<Object>();
		for (PlayerInfoData data : packet.entries) {
			Object item = Class.forName("com.velocitypowered.proxy.protocol.packet.PlayerListItem$Item").getConstructor(UUID.class).newInstance(data.uniqueId);
			item.getClass().getMethod("setDisplayName", Component.class).invoke(item, data.displayName == null ? null : Main.stringToComponent(data.displayName.toString(clientVersion)));
			if (data.gameMode != null) {
				item.getClass().getMethod("setGameMode", int.class).invoke(item, data.gameMode.ordinal()-1);
			}
			item.getClass().getMethod("setLatency", int.class).invoke(item, data.latency);
			item.getClass().getMethod("setProperties", List.class).invoke(item, data.skin);
			item.getClass().getMethod("setName", String.class).invoke(item, data.name);
			items.add(item);
		}
		return Class.forName("com.velocitypowered.proxy.protocol.packet.PlayerListItem").getConstructor(int.class, List.class).newInstance(packet.action.ordinal(), items);
	}

	@Override
	public Object build(PacketPlayOutPlayerListHeaderFooter packet, ProtocolVersion clientVersion) {
		return packet;
	}

	@Override
	public Object build(PacketPlayOutScoreboardDisplayObjective packet, ProtocolVersion clientVersion) {
		return new ScoreboardDisplay((byte)packet.slot, packet.objectiveName);
	}

	@Override
	public Object build(PacketPlayOutScoreboardObjective packet, ProtocolVersion clientVersion) {
		return new ScoreboardObjective(packet.objectiveName, jsonOrCut(packet.displayName, clientVersion, 32), packet.renderType == null ? null : HealthDisplay.valueOf(packet.renderType.toString()), (byte)packet.method);
	}

	@Override
	public Object build(PacketPlayOutScoreboardScore packet, ProtocolVersion clientVersion) {
		return new ScoreboardScore(packet.player, (byte) packet.action.ordinal(), packet.objectiveName, packet.score);
	}

	@Override
	public Object build(PacketPlayOutScoreboardTeam packet, ProtocolVersion clientVersion) {
		int color = 0;
		if (clientVersion.getMinorVersion() >= 13) {
			color = (packet.color != null ? packet.color : EnumChatFormat.lastColorsOf(packet.playerPrefix)).getNetworkId();
		}
		return new Team(packet.name, (byte)packet.method, jsonOrCut(packet.name, clientVersion, 16), jsonOrCut(packet.playerPrefix, clientVersion, 16), jsonOrCut(packet.playerSuffix, clientVersion, 16), 
				packet.nametagVisibility, packet.collisionRule, color, (byte)packet.options, packet.players.toArray(new String[0]));
	}

	@Override
	public Object build(PacketPlayOutTitle packet, ProtocolVersion clientVersion) throws Exception {
		Object velocityPacket = Class.forName("com.velocitypowered.proxy.protocol.packet.TitlePacket").getConstructor().newInstance();
		int actionId = packet.action.ordinal();
		if (clientVersion.getNetworkId() <= ProtocolVersion.v1_10_2.getNetworkId() && actionId >= 2) {
			actionId--;
		}
		velocityPacket.getClass().getMethod("setAction", int.class).invoke(velocityPacket, actionId);
		if (packet.text != null) velocityPacket.getClass().getMethod("setComponent", String.class).invoke(velocityPacket, IChatBaseComponent.optimizedComponent(packet.text).toString(clientVersion));
		velocityPacket.getClass().getMethod("setFadeIn", int.class).invoke(velocityPacket, packet.fadeIn);
		velocityPacket.getClass().getMethod("setStay", int.class).invoke(velocityPacket, packet.stay);
		velocityPacket.getClass().getMethod("setFadeOut", int.class).invoke(velocityPacket, packet.fadeOut);
		return velocityPacket;
	}

	@SuppressWarnings("unchecked")
	@Override
	public PacketPlayOutPlayerInfo readPlayerInfo(Object packet, ProtocolVersion clientVersion) throws Exception{
		List<PlayerInfoData> listData = new ArrayList<PlayerInfoData>();
		for (Object item : (List<Object>) packet.getClass().getMethod("getItems").invoke(packet)) {
			Component displayNameComponent = (Component) item.getClass().getMethod("getDisplayName").invoke(item);
			String displayName = displayNameComponent == null ? null : GsonComponentSerializer.gson().serialize(displayNameComponent);
			listData.add(new PlayerInfoData(
					(String) item.getClass().getMethod("getName").invoke(item), 
					(UUID) item.getClass().getMethod("getUuid").invoke(item), 
					item.getClass().getMethod("getProperties").invoke(item), 
					(int) item.getClass().getMethod("getLatency").invoke(item), 
					EnumGamemode.values()[(int)item.getClass().getMethod("getGameMode").invoke(item)+1], 
					displayName == null ? null : IChatBaseComponent.fromString(displayName)
				)
			);
		}
		return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.values()[(int) (packet.getClass().getMethod("getAction").invoke(packet))], listData);
	}

	@Override
	public PacketPlayOutScoreboardObjective readObjective(Object bungeePacket, ProtocolVersion clientVersion) throws Exception {
		ScoreboardObjective packet = (ScoreboardObjective) bungeePacket;
		String title;
		if (clientVersion.getMinorVersion() >= 13) {
			title = packet.value == null ? null : IChatBaseComponent.fromString(packet.value).toLegacyText();
		} else {
			title = packet.value;
		}
		EnumScoreboardHealthDisplay renderType = (packet.type == null ? null : EnumScoreboardHealthDisplay.valueOf(packet.type.toString().toUpperCase()));
		return new PacketPlayOutScoreboardObjective(packet.action, packet.name, title, renderType);
	}

	@Override
	public PacketPlayOutScoreboardDisplayObjective readDisplayObjective(Object bungeePacket, ProtocolVersion clientVersion) throws Exception {
		return new PacketPlayOutScoreboardDisplayObjective(((ScoreboardDisplay) bungeePacket).position, ((ScoreboardDisplay) bungeePacket).name);
	}
}