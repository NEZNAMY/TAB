package me.neznamy.tab.shared.features;

import me.neznamy.tab.shared.ITabPlayer;
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
	public void onJoin(ITabPlayer p) {
	}

	@Override
	public void onQuit(ITabPlayer p) {
		Object packet = ITabPlayer.buildPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, p.getInfoData()), null);
		for (ITabPlayer all : Shared.getPlayers()) {
			all.sendPacket(packet);
		}
	}
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
	}
}