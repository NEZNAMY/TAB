package me.neznamy.tab.platforms.bukkit.placeholders.afk;

import me.neznamy.tab.shared.ITabPlayer;

/**
 * An interface for hooking into permission plugins for %afk% placeholder
 */
public interface AFKProvider {

	public boolean isAFK(ITabPlayer p) throws Exception;
}
