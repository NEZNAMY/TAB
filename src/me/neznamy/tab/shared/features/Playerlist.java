package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PluginHooks;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;

public class Playerlist {

	public static boolean enable;
	public static int refresh;

	public static void load(){
		if (enable) {
			updateNames(true);
			Shared.cpu.startRepeatingMeasuredTask(refresh, "refreshing tablist prefix/suffix", "Tablist names 1", new Runnable() {
				public void run() {
					updateNames(false);
				}
			});
		}
	}
	private static void updateNames(boolean force){
		List<PlayerInfoData> updatedPlayers = new ArrayList<PlayerInfoData>();
		for (ITabPlayer p : Shared.getPlayers()) {
			if (!p.disabledTablistNames && (p.isListNameUpdateNeeded() || force)) updatedPlayers.add(p.getInfoData());
		}
		if (!updatedPlayers.isEmpty()) {
			Object packet = ITabPlayer.buildPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, updatedPlayers), null);
			for (ITabPlayer all : Shared.getPlayers()) {
				if (all.getVersion().getMinorVersion() >= 8) all.sendPacket(packet);
			}
		}
	}
	public static void unload(){
		if (enable) updateNames(true);
	}
	public static void modifyPacket(PacketPlayOutPlayerInfo packet, ITabPlayer receiver){
		if (receiver.getVersion().getMinorVersion() < 8) return;
		List<PlayerInfoData> v180PrefixBugFixList = new ArrayList<PlayerInfoData>();
		for (PlayerInfoData playerInfoData : packet.players) {
			ITabPlayer packetPlayer = Shared.getPlayerByTablistUUID(playerInfoData.uniqueId);
			if (packet.action == EnumPlayerInfoAction.REMOVE_PLAYER && GlobalPlayerlist.enabled) {
				if (packetPlayer != null) { //player online
					if (!PluginHooks._isVanished(packetPlayer)) {
						//changing to random non-existing player, the easiest way to cancel the removal
						playerInfoData.uniqueId = UUID.randomUUID();
					}
				}
			}
			if (packet.action == EnumPlayerInfoAction.UPDATE_GAME_MODE || packet.action == EnumPlayerInfoAction.ADD_PLAYER) {
				if (Configs.doNotMoveSpectators && playerInfoData.gamemode == EnumGamemode.SPECTATOR && playerInfoData.uniqueId != receiver.getTablistId()) playerInfoData.gamemode = EnumGamemode.CREATIVE;
			}
			if (packet.action == EnumPlayerInfoAction.UPDATE_DISPLAY_NAME || packet.action == EnumPlayerInfoAction.ADD_PLAYER) {
				if (packetPlayer != null && !packetPlayer.disabledTablistNames) {
					playerInfoData.listName = packetPlayer.getTabFormat(receiver);
				}
			}
			if (packet.action == EnumPlayerInfoAction.ADD_PLAYER) {
				if (packetPlayer == null) {
					//NPC on bukkit
					if (Configs.unlimitedTags && Configs.modifyNPCnames) {
						if (playerInfoData.name.length() <= 15) {
							if (playerInfoData.name.length() <= 14) {
								playerInfoData.name += Shared.COLOR + "r";
							} else {
								playerInfoData.name += " ";
							}
						}
					}
				} else {
					v180PrefixBugFixList.add(playerInfoData.clone());
				}
			}
		}
		if (packet.action == EnumPlayerInfoAction.ADD_PLAYER && receiver.getVersion() == ProtocolVersion.v1_8) {
			Shared.cpu.runTaskLater(50, "sending PacketPlayOutPlayerInfo", "Tablist names 3", new Runnable() {

				@Override
				public void run() {
					receiver.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, v180PrefixBugFixList));
				}
			});
		}
	}
}