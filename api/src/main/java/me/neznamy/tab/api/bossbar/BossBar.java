package me.neznamy.tab.api.bossbar;

import java.util.Set;
import java.util.UUID;

import me.neznamy.tab.api.TabPlayer;

/**
 * An interface representing a bossbar line. 
 * <p>
 * For clients 1.8 and lower it uses wither, which only allows 1 bar 
 * to be displayed at a time. Entity packets are not available on bungeecord,
 * where nothing will be displayed for these players.
 * <p>
 * For 1.9+ it uses the new bossbar feature, allowing configurable styles
 * and colors, as well as display several bars at once. Limit of maximum 
 * displayed bossbars depends on client resolution and GUI scale, up to
 * 9 concurrent bossbars displayed at once.
 */
public interface BossBar {

	/**
	 * Returns name of this bossbar. If it was defined in config, returns
	 * the name specified in config. If it was made using the API, returns
	 * randomly generated ID given to this bossbar when creating.
	 * @return	name of bossbar
	 */
	public String getName();

	/**
	 * Returns randomly generated uuid of this bossbar used internally to match
	 * the bossbar in client with packets or with Bukkit API bossbar instances.
	 * @return	uuid of this bossbar
	 */
	public UUID getUniqueId();

	/**
	 * Changes bossbar title to specified string, supporting placeholders.
	 * <p>
	 * If title contains new placeholders not used before, they are registered using
	 * standard registration logic and refreshed periodically. No need to call
	 * this method to try to keep placeholder values up to date.
	 * <p>
	 * If specified title is equal to the current one, nothing happens.
	 * 
	 * @param	title
	 * 			New title to display in this bossbar
	 */
	public void setTitle(String title);

	/**
	 * Changes bossbar progress to specified string, supporting placeholders.
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
	 * 			New progress to use in this bossbar
	 */
	public void setProgress(String progress);

	/**
	 * Changes bossbar progress to specified value ranging from 0 to 100.
	 * <p>
	 * If specified progress is equal to the current one, nothing happens.
	 * @param	progress
	 * 			New progress to use in this bossbar
	 */
	public void setProgress(float progress);

	/**
	 * Changes bossbar color to specified string, supporting placeholders.
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
	 * 			New color to use in this bossbar
	 */
	public void setColor(String color);

	/**
	 * Changes bossbar color to specified enum constant.
	 * <p>
	 * If specified color is equal to the current one, nothing happens.
	 * 
	 * @param	color
	 * 			New color to use in this bossbar
	 */
	public void setColor(BarColor color);

	/**
	 * Changes bossbar style to specified string, supporting placeholders.
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
	 * 			New style to use in this bossbar
	 */
	public void setStyle(String style);

	/**
	 * Changes bossbar style to specified enum constant.
	 * <p>
	 * If specified style is equal to the current one, nothing happens.
	 * 
	 * @param	style
	 * 			New style to use in this bossbar
	 */
	public void setStyle(BarStyle style);

	/**
	 * Returns current title of the bossbar in raw format. If it contains placeholders,
	 * their raw identifiers are used in the result.
	 * @return	title of the bossbar
	 */
	public String getTitle();

	/**
	 * Returns progress of the bossbar as a string, which is either entered string 
	 * containing placeholders or entered number converted to string
	 * @return	entered progress as a string
	 */
	public String getProgress();

	/**
	 * Returns color of the bossbar as a string, which is either entered string 
	 * containing placeholders or entered enum value converted to string
	 * @return	entered color as a string
	 */
	public String getColor();

	/**
	 * Returns style of the bossbar as a string, which is either entered string 
	 * containing placeholders or entered enum value converted to string
	 * @return	entered style as a string
	 */
	public String getStyle();

	/**
	 * Registers this bossbar to specified player.
	 * <p>
	 * If the player already sees this bossbar, nothing happens.
	 * 
	 * @param	player
	 * 			Player to register this bossbar to
	 */
	public void addPlayer(TabPlayer player);

	/**
	 * Unregisters this bossbar from specified player.
	 * <p>
	 * If the player does not see this bossbar, nothing happens.
	 * 
	 * @param	player
	 * 			Player to unregister this bossbar from
	 */
	public void removePlayer(TabPlayer player);

	/**
	 * Returns set of players who can see this bossbar.
	 * <p>
	 * The returned set is immutable can only be used to read, writing
	 * does not do anything. For adding/removing playes see {@link #addPlayer(TabPlayer)}
	 * and {@link #removePlayer(TabPlayer)}.
	 * @return	Immutable set of players seeing this bossbar
	 */
	public Set<TabPlayer> getPlayers();
}