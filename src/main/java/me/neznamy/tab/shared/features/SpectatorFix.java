package me.neznamy.tab.shared.features;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.CPUFeature;
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
	public PacketPlayOutPlayerInfo onPacketSend(ITabPlayer receiver, PacketPlayOutPlayerInfo info) {
		if (receiver.getVersion().getMinorVersion() < 8) return info;
		if (allowBypass && receiver.hasPermission("tab.spectatorbypass")) return info;
		if (info.action != EnumPlayerInfoAction.UPDATE_GAME_MODE && info.action != EnumPlayerInfoAction.ADD_PLAYER) return info;
		for (PlayerInfoData playerInfoData : info.entries) {
			if (playerInfoData.gameMode == EnumGamemode.SPECTATOR) {
				ITabPlayer changed = Shared.getPlayerByTablistUUID(playerInfoData.uniqueId);
				if (changed != receiver) playerInfoData.gameMode = EnumGamemode.CREATIVE;
			}
		}
		return info;
	}

	@Override
	public CPUFeature getCPUName() {
		return CPUFeature.SPECTATOR_FIX;
	}
}