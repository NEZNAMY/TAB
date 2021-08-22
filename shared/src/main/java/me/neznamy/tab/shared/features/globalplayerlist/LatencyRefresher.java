package me.neznamy.tab.shared.features.globalplayerlist;

import java.util.HashSet;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.TAB;

public class LatencyRefresher extends TabFeature {

	public LatencyRefresher() {
		super("Global playerlist");
		TAB.getInstance().getPlaceholderManager().getPlaceholderUsage().computeIfAbsent("%ping%", x -> new HashSet<>()).add(this);
	}

	@Override
	public void refresh(TabPlayer p, boolean force) {
		//player ping changed, must manually update latency for players on other servers
		PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_LATENCY, new PlayerInfoData(p.getTablistUUID(), p.getPing()));
		for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
			if (!p.getServer().equals(all.getServer())) all.sendCustomPacket(packet, this);
		}
	}
}
