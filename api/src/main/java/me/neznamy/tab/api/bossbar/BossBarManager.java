package me.neznamy.tab.api.bossbar;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import me.neznamy.tab.api.TabPlayer;

/**
 * An interface allowing work with bossbars such as creating, sending 
 * and toggling.
 * <p>
 * Instance can be obtained using {@link me.neznamy.tab.api.TabAPI#getBossBarManager()}.
 * This requires the bossbar feature to be enabled in config, otherwise the method will
 * return null.
 */
public interface BossBarManager {

	/**
	 * Creates bossbar with specified parameters and registers it to bossbar manager,
	 * allowing to retrieve it later.
	 * 
	 * @param	title
	 * 			Title to display, supports placeholders
	 * 
	 * @param	progress
	 * 			Progress to use. Must be between 0 and 100
	 * 
	 * @param	color
	 * 			Color to use
	 * 
	 * @param	style
	 * 			Style to use
	 * @return	The newly created bossbar
	 */
	public BossBar createBossBar(String title, float progress, BarColor color, BarStyle style);

	/**
	 * Creates bossbar with specified parameters and registers it to bossbar manager,
	 * allowing to retrieve it later.
	 * 
	 * @param	title
	 * 			Title to display, supports placeholders
	 * 
	 * @param	progress
	 * 			Progress to use. Must be a string version of a number, or
	 * 			a placeholder that returns a number
	 * 
	 * @param	color
	 * 			Color to use. Must be a string version of one of enum constants, or
	 * 			a placeholder that returns one of them
	 * 
	 * @param	style
	 * 			Style to use. Must be a string version of one of enum constants, or
	 * 			a placeholder that returns one of them
	 * @return	The newly created bossbar
	 */
	public BossBar createBossBar(String title, String progress, String color, String style);

	/**
	 * Returns registered bossbar with given name. For bossbars from config it is the name
	 * they were created with, for API bossbars their randomly generated name, which can be
	 * retrieved using {@link me.neznamy.tab.api.bossbar.BossBar#getName()}.
	 * Returns null if no such bossbar exists.
	 * 
	 * @param	name
	 * 			Name of registered bossbar
	 * @return	bossbar with specified name
	 */
	public BossBar getBossBar(String name);

	/**
	 * Returns registered bossbar with specified UUID. This UUID is randomly generated when
	 * bossbar is created and used in packets / to match which the one from Bukkit API.
	 * Returns null if no such bossbar exists.
	 * 
	 * @param	id
	 * 			UUID to return bossbar by
	 * @return	bossbar with specified uuid
	 */
	public BossBar getBossBar(UUID id);

	/**
	 * Returns a map of all registered bossbars. This includes both bossbars from config 
	 * and those registered via API.
	 * <p>
	 * Map key is bossbar's name available in {@link me.neznamy.tab.api.bossbar.BossBar#getName()},
	 * map value is the bossbar interface.
	 * @return	all registered bossbars
	 */
	public Map<String, BossBar> getRegisteredBossBars();

	/**
	 * Toggles bossbar for this player and sends toggle message if {@code sendToggleMessage} is true.
	 * 
	 * @param	player
	 * 			Player to toggle bossbar for
	 * 
	 * @param	sendToggleMessage
	 * 			{@code true} if toggle message should be sent, {@code false} if not
	 * @see #hasBossBarVisible(TabPlayer)
	 * @see #setBossBarVisible(TabPlayer, boolean, boolean)
	 */
	public void toggleBossBar(TabPlayer player, boolean sendToggleMessage);

	/**
	 * Returns true if player can see bossbars, false if toggled via command or API.
	 * 
	 * @param	player
	 * 			Player to check toggle status of
	 * @return	{@code true} if visible, {@code false} if disabled (toggled)
	 * @see #toggleBossBar(TabPlayer, boolean)
	 * @see #setBossBarVisible(TabPlayer, boolean, boolean)
	 */
	public boolean hasBossBarVisible(TabPlayer player);

	/**
	 * Sets bossbar visibility of player to set value. If value did not change, nothing happens.
	 * If visibility changed, toggle message is sent if {@code sendToggleMessage} is {@code true}.
	 * 
	 * @param	player
	 * 			Player to set bossbar visibility of
	 * @param	visible
	 * 			{@code true} if bossbar should be visible, {@code false} if not
	 * @param	sendToggleMessage
	 * 			{@code true} if toggle message should be sent if value changed, {@code false} if not
	 * @see #toggleBossBar(TabPlayer, boolean)
	 * @see #hasBossBarVisible(TabPlayer)
	 */
	public void setBossBarVisible(TabPlayer player, boolean visible, boolean sendToggleMessage);

	/**
	 * Temporarily displays registered bossbar to player for specified amount of time in milliseconds.
	 * @param	player
	 * 			Player to show bossbar to
	 * @param	bossbar
	 * 			Name of registered bossbar to show
	 * @param	duration
	 * 			In milliseconds for how long should bossbar be displayed
	 * @throws	IllegalArgumentException
	 * 			if no bossbar with specified name exists
	 * @see #announceBossBar(String, int)
	 * @see #getAnnouncedBossBars()
	 */
	public void sendBossBarTemporarily(TabPlayer player, String bossbar, int duration);

	/**
	 * Temporarily displays registered bossbar to all players for specified amount of time in milliseconds.
	 * @param	bossbar
	 * 			Name of registered bossbar to show
	 * @param	duration
	 * 			In milliseconds for how long should bossbar be displayed
	 * @throws	IllegalArgumentException
	 * 			if no bossbar with specified name exists
	 * @see #sendBossBarTemporarily(TabPlayer, String, int)
	 * @see #getAnnouncedBossBars()
	 */
	public void announceBossBar(String bossbar, int duration);

	/**
	 * Returns set of bossbars which are currently being announced.
	 * @return	set of currently active bossbar announcements
	 * @see #sendBossBarTemporarily(TabPlayer, String, int)
	 * @see #announceBossBar(String, int)
	 */
	public Set<BossBar> getAnnouncedBossBars();
}