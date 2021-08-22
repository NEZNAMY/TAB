package me.neznamy.tab.shared.features.globalplayerlist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

public class VanishRefresher extends TabFeature {

	private GlobalPlayerlist playerlist;
	private List<TabPlayer> vanishedPlayers = new ArrayList<>();
	
	protected VanishRefresher(GlobalPlayerlist playerlist) {
		super("Global playerlist");
		this.playerlist = playerlist;
		TAB.getInstance().getPlaceholderManager().getPlaceholderUsage().computeIfAbsent("%internal:vanished%", x -> new HashSet<>()).add(this);
	}
	
	@Override
	public void refresh(TabPlayer p, boolean force) {
		if (vanishedPlayers.contains(p) && !p.isVanished()) {
			vanishedPlayers.remove(p);
			for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
				if (viewer == p) continue;
				if (playerlist.shouldSee(viewer, p)) {
					viewer.sendCustomPacket(playerlist.getAddPacket(p, viewer), this);
				}
			}
		}
		if (!vanishedPlayers.contains(p) && p.isVanished()) {
			vanishedPlayers.add(p);
			for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
				if (all == p) continue;
				if (!playerlist.shouldSee(all, p)) {
					all.sendCustomPacket(playerlist.getRemovePacket(p), this);
				}
			}
		}
	}
	
	@Override
	public void onQuit(TabPlayer p) {
		vanishedPlayers.remove(p);
	}
}