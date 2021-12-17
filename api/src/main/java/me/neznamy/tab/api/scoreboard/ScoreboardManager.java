package me.neznamy.tab.api.scoreboard;

import java.util.List;
import java.util.Map;

import me.neznamy.tab.api.TabPlayer;

/**
 * Interface allowing work with scoreboards, such as creating,
 * sending, toggling and modifying.
 * <p>
 * Instance can be obtained using {@link me.neznamy.tab.api.TabAPI#getScoreboardManager()}.
 * This requires the scoreboard feature to be enabled in config,
 * the method will return null otherwise.
 */
public interface ScoreboardManager {

	/**
	 * Creates new scoreboard, registers and returns it. {@code name}
	 * is internal name of the scoreboard, which it will be registered
	 * with and further work where scoreboard name is used allowed.
	 * <p>
	 * Scoreboard {@code title} is limited to 32 characters on <1.13. If needed,
	 * it will be cut to 32 character for those players (after replacing 
	 * placeholders). RGB is supported using any of the supported formats.
	 * Placeholders are supported.
	 * <p>
	 * {@code lines} will display in the same order as contained in the list.
	 * Client can only see up to 15 lines at a time, however you can define
	 * more, if some of them only consist of placeholder that might be empty.
	 * 
	 * @param	name
	 * 			Internal name of the scoreboard
	 * @param	title
	 * 			The scoreboard title
	 * @param	lines
	 * 			Lines of text in scoreboard (supports placeholders)
	 * @return	The new scoreboard with given parameters
	 * @throws	NullPointerException
	 * 			if {@code name} or {@code lines} is {@code null}
	 */
	Scoreboard createScoreboard(String name, String title, List<String> lines);
	
	/**
	 * Returns map of registered scoreboards via config and API.
	 * Map key is name of scoreboard, which is defined either in config
	 * or using {@link #createScoreboard(String, String, List)}.
	 * Map value is reference to the actual scoreboard.
	 * 
	 * @return	map of registered scoreboards
	 */
	Map<String, Scoreboard> getRegisteredScoreboards();
	
	/**
	 * Displays scoreboard to defined player. This will disable all display
	 * condition checks until the scoreboard is unregistered using 
	 * {@link #resetScoreboard(TabPlayer)}. If the player already sees
	 * this scoreboard, nothing will happen.
	 * 
	 * @param	player
	 * 			Player to send scoreboard to
	 * @param	scoreboard
	 * 			Scoreboard to display
	 * @throws	NullPointerException
	 * 			if {@code player} or {@code scoreboard} is {@code null}
	 * @see		#hasCustomScoreboard(TabPlayer)
	 * @see		#resetScoreboard(TabPlayer)
	 */
	void showScoreboard(TabPlayer player, Scoreboard scoreboard);
	
	/**
	 * Returns {@code true} if player has custom scoreboard set using
	 * {@link #showScoreboard(TabPlayer, Scoreboard)}, {@code false} if not.
	 * 
	 * @param	player
	 * 			Player to check
	 * @return	{@code true} if player has custom scoreboard set using API, 
	 * 			{@code false} if not
	 * @see		#showScoreboard(TabPlayer, Scoreboard)
	 * @see		#resetScoreboard(TabPlayer)
	 */
	boolean hasCustomScoreboard(TabPlayer player);
	
	/**
	 * Hides custom scoreboard sent using {@link #showScoreboard(TabPlayer, Scoreboard)}
	 * and re-enables internal display logic with conditions. If player does not
	 * have any forced scoreboard, nothing happens.
	 * 
	 * @param	player
	 * 			Player to hide custom scoreboard from
	 * @see		#showScoreboard(TabPlayer, Scoreboard)
	 * @see		#hasCustomScoreboard(TabPlayer)
	 */
	void resetScoreboard(TabPlayer player);
	
	/**
	 * Returns {@code true} if player has scoreboard enabled, {@code false} 
	 * if disabled (toggled) using either toggle command, 
	 * {@link #toggleScoreboard(TabPlayer, boolean)} or
	 * {@link #setScoreboardVisible(TabPlayer, boolean, boolean)}.
	 * 
	 * @param	player
	 * 			Player to get visibility status of
	 * @return	{@code true} if visible, {@code false} if disabled
	 * @see		#setScoreboardVisible(TabPlayer, boolean, boolean)
	 * @see		#toggleScoreboard(TabPlayer, boolean)
	 */
	boolean hasScoreboardVisible(TabPlayer player);
	
	/**
	 * Sets scoreboard visibility of player to defined value. If visibility status
	 * is same as player had before, nothing happens. If this call changes
	 * visibility, scoreboard is toggled. If {@code sendToggleMessage} is {@code true},
	 * toggle message defined in configuration is sent.
	 * 
	 * @param	player
	 * 			Player to set visibility for
	 * @param	visible
	 * 			New visibility status
	 * @param	sendToggleMessage
	 * 			whether to send toggle message if status changed or not
	 * @see		#toggleScoreboard(TabPlayer, boolean)
	 * @see		#hasScoreboardVisible(TabPlayer)
	 */
	void setScoreboardVisible(TabPlayer player, boolean visible, boolean sendToggleMessage);
	
	/**
	 * Toggles scoreboard for specified player. If player had scoreboard visible,
	 * hides it. If hidden, shows it. If {@code sendToggleMessage} is {@code true},
	 * toggle message defined in configuration is sent.
	 * 
	 * @param	player
	 * 			Player to toggle scoreboard for
	 * @param	sendToggleMessage
	 * 			Whether to send toggle message or not
	 * @see		#hasScoreboardVisible(TabPlayer)
	 * @see		#setScoreboardVisible(TabPlayer, boolean, boolean)
	 */
	void toggleScoreboard(TabPlayer player, boolean sendToggleMessage);
	
	/**
	 * Temporarily displays scoreboard to all players for specified amount of 
	 * time (in milliseconds). Scoreboard name is either defined in config, or 
	 * through API in {@link #createScoreboard(String, String, List)}.
	 * 
	 * @param	scoreboard
	 * 			Scoreboard from config or registered via API
	 * @param	duration
	 * 			Duration in milliseconds
	 * @throws	IllegalArgumentException
	 * 			if no scoreboard was found with such name or {@code duration}
	 * 			is < 0.
	 */
	void announceScoreboard(String scoreboard, int duration);

	/**
	 * Returns player's currently displayed scoreboard. This can be either with
	 * configuration, overridden with commands or the API. Will return {@code null}
	 * if player does not see any scoreboard due to not meeting any display condition.
	 * @param	player
	 * 			player to get active scoreboard of
	 * @return	player's active scoreboard or {@code null} if player has no scoreboard
	 */
	Scoreboard getActiveScoreboard(TabPlayer player);
}