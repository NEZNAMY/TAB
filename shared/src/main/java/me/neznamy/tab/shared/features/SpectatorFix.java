package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.packet.PlayerInfoPacketListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;

/**
 * Cancelling gamemode change packet to spectator gamemode to avoid players being moved on
 * the bottom of tablist with transparent name. Does not work on self as that would result
 * in players not being able to clip through walls.
 */
public class SpectatorFix implements PlayerInfoPacketListener, Loadable {

	//if bypass permission should be enabled
	private boolean allowBypass;
	
	/**
	 * Constructs new instance and loads config options
	 */
	public SpectatorFix() {
		allowBypass = TAB.getInstance().getConfiguration().config.getBoolean("allow-spectator-bypass-permission", false);
		TAB.getInstance().debug(String.format("Loaded SpectatorFix feature with parameters allowBypass=%s", allowBypass));
	}
	
	@Override
	public void onPacketSend(TabPlayer receiver, PacketPlayOutPlayerInfo info) {
		if (allowBypass && receiver.hasPermission("tab.spectatorbypass")) return;
		if (info.action != EnumPlayerInfoAction.UPDATE_GAME_MODE && info.action != EnumPlayerInfoAction.ADD_PLAYER) return;
		for (PlayerInfoData playerInfoData : info.entries) {
			if (playerInfoData.gameMode == EnumGamemode.SPECTATOR) {
				TabPlayer changed = TAB.getInstance().getPlayerByTablistUUID(playerInfoData.uniqueId);
				if (changed != receiver) playerInfoData.gameMode = EnumGamemode.CREATIVE;
			}
		}
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.SPECTATOR_FIX;
	}
	
	@Override
	public void load() {
		updateAll(false);
	}

	@Override
	public void unload() {
		updateAll(true);
	}
	
	private void updateAll(boolean realGamemode) {
		for (TabPlayer p : TAB.getInstance().getPlayers()) {
			if (allowBypass && p.hasPermission("tab.spectatorbypass")) continue;
			List<PlayerInfoData> list = new ArrayList<PlayerInfoData>();
			for (TabPlayer target : TAB.getInstance().getPlayers()) {
				if (p == target) continue;
				list.add(new PlayerInfoData(p.getUniqueId(), realGamemode ? EnumGamemode.values()[p.getGamemode()+1] : EnumGamemode.CREATIVE));
			}
			p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_GAME_MODE, list), getFeatureType());
		}
	}
}