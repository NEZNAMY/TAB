package me.neznamy.tab.platforms.velocity.event;

import me.neznamy.tab.api.TabPlayer;

/**
 * Velocity event that is called when player is successfully loaded after joining. This also includes plugin reloading.
 *
 * @deprecated for removal, use {@link me.neznamy.tab.api.event.player.PlayerLoadEvent}
 */
@Deprecated
public class TabPlayerLoadEvent {

	private final TabPlayer player;
	
	public TabPlayerLoadEvent(TabPlayer player) {
		this.player = player;
	}
	
	public TabPlayer getPlayer() {
		return player;
	}
}
