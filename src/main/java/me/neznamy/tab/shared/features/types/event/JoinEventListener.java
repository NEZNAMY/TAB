package me.neznamy.tab.shared.features.types.event;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.types.Feature;

/**
 * Classes implementing this interface will receive join event
 */
public interface JoinEventListener extends Feature {

	public void onJoin(TabPlayer connectedPlayer);
}
