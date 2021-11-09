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
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * Packet builder for Velocity platform
 */
public class VelocityPacketBuilder extends PacketBuilder {

	@Override
	public Object build(PacketPlayOutBoss packet, ProtocolVersion clientVersion) {
		return packet;
	}

	@Override
	public Object build(PacketPlayOutChat packet, ProtocolVersion clientVersion) {
		return packet;
	}

	@Override
	public Object build(PacketPlayOutPlayerInfo packet, ProtocolVersion clientVersion) {
		return packet;
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
			color = (packet.getColor() != null ? packet.getColor() : EnumChatFormat.lastColorsOf(packet.getPlayerPrefix())).getNetworkId();
		}
		return new ScoreboardTeam(packet.getName(), (byte)packet.getMethod(), jsonOrCut(packet.getName(), clientVersion, 16), jsonOrCut(packet.getPlayerPrefix(), clientVersion, 16), jsonOrCut(packet.getPlayerSuffix(), clientVersion, 16),
				packet.getNametagVisibility(), packet.getCollisionRule(), color, (byte)packet.getOptions(), packet.getPlayers() instanceof List ? (List<String>)packet.getPlayers() : new ArrayList<>(packet.getPlayers()));
	}

	@Override
	public PacketPlayOutPlayerInfo readPlayerInfo(Object packet, ProtocolVersion clientVersion) {
		return null;
	}

	@Override
	public PacketPlayOutScoreboardObjective readObjective(Object packet, ProtocolVersion clientVersion) throws ParseException {
		ScoreboardObjective newPacket = (ScoreboardObjective) packet;
		String title;
		if (clientVersion.getMinorVersion() >= 13) {
			title = newPacket.getValue() == null ? null : IChatBaseComponent.deserialize(newPacket.getValue()).toLegacyText();
		} else {
			title = newPacket.getValue();
		}
		EnumScoreboardHealthDisplay renderType = (newPacket.getType() == null ? null : EnumScoreboardHealthDisplay.valueOf(newPacket.getType().toString().toUpperCase()));
		return new PacketPlayOutScoreboardObjective(newPacket.getAction(), newPacket.getName(), title, renderType);
	}

	@Override
	public PacketPlayOutScoreboardDisplayObjective readDisplayObjective(Object packet, ProtocolVersion clientVersion) {
		return new PacketPlayOutScoreboardDisplayObjective(((ScoreboardDisplay) packet).getPosition(), ((ScoreboardDisplay) packet).getName());
	}
}