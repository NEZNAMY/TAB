package me.neznamy.tab.shared.features.globalplayerlist;

import java.util.*;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;

public class VanishRefresher extends TabFeature {

	private final GlobalPlayerList playerList;
	private final Set<TabPlayer> vanishedPlayers = Collections.newSetFromMap(new WeakHashMap<>());
	
	protected VanishRefresher(GlobalPlayerList playerList) {
		super("Global PlayerList", "Updating vanished players");
		this.playerList = playerList;
		TAB.getInstance().getPlaceholderManager().addUsedPlaceholder("%vanished%", this);
	}
	
	@Override
	public void refresh(TabPlayer p, boolean force) {
		if (vanishedPlayers.contains(p) && !p.isVanished()) {
			vanishedPlayers.remove(p);
			for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
				if (viewer == p) continue;
				if (playerList.shouldSee(viewer, p)) {
					viewer.sendCustomPacket(playerList.getAddPacket(p, viewer), TabConstants.PacketCategory.GLOBAL_PLAYERLIST_VANISH);
				}
			}
		}
		if (!vanishedPlayers.contains(p) && p.isVanished()) {
			vanishedPlayers.add(p);
			for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
				if (all == p) continue;
				if (!playerList.shouldSee(all, p)) {
					all.sendCustomPacket(playerList.getRemovePacket(p), TabConstants.PacketCategory.GLOBAL_PLAYERLIST_VANISH);
				}
			}
		}
	}
}