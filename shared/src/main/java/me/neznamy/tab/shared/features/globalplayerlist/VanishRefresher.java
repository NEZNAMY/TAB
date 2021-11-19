package me.neznamy.tab.shared.features.globalplayerlist;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;

public class VanishRefresher extends TabFeature {

	private final GlobalPlayerlist playerlist;
	private final List<TabPlayer> vanishedPlayers = new ArrayList<>();
	
	protected VanishRefresher(GlobalPlayerlist playerlist) {
		super("Global Playerlist", "Updating vanished players");
		this.playerlist = playerlist;
		TAB.getInstance().getPlaceholderManager().addUsedPlaceholder("%vanished%", this);
	}
	
	@Override
	public void refresh(TabPlayer p, boolean force) {
		if (vanishedPlayers.contains(p) && !p.isVanished()) {
			vanishedPlayers.remove(p);
			for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
				if (viewer == p) continue;
				if (playerlist.shouldSee(viewer, p)) {
					viewer.sendCustomPacket(playerlist.getAddPacket(p, viewer), TabConstants.PacketCategory.GLOBAL_PLAYERLIST_VANISH);
				}
			}
		}
		if (!vanishedPlayers.contains(p) && p.isVanished()) {
			vanishedPlayers.add(p);
			for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
				if (all == p) continue;
				if (!playerlist.shouldSee(all, p)) {
					all.sendCustomPacket(playerlist.getRemovePacket(p), TabConstants.PacketCategory.GLOBAL_PLAYERLIST_VANISH);
				}
			}
		}
	}
	
	@Override
	public void onQuit(TabPlayer p) {
		vanishedPlayers.remove(p);
	}
}