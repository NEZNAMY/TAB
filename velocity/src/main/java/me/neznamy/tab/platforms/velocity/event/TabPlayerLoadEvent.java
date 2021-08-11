package me.neznamy.tab.platforms.velocity.event;

import me.neznamy.tab.api.TabPlayer;

/**
 * Velocity event that is called when player is successfully loaded after joining. This also includes plugin reloading.
 */
public class TabPlayerLoadEvent {

	private TabPlayer player;
	
	public TabPlayerLoadEvent(TabPlayer player) {
		this.player = player;
	}
	
	public TabPlayer getPlayer() {
		return player;
	}
}