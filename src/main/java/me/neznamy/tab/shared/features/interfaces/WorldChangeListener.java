package me.neznamy.tab.shared.features.interfaces;

import me.neznamy.tab.shared.ITabPlayer;

/**
 * Classes implementing this interface will receive world/server switch event
 */
public interface WorldChangeListener {

	public void onWorldChange(ITabPlayer changed, String from, String to);
}
