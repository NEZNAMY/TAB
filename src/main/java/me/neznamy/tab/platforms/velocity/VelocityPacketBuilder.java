package me.neznamy.tab.platforms.velocity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.velocitypowered.api.util.GameProfile.Property;
import com.velocitypowered.proxy.protocol.packet.BossBar;
import com.velocitypowered.proxy.protocol.packet.Chat;
import com.velocitypowered.proxy.protocol.packet.HeaderAndFooter;
import com.velocitypowered.proxy.protocol.packet.PlayerListItem;
import com.velocitypowered.proxy.protocol.packet.PlayerListItem.Item;
import com.velocitypowered.proxy.protocol.packet.TitlePacket;

import me.neznamy.tab.platforms.velocity.protocol.ScoreboardDisplay;
import me.neznamy.tab.platforms.velocity.protocol.ScoreboardObjective;
import me.neznamy.tab.platforms.velocity.protocol.ScoreboardObjective.HealthDisplay;
import me.neznamy.tab.platforms.velocity.protocol.ScoreboardScore;
import me.neznamy.tab.platforms.velocity.protocol.Team;
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
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

/**
 * Packet builder for Velocity platform
 */
public class VelocityPacketBuilder implements PacketBuilder {

	@Override
	public Object build(PacketPlayOutBoss packet, ProtocolVersion clientVersion) {
		if (clientVersion.getMinorVersion() < 9) return null;
		BossBar velocityPacket = new BossBar();
		velocityPacket.setUuid(packet.id);
		velocityPacket.setAction(packet.operation.ordinal());
		velocityPacket.setPercent(packet.pct);
		velocityPacket.setName(packet.name == null ? null : IChatBaseComponent.optimizedComponent(packet.name).toString(clientVersion));
		velocityPacket.setColor(packet.color == null ? 0 : packet.color.ordinal());
		velocityPacket.setOverlay(packet.overlay == null ? 0 : packet.overlay.ordinal());
		velocityPacket.setFlags(packet.getFlags());
		return velocityPacket;
	}

	@Override
	public Object build(PacketPlayOutChat packet, ProtocolVersion clientVersion) {
		return new Chat(packet.message.toString(clientVersion), (byte) packet.type.ordinal(), UUID.randomUUID());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object build(PacketPlayOutPlayerInfo packet, ProtocolVersion clientVersion) {
		List<Item> items = new ArrayList<Item>();
		for (PlayerInfoData data : packet.entries) {
			Item item = new Item(data.uniqueId);
			item.setDisplayName(data.displayName == null ? null : GsonComponentSerializer.gson().deserialize(data.displayName.toString(clientVersion)));
			if (data.gameMode != null) item.setGameMode(data.gameMode.ordinal()-1);
			item.setLatency(data.latency);
			item.setProperties((List<Property>) data.skin);
			item.setName(data.name);
			items.add(item);
		}
		return new PlayerListItem(packet.action.ordinal(), items);
	}

	@Override
	public Object build(PacketPlayOutPlayerListHeaderFooter packet, ProtocolVersion clientVersion) {
		return new HeaderAndFooter(packet.header.toString(clientVersion, true), packet.footer.toString(clientVersion, true));
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
		TitlePacket velocityPacket = new TitlePacket();
		int actionId = packet.action.ordinal();
	    if (clientVersion.getNetworkId() <= ProtocolVersion.v1_10_2.getNetworkId() && actionId >= 2) {
	    	actionId--;
	    }
	    velocityPacket.setAction(actionId);
	    velocityPacket.setComponent(IChatBaseComponent.optimizedComponent(packet.text).toString(clientVersion));
	    velocityPacket.setFadeIn(packet.fadeIn);
	    velocityPacket.setStay(packet.stay);
	    velocityPacket.setFadeOut(packet.fadeOut);
		return velocityPacket;
	}
	
	@Override
	public PacketPlayOutPlayerInfo readPlayerInfo(Object velocityPacket, ProtocolVersion clientVersion){
		PlayerListItem list = (PlayerListItem) velocityPacket;
		List<PlayerInfoData> listData = new ArrayList<PlayerInfoData>();
		for (PlayerListItem.Item item : list.getItems()) {
			String displayName = item.getDisplayName() == null ? null : GsonComponentSerializer.gson().serialize(item.getDisplayName());
			listData.add(new PlayerInfoData(item.getName(), item.getUuid(), item.getProperties(), item.getLatency(), EnumGamemode.values()[item.getGameMode()+1], displayName == null ? null : IChatBaseComponent.fromString(displayName)));
		}
		return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.values()[(list.getAction())], listData);
	}
	
	@Override
	public PacketPlayOutScoreboardObjective readObjective(Object bungeePacket, ProtocolVersion clientVersion) throws Exception {
		ScoreboardObjective packet = (ScoreboardObjective) bungeePacket;
		String title;
		if (clientVersion.getMinorVersion() >= 13) {
			title = IChatBaseComponent.fromString(packet.value).toLegacyText();
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