package me.neznamy.tab.shared.features.types.event;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.types.Feature;

/**
 * Classes implementing this interface will receive sneak event
 */
public interface SneakEventListener extends Feature {

	/**
	 * Processes sneak event
	 * @param player - player who sneaked
	 * @param isSneaking - new sneak status
	 */
	public void onSneak(TabPlayer player, boolean isSneaking);
}