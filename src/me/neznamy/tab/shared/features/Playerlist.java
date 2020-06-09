package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.interfaces.CustomPacketFeature;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;

public class Playerlist implements Loadable, WorldChangeListener, CustomPacketFeature{

	public void load(){
		int refresh = Configs.config.getInt("tablist-refresh-interval-milliseconds", 1000);
		if (refresh < 50) Shared.errorManager.refreshTooLow("Tablist prefix/suffix", refresh);
		updateNames(true);
		Shared.featureCpu.startRepeatingMeasuredTask(refresh, "refreshing tablist prefix/suffix", CPUFeature.TABLIST_NAMES_1, new Runnable() {
			public void run() {
				updateNames(false);
			}
		});
	}
	public void unload(){
		updateNames(true);
	}
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		if (!Configs.disabledTablistNames.contains("NORESET")) p.updatePlayerListName();
	}
	private void updateNames(boolean force){
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() < 8) return;
		List<PlayerInfoData> updatedPlayers = new ArrayList<PlayerInfoData>();
		for (ITabPlayer p : Shared.getPlayers()) {
			if (!p.disabledTablistNames && (p.isListNameUpdateNeeded() || force)) updatedPlayers.add(p.getInfoData());
		}
		if (!updatedPlayers.isEmpty()) {
			for (ITabPlayer all : Shared.getPlayers()) {
				all.sendPacket(PacketAPI.buildPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, updatedPlayers), all.getVersion()));
			}
		}
	}
	@Override
	public UniversalPacketPlayOut onPacketSend(ITabPlayer receiver, UniversalPacketPlayOut packet) {
		if (!(packet instanceof PacketPlayOutPlayerInfo)) return packet;
		if (receiver.getVersion().getMinorVersion() < 8) return packet;
		PacketPlayOutPlayerInfo info = (PacketPlayOutPlayerInfo) packet;
		boolean UPDATE_NAME = info.action == EnumPlayerInfoAction.UPDATE_DISPLAY_NAME;
		boolean ADD = info.action == EnumPlayerInfoAction.ADD_PLAYER;
		if (!UPDATE_NAME && !ADD) return packet;
		List<PlayerInfoData> v180PrefixBugFixList = new ArrayList<PlayerInfoData>();
		for (PlayerInfoData playerInfoData : info.entries) {
			ITabPlayer packetPlayer = Shared.getPlayerByTablistUUID(playerInfoData.uniqueId);
			if (packetPlayer != null && !packetPlayer.disabledTablistNames && packetPlayer.isConnected()) {
				playerInfoData.displayName = packetPlayer.getTabFormat(receiver);
				if (ADD) {
					//preventing plugins from changing player name as nametag feature would not work correctly
					if ((Shared.features.containsKey("nametag16") || Shared.features.containsKey("nametagx")) && !playerInfoData.name.equals(packetPlayer.getName())) {
						Shared.debug("Blocking name change of player " +  packetPlayer.getName() + " to " + playerInfoData.name + " for " + receiver.getName());
						playerInfoData.name = packetPlayer.getName();
					}
				}
			}
			if (ADD) {
				if (packetPlayer != null && receiver.getVersion() == ProtocolVersion.v1_8) v180PrefixBugFixList.add(playerInfoData.clone());
			}
		}
		if (ADD && receiver.getVersion() == ProtocolVersion.v1_8) {
			//1.8.0 bug, sending to all 1.8.x clients as there is no way to find out if they use 1.8.0
			Shared.featureCpu.runTaskLater(50, "sending PacketPlayOutPlayerInfo", CPUFeature.TABLIST_NAMES_3, new Runnable() {

				@Override
				public void run() {
					receiver.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, v180PrefixBugFixList));
				}
			});
		}
		return info;
	}
	@Override
	public CPUFeature getCPUName() {
		return CPUFeature.TABLIST_NAMES_2;
	}
}