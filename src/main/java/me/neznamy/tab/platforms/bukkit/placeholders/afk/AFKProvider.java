package me.neznamy.tab.platforms.bukkit.placeholders.afk;

import me.neznamy.tab.api.TabPlayer;

/**
 * An interface for hooking into permission plugins for %afk% placeholder
 */
public interface AFKProvider {

	public boolean isAFK(TabPlayer p) throws Exception;
}
