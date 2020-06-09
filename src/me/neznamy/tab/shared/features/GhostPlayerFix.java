package me.neznamy.tab.shared.features;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.interfaces.QuitEventListener;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;

public class GhostPlayerFix implements QuitEventListener{

	@Override
	public void onQuit(ITabPlayer disconnectedPlayer) {
		Object packet = PacketAPI.buildPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, disconnectedPlayer.getInfoData()), null);
		Shared.featureCpu.runMeasuredTask("removing players", CPUFeature.GHOST_PLAYER_FIX, new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(100);
					for (ITabPlayer all : Shared.getPlayers()) {
						if (all == disconnectedPlayer) continue;
						all.sendPacket(packet);
					}
				} catch (InterruptedException e) {
					
				}
			}
		});
	}
}