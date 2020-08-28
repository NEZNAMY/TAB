package me.neznamy.tab.platforms.bukkit.placeholders.afk;

import me.neznamy.tab.shared.ITabPlayer;

/**
 * No afk plugin found, returning false
 */
public class None implements AFKProvider {

	@Override
	public boolean isAFK(ITabPlayer p) {
		return false;
	}
}