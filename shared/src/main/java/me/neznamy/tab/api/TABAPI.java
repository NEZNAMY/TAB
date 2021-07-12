package me.neznamy.tab.api;

import java.util.UUID;

import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;
import me.neznamy.tab.api.team.ScoreboardTeamManager;
import me.neznamy.tab.shared.TAB;

/**
 * The primary API class to get instances of other API classes
 */
public interface TabAPI {

	public static TabAPI getInstance() {
		return TAB.getInstance();
	}

	/**
	 * Returns player object from given UUID
	 * @return player object from given UUID
	 * @param id - Player UUID
	 */
	public TabPlayer getPlayer(UUID id);

	/**
	 * Returns player object from given name
	 * @return player object from given name
	 * @param name - Player name
	 */
	public TabPlayer getPlayer(String name);

	/**
	 * Return bossbar manager instance if the feature is enabled. Returns null otherwise.
	 * @return bossbar manager
	 */
	public BossBarManager getBossBarManager();
	
	/**
	 * Returns scoreboard manager instance if the feature is enabled. Returns null otherwise.
	 * @return scoreboard manager
	 */
	public ScoreboardManager getScoreboardManager();
	
	/**
	 * Returns scoreboard team manager instance if the feature is enabled, false otherwise
	 * @return scoreboard team manager
	 */
	public ScoreboardTeamManager getScoreboardTeamManager();
	
	/**
	 * Returns PlaceholderManager instance
	 * @return PlaceholderManager instance
	 */
	public PlaceholderManager getPlaceholderManager();
}