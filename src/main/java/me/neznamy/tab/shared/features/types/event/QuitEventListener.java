package me.neznamy.tab.shared.features.types.event;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.types.Feature;

/**
 * Classes implementing this interface will receive quit event
 */
public interface QuitEventListener extends Feature {

	public void onQuit(TabPlayer disconnectedPlayer);
}
