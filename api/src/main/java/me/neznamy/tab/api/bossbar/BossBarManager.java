package me.neznamy.tab.api.bossbar;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.neznamy.tab.api.TabPlayer;

/**
 * An interface allowing work with BossBars such as creating, sending
 * and toggling.
 * <p>
 * Instance can be obtained using {@link me.neznamy.tab.api.TabAPI#getBossBarManager()}.
 * This requires the BossBar feature to be enabled in config, otherwise the method will
 * return null.
 */
public interface BossBarManager {

	/**
	 * Creates BossBar with specified parameters and registers it to BossBar manager,
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
	 * @return	The newly created BossBar
	 */
	BossBar createBossBar(String title, float progress, BarColor color, BarStyle style);

	/**
	 * Creates BossBar with specified parameters and registers it to BossBar manager,
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
	 * @return	The newly created BossBar
	 */
	BossBar createBossBar(String title, String progress, String color, String style);

	/**
	 * Returns registered BossBar with given name. For BossBars from config it is the name
	 * they were created with, for API BossBars their randomly generated name, which can be
	 * retrieved using {@link me.neznamy.tab.api.bossbar.BossBar#getName()}.
	 * Returns null if no such BossBar exists.
	 * 
	 * @param	name
	 * 			Name of registered BossBar
	 * @return	BossBar with specified name
	 */
	BossBar getBossBar(String name);

	/**
	 * Returns registered BossBar with specified UUID. This UUID is randomly generated when
	 * BossBar is created and used in packets / to match which the one from Bukkit API.
	 * Returns null if no such BossBar exists.
	 * 
	 * @param	id
	 * 			UUID to return BossBar by
	 * @return	BossBar with specified uuid
	 */
	BossBar getBossBar(UUID id);

	/**
	 * Returns a map of all registered BossBars. This includes both BossBars from config
	 * and those registered via API.
	 * <p>
	 * Map key is BossBar's name available in {@link me.neznamy.tab.api.bossbar.BossBar#getName()},
	 * map value is the BossBar interface.
	 * @return	all registered BossBars
	 */
	Map<String, BossBar> getRegisteredBossBars();

	/**
	 * Toggles BossBar for this player and sends toggle message if {@code sendToggleMessage} is true.
	 * 
	 * @param	player
	 * 			Player to toggle BossBar for
	 * 
	 * @param	sendToggleMessage
	 * 			{@code true} if toggle message should be sent, {@code false} if not
	 * @see #hasBossBarVisible(TabPlayer)
	 * @see #setBossBarVisible(TabPlayer, boolean, boolean)
	 */
	void toggleBossBar(TabPlayer player, boolean sendToggleMessage);

	/**
	 * Returns true if player can see BossBars, false if toggled via command or API.
	 * 
	 * @param	player
	 * 			Player to check toggle status of
	 * @return	{@code true} if visible, {@code false} if disabled (toggled)
	 * @see #toggleBossBar(TabPlayer, boolean)
	 * @see #setBossBarVisible(TabPlayer, boolean, boolean)
	 */
	boolean hasBossBarVisible(TabPlayer player);

	/**
	 * Sets BossBar visibility of player to set value. If value did not change, nothing happens.
	 * If visibility changed, toggle message is sent if {@code sendToggleMessage} is {@code true}.
	 * 
	 * @param	player
	 * 			Player to set BossBar visibility of
	 * @param	visible
	 * 			{@code true} if BossBar should be visible, {@code false} if not
	 * @param	sendToggleMessage
	 * 			{@code true} if toggle message should be sent if value changed, {@code false} if not
	 * @see #toggleBossBar(TabPlayer, boolean)
	 * @see #hasBossBarVisible(TabPlayer)
	 */
	void setBossBarVisible(TabPlayer player, boolean visible, boolean sendToggleMessage);

	/**
	 * Temporarily displays registered BossBar to player for specified amount of time in milliseconds.
	 * @param	player
	 * 			Player to show BossBar to
	 * @param	bossBar
	 * 			Name of registered BossBar to show
	 * @param	duration
	 * 			In milliseconds for how long should BossBar be displayed
	 * @throws	IllegalArgumentException
	 * 			if no BossBar with specified name exists
	 * @see #announceBossBar(String, int)
	 * @see #getAnnouncedBossBars()
	 */
	void sendBossBarTemporarily(TabPlayer player, String bossBar, int duration);

	/**
	 * Temporarily displays registered BossBar to all players for specified amount of time in milliseconds.
	 * @param	bossBar
	 * 			Name of registered BossBar to show
	 * @param	duration
	 * 			In milliseconds for how long should BossBar be displayed
	 * @throws	IllegalArgumentException
	 * 			if no BossBar with specified name exists
	 * @see #sendBossBarTemporarily(TabPlayer, String, int)
	 * @see #getAnnouncedBossBars()
	 */
	void announceBossBar(String bossBar, int duration);

	/**
	 * Returns list of BossBars which are currently being announced.
	 * @return	list of currently active BossBar announcements
	 * @see #sendBossBarTemporarily(TabPlayer, String, int)
	 * @see #announceBossBar(String, int)
	 */
	List<BossBar> getAnnouncedBossBars();
}