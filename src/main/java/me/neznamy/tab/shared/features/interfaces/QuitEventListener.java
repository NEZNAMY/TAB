package me.neznamy.tab.shared.features.interfaces;

import me.neznamy.tab.shared.ITabPlayer;

/**
 * Classes implementing this interface will receive quit event
 */
public interface QuitEventListener {

	public void onQuit(ITabPlayer disconnectedPlayer);
}
