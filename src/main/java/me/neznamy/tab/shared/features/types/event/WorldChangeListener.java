package me.neznamy.tab.shared.features.types.event;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.types.Feature;

/**
 * Classes implementing this interface will receive world/server switch event
 */
public interface WorldChangeListener extends Feature {

	/**
	 * Processes world/server switch
	 * @param changed - player who switched world/server
	 * @param from - world/server player changed from
	 * @param to - world/server player changed to
	 */
	public void onWorldChange(TabPlayer changed, String from, String to);
}
