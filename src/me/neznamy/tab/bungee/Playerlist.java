package me.neznamy.tab.bungee;

import java.util.UUID;

import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Action;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;

public class Playerlist {
	
	public static boolean enable;
	public static int refresh;
	
	public static void load() {
		if (enable) {
			for (ITabPlayer p : Shared.getPlayers()) {
				if (!p.disabledTablistNames) p.updatePlayerListName(true);
			}
			Shared.scheduleRepeatingTask(refresh, "refreshing tablist prefix/suffix", new Runnable() {
				
				public void run() {
					for (ITabPlayer p : Shared.getPlayers()) {
						if (!p.disabledTablistNames) p.updatePlayerListName(false);
					}
				}
			});
		}
	}
	public static void unload() {
		if (enable) for (ITabPlayer p : Shared.getPlayers()) p.setPlayerListName();
	}
	public static void modifyPacket(PlayerListItem packet, ITabPlayer receiver){
		if (!enable) return;
		Action action = packet.getAction();
	
		if (action == Action.UPDATE_LATENCY) return;
		
		Item playerInfoData = packet.getItems()[0];
		int gamemode = playerInfoData.getGamemode();
		UUID uuid = playerInfoData.getUuid();
		ITabPlayer player = Shared.getPlayer(uuid);
		
		if (action == Action.UPDATE_GAMEMODE) {
			if (Configs.doNotMoveSpectators && gamemode == 3 && uuid != receiver.getUniqueId()) playerInfoData.setGamemode(1);
		}
		if (action == Action.UPDATE_DISPLAY_NAME) {
			if (player == null || player.disabledTablistNames) return;
			String format = player.getTabFormat(receiver);
			playerInfoData.setDisplayName((String) Shared.mainClass.createComponent(format));
		}
		if (action == Action.ADD_PLAYER) {
			if (player != null) {
				if (!player.disabledTablistNames) {
					String format = player.getTabFormat(receiver);
					playerInfoData.setDisplayName((String) Shared.mainClass.createComponent(format));
					if (Configs.doNotMoveSpectators && gamemode == 3 && uuid != receiver.getUniqueId()) playerInfoData.setGamemode(1);
				}
			}
		}
	}
}