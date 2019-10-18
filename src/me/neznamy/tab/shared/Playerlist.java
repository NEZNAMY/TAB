package me.neznamy.tab.shared;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.shared.Shared.Feature;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;

public class Playerlist {

	public static boolean enable;
	public static int refresh;

	public static void load() {
		if (enable) {
			updateNames(true);
			Shared.scheduleRepeatingTask(refresh, "refreshing tablist prefix/suffix", Feature.PLAYERLIST_1, new Runnable() {
				public void run() {
					updateNames(false);
				}
			});
		}
	}
	private static void updateNames(boolean force) {
		List<PlayerInfoData> updatedPlayers = new ArrayList<PlayerInfoData>();
		for (ITabPlayer p : Shared.getPlayers()) {
			if (!p.disabledTablistNames && (p.isListNameUpdateNeeded() || force)) updatedPlayers.add(p.getInfoData());
		}
		if (!updatedPlayers.isEmpty()) {
			PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, updatedPlayers);
			for (ITabPlayer all : Shared.getPlayers()) {
				if (all.getVersion().getNetworkId() >= ProtocolVersion.v1_8.getNetworkId()) all.sendCustomPacket(packet);
			}
		}
	}
	public static void unload() {
		if (enable) {
			updateNames(true);
		}
	}
	public static void modifyPacket(PacketPlayOutPlayerInfo packet, ITabPlayer receiver){
		if (receiver.getVersion().getNetworkId() < ProtocolVersion.v1_8.getNetworkId()) return;
		for (PlayerInfoData playerInfoData : packet.players) {
			ITabPlayer packetPlayer = Shared.getPlayerByTablistUUID(playerInfoData.uniqueId);
			if (packet.action == EnumPlayerInfoAction.UPDATE_GAME_MODE || packet.action == EnumPlayerInfoAction.ADD_PLAYER) {
				if (Configs.doNotMoveSpectators && playerInfoData.gamemode == EnumGamemode.SPECTATOR && playerInfoData.uniqueId != receiver.getUniqueId()) playerInfoData.gamemode = EnumGamemode.CREATIVE;
			}
			if (packet.action == EnumPlayerInfoAction.UPDATE_DISPLAY_NAME || packet.action == EnumPlayerInfoAction.ADD_PLAYER) {
				if (packetPlayer != null && !packetPlayer.disabledTablistNames) playerInfoData.listName = packetPlayer.getTabFormat(receiver);
			}
			if (packet.action == EnumPlayerInfoAction.ADD_PLAYER) {
				if (packetPlayer == null) {
					//NPC on bukkit
					if (Configs.unlimitedTags && Configs.modifyNPCnames) {
						if (playerInfoData.name.length() <= 15) {
							if (playerInfoData.name.length() <= 14) {
								playerInfoData.name += "§r";
							} else {
								playerInfoData.name += " ";
							}
						}
					}
				}
			}
		}
	}
}