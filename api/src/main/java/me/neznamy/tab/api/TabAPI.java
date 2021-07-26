package me.neznamy.tab.api;

import java.util.Collection;
import java.util.UUID;

import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;
import me.neznamy.tab.api.team.ScoreboardTeamManager;

/**
 * The primary API class to get instances of other API classes
 */
public abstract class TabAPI {

	private static TabAPI instance;
	
	public static TabAPI getInstance() {
		return instance;
	}
	
	public static void setInstance(TabAPI instance) {
		TabAPI.instance = instance;
	}

	/**
	 * Returns player object from given UUID
	 * @return player object from given UUID
	 * @param id - Player UUID
	 */
	public abstract TabPlayer getPlayer(UUID id);

	/**
	 * Returns player object from given name
	 * @return player object from given name
	 * @param name - Player name
	 */
	public abstract TabPlayer getPlayer(String name);
	
	/**
	 * Returns collection of all online players. Will return empty list if plugin is disabled (due to a broken configuration file for example).
	 * @return collection of online players
	 */
	public abstract Collection<TabPlayer> getOnlinePlayers();

	/**
	 * Return bossbar manager instance if the feature is enabled. Returns null otherwise.
	 * @return bossbar manager
	 */
	public abstract BossBarManager getBossBarManager();
	
	/**
	 * Returns scoreboard manager instance if the feature is enabled. Returns null otherwise.
	 * @return scoreboard manager
	 */
	public abstract ScoreboardManager getScoreboardManager();
	
	/**
	 * Returns scoreboard team manager instance if the feature is enabled, false otherwise
	 * @return scoreboard team manager
	 */
	public abstract ScoreboardTeamManager getScoreboardTeamManager();
	
	/**
	 * Returns PlaceholderManager instance
	 * @return PlaceholderManager instance
	 */
	public abstract PlaceholderManager getPlaceholderManager();

	/**
	 * Returns ErrorManager instance
	 * @return ErrorManager instance
	 */
	public abstract ErrorManager getErrorManager();

	/**
	 * Returns platform instance
	 * @return platform instance
	 */
	public abstract Platform getPlatform();
	
	/**
	 * Returns feature manager instance
	 * @return feature manager instance
	 */
	public abstract FeatureManager getFeatureManager();

	public abstract void setBrokenFile(String path);
}