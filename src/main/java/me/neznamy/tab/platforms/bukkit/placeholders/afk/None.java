package me.neznamy.tab.platforms.bukkit.placeholders.afk;

import me.neznamy.tab.api.AFKProvider;
import me.neznamy.tab.api.TabPlayer;

/**
 * No afk plugin found, returning false
 */
public class None implements AFKProvider {

	@Override
	public boolean isAFK(TabPlayer p) {
		return false;
	}
}