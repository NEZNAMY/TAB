package me.neznamy.tab.api;

import java.util.Set;

/**
 * An interface to be worked with to manipulate scoreboard
 */
public interface Scoreboard {

	/**
	 * Unregisters this scoreboard from specified player
	 * @param p - player to unregister scoreboard for
	 */
	public void unregister(TabPlayer p);

	/**
	 * Registers this scoreboard to specified player
	 * @param p - player to register scoreboard for
	 */
	public void register(TabPlayer p);

	/**
	 * Returns name of this scoreboard
	 * @return name of this scoreboard
	 */
	public String getName();

	/**
	 * Returns list of all players who can see this scoreboard
	 * @return list of players who can see this scoreboard
	 */
	public Set<TabPlayer> getRegisteredUsers();
}