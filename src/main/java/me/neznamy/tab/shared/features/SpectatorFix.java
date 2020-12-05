package me.neznamy.tab.shared.features;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.interfaces.PlayerInfoPacketListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;

/**
 * Feature handler for spectator fix feature
 */
public class SpectatorFix implements PlayerInfoPacketListener {

	private boolean allowBypass;
	
	public SpectatorFix() {
		allowBypass = Configs.config.getBoolean("allow-spectator-bypass-permission", false);
	}
	
	@Override
	public void onPacketSend(TabPlayer receiver, PacketPlayOutPlayerInfo info) {
		if (allowBypass && receiver.hasPermission("tab.spectatorbypass")) return;
		if (info.action != EnumPlayerInfoAction.UPDATE_GAME_MODE && info.action != EnumPlayerInfoAction.ADD_PLAYER) return;
		for (PlayerInfoData playerInfoData : info.entries) {
			if (playerInfoData.gameMode == EnumGamemode.SPECTATOR) {
				TabPlayer changed = Shared.getPlayerByTablistUUID(playerInfoData.uniqueId);
				if (changed != receiver) playerInfoData.gameMode = EnumGamemode.CREATIVE;
			}
		}
	}

	/**
	 * Returns name of the feature displayed in /tab cpu
	 * @return name of the feature displayed in /tab cpu
	 */
	@Override
	public TabFeature getFeatureType() {
		return TabFeature.SPECTATOR_FIX;
	}
}