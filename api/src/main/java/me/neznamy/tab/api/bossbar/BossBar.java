package me.neznamy.tab.api.bossbar;

import java.util.List;
import java.util.UUID;

import me.neznamy.tab.api.TabPlayer;

/**
 * An interface representing a BossBar line.
 * <p>
 * For clients 1.8 and lower it uses wither, which only allows 1 bar 
 * to be displayed at a time. Entity packets are not available on BungeeCord,
 * where nothing will be displayed for these players.
 * <p>
 * For 1.9+ it uses the new BossBar feature, allowing configurable styles
 * and colors, as well as display several bars at once. Limit of maximum 
 * displayed BossBars depends on client resolution and GUI scale, up to
 * 9 concurrent BossBars displayed at once.
 */
public interface BossBar {

	/**
	 * Returns name of this BossBar. If it was defined in config, returns
	 * the name specified in config. If it was made using the API, returns
	 * randomly generated ID given to this BossBar when creating.
	 * @return	name of BossBar
	 */
	String getName();

	/**
	 * Returns randomly generated uuid of this BossBar used internally to match
	 * the BossBar in client with packets or with Bukkit API BossBar instances.
	 * @return	uuid of this BossBar
	 */
	UUID getUniqueId();

	/**
	 * Changes BossBar title to specified string, supporting placeholders.
	 * <p>
	 * If title contains new placeholders not used before, they are registered using
	 * standard registration logic and refreshed periodically. No need to call
	 * this method to try to keep placeholder values up to date.
	 * <p>
	 * If specified title is equal to the current one, nothing happens.
	 * 
	 * @param	title
	 * 			New title to display in this BossBar
	 */
	void setTitle(String title);

	/**
	 * Changes BossBar progress to specified string, supporting placeholders.
	 * The string must be a string version of a float value, or a placeholder that 
	 * outputs a float value with range 0-100.
	 * <p>
	 * If progress contains new placeholders not used before, they are registered using
	 * standard registration logic and refreshed periodically. No need to call
	 * this method to try to keep placeholder values up to date.
	 * <p>
	 * If specified progress is equal to the current one, nothing happens.
	 * 
	 * @param	progress
	 * 			New progress to use in this BossBar
	 */
	void setProgress(String progress);

	/**
	 * Changes BossBar progress to specified value ranging from 0 to 100.
	 * <p>
	 * If specified progress is equal to the current one, nothing happens.
	 * @param	progress
	 * 			New progress to use in this BossBar
	 */
	void setProgress(float progress);

	/**
	 * Changes BossBar color to specified string, supporting placeholders.
	 * The string must be a string version of one of the supported values, 
	 * or a placeholder that outputs one of them.
	 * <p>
	 * If color contains new placeholders not used before, they are registered using
	 * standard registration logic and refreshed periodically. No need to call
	 * this method to try to keep placeholder values up to date.
	 * <p>
	 * If specified color is equal to the current one, nothing happens.
	 * 
	 * @param	color
	 * 			New color to use in this BossBar
	 */
	void setColor(String color);

	/**
	 * Changes BossBar color to specified enum constant.
	 * <p>
	 * If specified color is equal to the current one, nothing happens.
	 * 
	 * @param	color
	 * 			New color to use in this BossBar
	 */
	void setColor(BarColor color);

	/**
	 * Changes BossBar style to specified string, supporting placeholders.
	 * The string must be a string version of one of the supported values, 
	 * or a placeholder that outputs one of them.
	 * <p>
	 * If style contains new placeholders not used before, they are registered using
	 * standard registration logic and refreshed periodically. No need to call
	 * this method to try to keep placeholder values up to date.
	 * <p>
	 * If specified style is equal to the current one, nothing happens.
	 * 
	 * @param	style
	 * 			New style to use in this BossBar
	 */
	void setStyle(String style);

	/**
	 * Changes BossBar style to specified enum constant.
	 * <p>
	 * If specified style is equal to the current one, nothing happens.
	 * 
	 * @param	style
	 * 			New style to use in this BossBar
	 */
	void setStyle(BarStyle style);

	/**
	 * Returns current title of the BossBar in raw format. If it contains placeholders,
	 * their raw identifiers are used in the result.
	 * @return	title of the BossBar
	 */
	String getTitle();

	/**
	 * Returns progress of the BossBar as a string, which is either entered string
	 * containing placeholders or entered number converted to string
	 * @return	entered progress as a string
	 */
	String getProgress();

	/**
	 * Returns color of the BossBar as a string, which is either entered string
	 * containing placeholders or entered enum value converted to string
	 * @return	entered color as a string
	 */
	String getColor();

	/**
	 * Returns style of the BossBar as a string, which is either entered string
	 * containing placeholders or entered enum value converted to string
	 * @return	entered style as a string
	 */
	String getStyle();

	/**
	 * Registers this BossBar to specified player.
	 * <p>
	 * If the player already sees this BossBar, nothing happens.
	 * 
	 * @param	player
	 * 			Player to register this BossBar to
	 */
	void addPlayer(TabPlayer player);

	/**
	 * Unregisters this BossBar from specified player.
	 * <p>
	 * If the player does not see this BossBar, nothing happens.
	 * 
	 * @param	player
	 * 			Player to unregister this BossBar from
	 */
	void removePlayer(TabPlayer player);

	/**
	 * Returns list of players who can see this BossBar.
	 * <p>
	 * The returned list can only be used to read. Writing
	 * will not work properly. For adding/removing players see {@link #addPlayer(TabPlayer)}
	 * and {@link #removePlayer(TabPlayer)}.
	 * @return	List of players seeing this BossBar
	 */
	List<TabPlayer> getPlayers();
}