package me.neznamy.tab.platforms.bukkit.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Bukkit event that is called when player is successfully loaded after joining. This also includes plugin reloading.
 *
 * @deprecated for removal, use {@link me.neznamy.tab.api.event.player.PlayerLoadEvent}
 */
@Deprecated
public class TabPlayerLoadEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	private final TabPlayer player;
	
	public TabPlayerLoadEvent(TabPlayer player) {
		this.player = player;
	}
	
	@Override
	public @NotNull HandlerList getHandlers(){
		return getHandlerList();
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	public TabPlayer getPlayer() {
		return player;
	}
}
