package me.neznamy.tab.shared.features;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.interfaces.QuitEventListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;

/**
 * A small class fixing bugs in other plugins
 */
public class GhostPlayerFix implements QuitEventListener {

	@Override
	public void onQuit(ITabPlayer disconnectedPlayer) {
		Object removePacket = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, disconnectedPlayer.getInfoData()).create(ProtocolVersion.SERVER_VERSION);
		Shared.cpu.runTaskLater(100, "removing players", getFeatureType(), UsageType.PLAYER_QUIT_EVENT, new Runnable() {

			@Override
			public void run() {
				for (ITabPlayer all : Shared.getPlayers()) {
					if (all == disconnectedPlayer) continue;
					all.sendPacket(removePacket);
				}
			}
		});
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.GHOST_PLAYER_FIX;
	}
}