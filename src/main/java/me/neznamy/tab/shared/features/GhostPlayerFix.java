package me.neznamy.tab.shared.features;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.interfaces.QuitEventListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;

public class GhostPlayerFix implements QuitEventListener{

	@Override
	public void onQuit(ITabPlayer disconnectedPlayer) {
		Object removePacket = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, disconnectedPlayer.getInfoData()).build(ProtocolVersion.SERVER_VERSION);
		Shared.featureCpu.runTaskLater(100, "removing players", CPUFeature.GHOST_PLAYER_FIX, new Runnable() {

			@Override
			public void run() {
				for (ITabPlayer all : Shared.getPlayers()) {
					if (all == disconnectedPlayer) continue;
					all.sendPacket(removePacket);
				}
			}
		});
	}
}