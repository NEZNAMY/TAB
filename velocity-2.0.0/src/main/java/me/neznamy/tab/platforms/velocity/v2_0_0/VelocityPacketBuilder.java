package me.neznamy.tab.platforms.velocity.v2_0_0;

import java.util.ArrayList;
import java.util.List;

import com.velocitypowered.api.util.GameProfile.Property;
import com.velocitypowered.proxy.network.packet.clientbound.ClientboundPlayerListItemPacket;
import com.velocitypowered.proxy.network.packet.clientbound.ClientboundPlayerListItemPacket.Item;
import com.velocitypowered.proxy.network.packet.clientbound.ClientboundTitlePacket;

import me.neznamy.tab.platforms.velocity.v2_0_0.packet.ScoreboardDisplay;
import me.neznamy.tab.platforms.velocity.v2_0_0.packet.ScoreboardObjective;
import me.neznamy.tab.platforms.velocity.v2_0_0.packet.ScoreboardObjective.HealthDisplay;
import me.neznamy.tab.platforms.velocity.v2_0_0.packet.ScoreboardScore;
import me.neznamy.tab.platforms.velocity.v2_0_0.packet.Team;
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
import me.neznamy.tab.shared.packets.PacketPlayOutTitle.EnumTitleAction;
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

	@SuppressWarnings("unchecked")
	@Override
	public Object build(PacketPlayOutPlayerInfo packet, ProtocolVersion clientVersion) {
		List<Item> items = new ArrayList<Item>();
		for (PlayerInfoData data : packet.entries) {
			Item item = new Item(data.uniqueId);
			if (data.displayName != null) item.setDisplayName(Main.stringToComponent(data.displayName.toString(clientVersion)));
			if (data.gameMode != null) item.setGameMode(data.gameMode.ordinal()-1);
			item.setLatency(data.latency);
			item.setProperties((List<Property>) data.skin);
			item.setName(data.name);
			items.add(item);
		}
		return new ClientboundPlayerListItemPacket(packet.action.ordinal(), items);
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
	public Object build(PacketPlayOutTitle packet, ProtocolVersion clientVersion) {
		int actionId = packet.action.ordinal();
		if (clientVersion.getNetworkId() <= ProtocolVersion.v1_10_2.getNetworkId() && actionId >= 2) {
			actionId--;
		}
		if (packet.action == EnumTitleAction.TIMES) {
			return new ClientboundTitlePacket(actionId, packet.fadeIn, packet.stay, packet.fadeOut);
		} else {
			return new ClientboundTitlePacket(actionId, packet.text == null ? null : IChatBaseComponent.optimizedComponent(packet.text).toString(clientVersion));
		}
	}

	@Override
	public PacketPlayOutPlayerInfo readPlayerInfo(Object packet, ProtocolVersion clientVersion) {
		ClientboundPlayerListItemPacket velocityPacket = (ClientboundPlayerListItemPacket) packet;
		List<PlayerInfoData> listData = new ArrayList<PlayerInfoData>();
		for (Item item : velocityPacket.getItems()) {
			Component displayNameComponent = item.getDisplayName();
			String displayName = displayNameComponent == null ? null : GsonComponentSerializer.gson().serialize(displayNameComponent);
			listData.add(new PlayerInfoData(item.getName(), item.getUuid(), item.getProperties(), item.getLatency(), EnumGamemode.values()[item.getGameMode()+1], displayName == null ? null : IChatBaseComponent.fromString(displayName)));
		}
		return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.values()[velocityPacket.getAction()], listData);
	}

	@Override
	public PacketPlayOutScoreboardObjective readObjective(Object velocityPacket, ProtocolVersion clientVersion) {
		ScoreboardObjective packet = (ScoreboardObjective) velocityPacket;
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
	public PacketPlayOutScoreboardDisplayObjective readDisplayObjective(Object packet, ProtocolVersion clientVersion) {
		return new PacketPlayOutScoreboardDisplayObjective(((ScoreboardDisplay) packet).position, ((ScoreboardDisplay) packet).name);
	}
}