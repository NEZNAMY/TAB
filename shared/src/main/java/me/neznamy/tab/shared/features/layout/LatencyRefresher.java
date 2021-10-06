package me.neznamy.tab.shared.features.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.CpuConstants;
import me.neznamy.tab.shared.TAB;

public class LatencyRefresher extends TabFeature {

	private Layout layout;
	
	public LatencyRefresher(Layout layout) {
		super("Layout - LatencyRefresher");
		this.layout = layout;
		addUsedPlaceholders(Arrays.asList("%ping%"));
	}
	
	@Override
	public void load() {
		for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
			onJoin(p);
		}
	}
	
	@Override
	public void onJoin(TabPlayer p) {
		refresh(p, false);
		for (ParentGroup group : layout.getGroups()) {
			List<PlayerInfoData> list = new ArrayList<>();
			for (TabPlayer all : group.getPlayers().keySet()) {
				if (all == p) continue; //already sent in refresh
				list.add(new PlayerInfoData(group.getPlayers().get(all).getUUID(), all.getPing()));
			}
			if (p.getVersion().getMinorVersion() < 8 || p.isBedrockPlayer()) continue;
			p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_LATENCY, list), CpuConstants.PacketCategory.LAYOUT_LATENCY);
		}
	}
	
	@Override
	public void refresh(TabPlayer p, boolean force) {
		for (ParentGroup group : layout.getGroups()) {
			if (group.getPlayers().get(p) != null) {
				PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_LATENCY, new PlayerInfoData(group.getPlayers().get(p).getUUID(), p.getPing()));
				for (TabPlayer all : layout.getViewers()) {
					if (all.getVersion().getMinorVersion() < 8 || all.isBedrockPlayer()) continue;
					all.sendCustomPacket(packet, CpuConstants.PacketCategory.LAYOUT_LATENCY);
				}
			}
		}
	}
}
