package me.neznamy.tab.shared.features;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.types.packet.PlayerInfoPacketListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;

/**
 * Feature handler for spectator fix feature
 */
public class SpectatorFix implements PlayerInfoPacketListener {

	private TAB tab;
	private boolean allowBypass;
	
	public SpectatorFix(TAB tab) {
		this.tab = tab;
		allowBypass = tab.getConfiguration().config.getBoolean("allow-spectator-bypass-permission", false);
	}
	
	@Override
	public void onPacketSend(TabPlayer receiver, PacketPlayOutPlayerInfo info) {
		if (allowBypass && receiver.hasPermission("tab.spectatorbypass")) return;
		if (info.action != EnumPlayerInfoAction.UPDATE_GAME_MODE && info.action != EnumPlayerInfoAction.ADD_PLAYER) return;
		for (PlayerInfoData playerInfoData : info.entries) {
			if (playerInfoData.gameMode == EnumGamemode.SPECTATOR) {
				TabPlayer changed = tab.getPlayerByTablistUUID(playerInfoData.uniqueId);
				if (changed != receiver) playerInfoData.gameMode = EnumGamemode.CREATIVE;
			}
		}
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.SPECTATOR_FIX;
	}
}