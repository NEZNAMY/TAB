package me.neznamy.tab.platforms.bungee;

import java.util.UUID;

import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.Shared.Feature;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Action;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;

public class Playerlist {

	public static boolean enable;
	public static int refresh;

	public static void load() {
		if (enable) {
			for (ITabPlayer p : Shared.getPlayers()) if (!p.disabledTablistNames) p.updatePlayerListName(true);
			Shared.scheduleRepeatingTask(refresh, "refreshing tablist prefix/suffix", Feature.PLAYERLIST_1, new Runnable() {
				public void run() {
					for (ITabPlayer p : Shared.getPlayers()) if (!p.disabledTablistNames) p.updatePlayerListName(false);
				}
			});
		}
	}
	public static void unload() {
		if (enable) for (ITabPlayer p : Shared.getPlayers()) p.setPlayerListName();
	}
	public static void modifyPacket(PlayerListItem packet, ITabPlayer receiver){
		Item playerInfoData = packet.getItems()[0];
		UUID uuid = playerInfoData.getUuid();
		ITabPlayer player = Shared.getPlayerByOfflineUUID(uuid);
		if (packet.getAction() == Action.UPDATE_GAMEMODE || packet.getAction() == Action.ADD_PLAYER) {
			if (Configs.doNotMoveSpectators && playerInfoData.getGamemode() == 3 && uuid != receiver.getUniqueId()) playerInfoData.setGamemode(1);
		}
		if (packet.getAction() == Action.UPDATE_DISPLAY_NAME || packet.getAction() == Action.ADD_PLAYER) {
			if (player == null || player.disabledTablistNames || receiver.getVersion().getNumber() < ProtocolVersion.v1_8.getNumber()) return;
			playerInfoData.setDisplayName((String) Shared.mainClass.createComponent(player.getTabFormat(receiver)));
		}
	}
}