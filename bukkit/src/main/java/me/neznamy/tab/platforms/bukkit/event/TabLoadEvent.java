package me.neznamy.tab.platforms.bukkit.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Bukkit event that is called each time TAB fully loads. This includes server startup, reload,
 * /tab reload and /plugman reload tab
 */
public class TabLoadEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers(){
		return getHandlerList();
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
}