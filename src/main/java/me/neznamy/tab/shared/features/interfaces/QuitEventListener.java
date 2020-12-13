package me.neznamy.tab.shared.features.interfaces;

import me.neznamy.tab.api.TabPlayer;

/**
 * Classes implementing this interface will receive quit event
 */
public interface QuitEventListener extends Feature {

	public void onQuit(TabPlayer disconnectedPlayer);
}
