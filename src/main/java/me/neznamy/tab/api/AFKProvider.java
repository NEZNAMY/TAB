package me.neznamy.tab.api;

/**
 * An interface for hooking into permission plugins for %afk% placeholder
 */
public interface AFKProvider {

	public boolean isAFK(TabPlayer p) throws Exception;
}
