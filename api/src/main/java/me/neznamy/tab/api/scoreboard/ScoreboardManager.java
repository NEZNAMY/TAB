package me.neznamy.tab.api.scoreboard;

import java.util.List;
import java.util.Map;

import me.neznamy.tab.api.TabPlayer;

public interface ScoreboardManager {

	/**
	 * Creates a new scoreboard, registers and returns it
	 * @param name - name of the scoreboard
	 * @param title - the scoreboard title
	 * @param lines - up to 15 lines of text (supports placeholders)
	 * @return The new scoreboard
	 */
	public Scoreboard createScoreboard(String name, String title, List<String> lines);
	
	/**
	 * Returns map of registered scoreboards via config and API
	 * @return map of registered scoreboards
	 */
	public Map<String, Scoreboard> getRegisteredScoreboards();
	
	/**
	 *  Displays scoreboard to defined player and disables all checks until removed again
	 * @param player - player to send scoreboard to
	 * @param scoreboard - scoreboard to display
	 */
	public void showScoreboard(TabPlayer player, Scoreboard scoreboard);
	
	/**
	 * Returns true if player has custom scoreboard set via API, false if not
	 * @param player - player ot check
	 * @return true if player has custom scoreboard set via API, false if not
	 */
	public boolean hasCustomScoreboard(TabPlayer player);
	
	/**
	 * Hides custom scoreboard sent by API and re-enables internal display logic with conditions
	 * @param player - player to hide custom scoreboard from
	 */
	public void resetScoreboard(TabPlayer player);
	
	/**
	 * Returns true if player has scoreboard enabled, false if disabled (toggled)
	 * @param player - player to get visibility status of
	 * @return true if visible, false if disabled
	 */
	public boolean hasScoreboardVisible(TabPlayer player);
	
	/**
	 * Sets scoreboard visibility to player and sends toggle message if status changed and toggle is true
	 * @param player - player to set visibility for
	 * @param visible - new visibility status
	 * @param sendToggleMessage - whether to send toggle message or not
	 */
	public void setScoreboardVisible(TabPlayer player, boolean visible, boolean sendToggleMessage);
	
	/**
	 * Toggles scoreboard for specified player
	 * @param player - player to toggle scoreboard for
	 * @param sendToggleMessage - whether to send toggle message or not
	 */
	public void toggleScoreboard(TabPlayer player, boolean sendToggleMessage);
	
	/**
	 * Temporarily displays scoreboard to all players for specified amount of time
	 * @param scoreboard - scoreboard from config or registered via API
	 * @param duration - duration in milliseconds
	 */
	public void announceScoreboard(String scoreboard, int duration);
}