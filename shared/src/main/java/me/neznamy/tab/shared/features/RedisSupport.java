package me.neznamy.tab.shared.features;

import me.neznamy.tab.api.TabPlayer;

/**
 * Feature synchronizing player display data between
 * multiple proxies connected with RedisBungee plugin.
 */
public interface RedisSupport {

	/**
	 * Sends a message to all other proxies to update
	 * player list formatting of requested player.
	 *
	 * @param	p
	 * 			Player to update
	 * @param	format
	 * 			TabList name format to use
	 */
	void updateTabFormat(TabPlayer p, String format);

	/**
	 * Sends a message to all other proxies to update
	 * NameTag prefix / suffix values of requested player.
	 *
	 * @param	p
	 * 			Player to update
	 * @param	tagPrefix
	 * 			New NameTag prefix
	 * @param	tagSuffix
	 * 			New NameTag suffix
	 */
	void updateNameTag(TabPlayer p, String tagPrefix, String tagSuffix);

	/**
	 * Sends a message to all other proxies to update
	 * BelowName number of requested player.
	 *
	 * @param	p
	 * 			Player to update
	 * @param	value
	 * 			New BelowName value
	 */
	void updateBelowName(TabPlayer p, String value);

	/**
	 * Sends a message to all other proxies to update
	 * yellow number of requested player.
	 *
	 * @param	p
	 * 			Player to update
	 * @param	value
	 * 			New number value
	 */
	void updateYellowNumber(TabPlayer p, String value);

	/**
	 * Sends a message to all other proxies to change
	 * team name of requested player.
	 *
	 * @param	p
	 * 			Player to update
	 * @param	to
	 * 			New team name
	 */
	void updateTeamName(TabPlayer p, String to);
}