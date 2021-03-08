package me.neznamy.tab.shared.features.types.event;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.types.Feature;

/**
 * Classes implementing this interface will receive respawn event
 */
public interface RespawnEventListener extends Feature {

	/**
	 * Processes the event
	 * @param respawned - player who respawned
	 */
	public void onRespawn(TabPlayer respawned);
}
