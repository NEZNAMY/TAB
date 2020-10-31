package me.neznamy.tab.api;

import java.util.Set;

/**
 * An interface to be worked with to manipulate scoreboard
 */
public interface Scoreboard {

	public void unregister(TabPlayer p);

	public void register(TabPlayer p);

	public String getName();

	public Set<TabPlayer> getRegisteredUsers();
}