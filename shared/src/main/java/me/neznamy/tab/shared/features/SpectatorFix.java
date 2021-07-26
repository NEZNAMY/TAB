package me.neznamy.tab.shared.features;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.TAB;

/**
 * Cancelling gamemode change packet to spectator gamemode to avoid players being moved on
 * the bottom of tablist with transparent name. Does not work on self as that would result
 * in players not being able to clip through walls.
 */
public class SpectatorFix extends TabFeature {

	/**
	 * Constructs new instance and loads config options
	 */
	public SpectatorFix() {
		super("Spectator fix");
		TAB.getInstance().debug("Loaded SpectatorFix feature");
	}
	
	@Override
	public void onPacketSend(TabPlayer receiver, PacketPlayOutPlayerInfo info) {
		if (receiver.hasPermission("tab.spectatorbypass")) return;
		if (info.getAction() != EnumPlayerInfoAction.UPDATE_GAME_MODE && info.getAction() != EnumPlayerInfoAction.ADD_PLAYER) return;
		for (PlayerInfoData playerInfoData : info.getEntries()) {
			if (playerInfoData.getGameMode() == EnumGamemode.SPECTATOR) {
				TabPlayer changed = TAB.getInstance().getPlayerByTablistUUID(playerInfoData.getUniqueId());
				if (changed != receiver) playerInfoData.setGameMode(EnumGamemode.CREATIVE);
			}
		}
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
		for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
			if (p.hasPermission("tab.spectatorbypass")) continue;
			List<PlayerInfoData> list = new ArrayList<>();
			for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
				if (p == target) continue;
				list.add(new PlayerInfoData(p.getUniqueId(), realGamemode ? EnumGamemode.values()[p.getGamemode()+1] : EnumGamemode.CREATIVE));
			}
			p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_GAME_MODE, list), this);
		}
	}
}