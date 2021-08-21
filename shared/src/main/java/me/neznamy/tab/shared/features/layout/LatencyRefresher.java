package me.neznamy.tab.shared.features.layout;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.PropertyUtils;
import me.neznamy.tab.shared.TAB;

public class LatencyRefresher extends TabFeature {

	private Layout layout;
	
	public LatencyRefresher(Layout layout) {
		super("Layout - LatencyRefresher");
		this.layout = layout;
	}
	
	@Override
	public void load() {
		for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
			onJoin(p);
		}
	}
	
	@Override
	public void onJoin(TabPlayer p) {
		p.setProperty(this, PropertyUtils.LAYOUT_LATENCY, "%ping%");
		refresh(p, false);
		for (ParentGroup group : layout.getGroups()) {
			List<PlayerInfoData> list = new ArrayList<>();
			for (TabPlayer all : group.getPlayers().keySet()) {
				if (all == p) continue; //already sent in refresh
				list.add(new PlayerInfoData(group.getPlayers().get(all).getUUID(), all.getPing()));
			}
			p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_LATENCY, list), this);
		}
	}
	
	@Override
	public void refresh(TabPlayer p, boolean force) {
		for (ParentGroup group : layout.getGroups()) {
			if (group.getPlayers().get(p) != null) {
				PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_LATENCY, new PlayerInfoData(group.getPlayers().get(p).getUUID(), p.getPing()));
				for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
					all.sendCustomPacket(packet, this);
				}
			}
		}
	}
}
