package me.neznamy.tab.shared.features.types.event;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.types.Feature;

/**
 * Classes implementing this interface will receive world/server switch event
 */
public interface WorldChangeListener extends Feature {

	public void onWorldChange(TabPlayer changed, String from, String to);
}
