package me.neznamy.tab.shared.features;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;

public class SpectatorFix implements CustomPacketFeature{

	@Override
	public UniversalPacketPlayOut onPacketSend(ITabPlayer receiver, UniversalPacketPlayOut packet) {
		if (!(packet instanceof PacketPlayOutPlayerInfo)) return packet;
		if (receiver.getVersion().getMinorVersion() < 8) return packet;
		PacketPlayOutPlayerInfo info = (PacketPlayOutPlayerInfo) packet;
		for (PlayerInfoData playerInfoData : info.players) {
			if (info.action == EnumPlayerInfoAction.UPDATE_GAME_MODE || info.action == EnumPlayerInfoAction.ADD_PLAYER) {
				if (playerInfoData.gamemode == EnumGamemode.SPECTATOR && playerInfoData.uniqueId != receiver.getTablistId()) playerInfoData.gamemode = EnumGamemode.CREATIVE;
			}
		}
		return info;
	}

	@Override
	public String getCPUName() {
		return "spectator fix";
	}
}