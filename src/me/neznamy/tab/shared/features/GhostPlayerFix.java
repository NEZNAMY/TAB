package me.neznamy.tab.shared.features;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;

public class GhostPlayerFix implements SimpleFeature{

	@Override
	public void load() {
	}
	@Override
	public void unload() {
	}
	@Override
	public void onJoin(ITabPlayer connectedPlayer) {
	}
	@Override
	public void onQuit(ITabPlayer disconnectedPlayer) {
		Object packet = PacketAPI.buildPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, disconnectedPlayer.getInfoData()), null);
		Shared.featureCpu.runMeasuredTask("removing players", "Ghost Player Fix", new Runnable() {

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
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
	}
}