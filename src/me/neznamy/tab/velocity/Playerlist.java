package me.neznamy.tab.velocity;

import java.util.UUID;

import com.velocitypowered.proxy.protocol.packet.PlayerListItem;
import com.velocitypowered.proxy.protocol.packet.PlayerListItem.Item;

import me.neznamy.tab.shared.*;
import me.neznamy.tab.shared.Shared.Feature;
import net.kyori.text.Component;

public class Playerlist {

    public static final int ADD_PLAYER = 0;
    public static final int UPDATE_GAMEMODE = 1;
    public static final int UPDATE_LATENCY = 2;
    public static final int UPDATE_DISPLAY_NAME = 3;
    public static final int REMOVE_PLAYER = 4;
	public static boolean enable;
	public static int refresh;

	public static void load() {
		if (enable) {
			for (ITabPlayer p : Shared.getPlayers()) if (!p.disabledTablistNames) p.updatePlayerListName(true);
			Shared.scheduleRepeatingTask(refresh, "refreshing tablist prefix/suffix", Feature.PLAYERLIST_1, new Runnable() {
				public void run() {
					for (ITabPlayer p : Shared.getPlayers()) if (!p.disabledTablistNames) p.updatePlayerListName(true);
				}
			});
		}
	}
	public static void unload() {
		if (enable) for (ITabPlayer p : Shared.getPlayers()) p.setPlayerListName();
	}
	public static void modifyPacket(PlayerListItem packet, ITabPlayer receiver){
		Item playerInfoData = packet.getItems().get(0);
		UUID uuid = playerInfoData.getUuid();
		ITabPlayer player = Shared.getPlayerByOfflineUUID(uuid);
		if (packet.getAction() == UPDATE_GAMEMODE || packet.getAction() == ADD_PLAYER) {
			if (Configs.doNotMoveSpectators && playerInfoData.getGameMode() == 3 && uuid != receiver.getUniqueId()) playerInfoData.setGameMode(1);
		}
		if (packet.getAction() == UPDATE_DISPLAY_NAME || packet.getAction() == ADD_PLAYER) {
			if (player == null || player.disabledTablistNames || receiver.getVersion().getNumber() < ProtocolVersion.v1_8.getNumber()) return;
			String newName = player.getTabFormat(receiver);
			playerInfoData.setDisplayName((Component) Shared.mainClass.createComponent(newName));
		}
	}
}