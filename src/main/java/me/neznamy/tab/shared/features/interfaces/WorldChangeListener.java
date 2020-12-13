package me.neznamy.tab.shared.features.interfaces;

import me.neznamy.tab.api.TabPlayer;

/**
 * Classes implementing this interface will receive world/server switch event
 */
public interface WorldChangeListener extends Feature {

	public void onWorldChange(TabPlayer changed, String from, String to);
}
