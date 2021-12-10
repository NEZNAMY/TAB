package me.neznamy.tab.platforms.bungeecord;

import java.util.ArrayList;
import java.util.List;

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
import net.md_5.bungee.protocol.packet.BossBar;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;
import net.md_5.bungee.protocol.packet.ScoreboardDisplay;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.ScoreboardObjective.HealthDisplay;
import net.md_5.bungee.protocol.packet.ScoreboardScore;
import net.md_5.bungee.protocol.packet.Team;

/**
 * Packet builder for BungeeCord platform
 */
public class BungeePacketBuilder extends PacketBuilder {

	@Override
	public Object build(PacketPlayOutBoss packet, ProtocolVersion clientVersion) {
		if (clientVersion.getMinorVersion() < 9) return null;
		BossBar bungeePacket = new BossBar(packet.getId(), packet.getOperation().ordinal());
		bungeePacket.setHealth(packet.getPct());
		bungeePacket.setTitle(packet.getName() == null ? null : IChatBaseComponent.optimizedComponent(packet.getName()).toString(clientVersion));
		bungeePacket.setColor(packet.getColor() == null ? 0 : packet.getColor().ordinal());
		bungeePacket.setDivision(packet.getOverlay() == null ? 0: packet.getOverlay().ordinal());
		bungeePacket.setFlags(packet.getFlags());
		return bungeePacket;
	}

	@Override
	public Object build(PacketPlayOutChat packet, ProtocolVersion clientVersion) {
		return new Chat(packet.getMessage().toString(clientVersion), (byte) packet.getType().ordinal());
	}

	@Override
	public Object build(PacketPlayOutPlayerInfo packet, ProtocolVersion clientVersion) {
		List<Item> items = new ArrayList<>();
		for (PlayerInfoData data : packet.getEntries()) {
			Item item = new Item();
			if (data.getDisplayName() != null) {
				if (clientVersion.getMinorVersion() >= 8) {
					item.setDisplayName(data.getDisplayName().toString(clientVersion));
				} else {
					item.setDisplayName(data.getDisplayName().toLegacyText());
				}
			} else if (clientVersion.getMinorVersion() < 8) {
				item.setDisplayName(data.getName()); //avoiding NPE, 1.7 client requires this, 1.8 added a leading boolean
			}
			if (data.getGameMode() != null) item.setGamemode(data.getGameMode().ordinal()-1);
			item.setPing(data.getLatency());
			if (data.getSkin() != null) {
				item.setProperties((String[][]) data.getSkin());
			} else {
				item.setProperties(new String[0][0]);
			}
			item.setUsername(data.getName());
			item.setUuid(data.getUniqueId());
			items.add(item);
		}
		PlayerListItem bungeePacket = new PlayerListItem();
		bungeePacket.setAction(PlayerListItem.Action.valueOf(packet.getAction().toString().replace("GAME_MODE", "GAMEMODE")));
		bungeePacket.setItems(items.toArray(new Item[0]));
		return bungeePacket;
	}

	@Override
	public Object build(PacketPlayOutPlayerListHeaderFooter packet, ProtocolVersion clientVersion) {
		return new PlayerListHeaderFooter(packet.getHeader().toString(clientVersion, true), packet.getFooter().toString(clientVersion, true));
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
		return new ScoreboardScore(packet.getPlayer(), (byte) packet.getAction().ordinal(), packet.getObjectiveName(), packet.getScore());
	}

	@Override
	public Object build(PacketPlayOutScoreboardTeam packet, ProtocolVersion clientVersion) {
		int color = 0;
		if (clientVersion.getMinorVersion() >= 13) {
			color = (packet.getColor() != null ? packet.getColor() : EnumChatFormat.lastColorsOf(packet.getPlayerPrefix())).ordinal();
		}
		return new Team(packet.getName(), (byte)packet.getMethod(), jsonOrCut(packet.getName(), clientVersion, 16), jsonOrCut(packet.getPlayerPrefix(), clientVersion, 16), jsonOrCut(packet.getPlayerSuffix(), clientVersion, 16), 
				packet.getNameTagVisibility(), packet.getCollisionRule(), color, (byte)packet.getOptions(), packet.getPlayers().toArray(new String[0]));
	}
	
	@Override
	public PacketPlayOutPlayerInfo readPlayerInfo(Object bungeePacket, ProtocolVersion clientVersion) {
		PlayerListItem item = (PlayerListItem) bungeePacket;
		List<PlayerInfoData> listData = new ArrayList<>();
		for (Item i : item.getItems()) {
			listData.add(new PlayerInfoData(i.getUsername(), i.getUuid(), i.getProperties(), i.getPing(), EnumGamemode.values()[i.getGamemode()+1], IChatBaseComponent.deserialize(i.getDisplayName())));
		}
		return new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.valueOf(item.getAction().toString().replace("GAMEMODE", "GAME_MODE")), listData);
	}

	@Override
	public PacketPlayOutScoreboardObjective readObjective(Object bungeePacket) {
		return new PacketPlayOutScoreboardObjective(((ScoreboardObjective) bungeePacket).getAction(), ((ScoreboardObjective) bungeePacket).getName(), null, null);
	}

	@Override
	public PacketPlayOutScoreboardDisplayObjective readDisplayObjective(Object bungeePacket){
		return new PacketPlayOutScoreboardDisplayObjective(((ScoreboardDisplay) bungeePacket).getPosition(), ((ScoreboardDisplay) bungeePacket).getName());
	}
}