package me.neznamy.tab.platforms.bungeecord.event;

import me.neznamy.tab.api.TabPlayer;
import net.md_5.bungee.api.plugin.Event;

/**
 * BungeeCord event that is called when player is successfully loaded after joining. This also includes plugin reloading.
 *
 * @deprecated for removal, use {@link me.neznamy.tab.api.event.player.PlayerLoadEvent}
 */
@Deprecated
public class TabPlayerLoadEvent extends Event {

	private final TabPlayer player;
	
	public TabPlayerLoadEvent(TabPlayer player) {
		this.player = player;
	}
	
	public TabPlayer getPlayer() {
		return player;
	}
}
