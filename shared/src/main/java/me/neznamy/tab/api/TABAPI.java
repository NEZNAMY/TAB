package me.neznamy.tab.api;

import java.util.UUID;

import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;
import me.neznamy.tab.shared.TAB;

/**
 * The primary API class to get instances of other API classes
 */
public class TabAPI {

	private static TabAPI instance = new TabAPI();
	
	private TabAPI() {
	}
	
	public static TabAPI getInstance() {
		return instance;
	}

	/**
	 * Returns player object from given UUID
	 * @return player object from given UUID
	 * @param id - Player UUID
	 */
	public TabPlayer getPlayer(UUID id) {
		return TAB.getInstance().getPlayer(id);
	}

	/**
	 * Returns player object from given name
	 * @return player object from given name
	 * @param name - Player name
	 */
	public TabPlayer getPlayer(String name) {
		return TAB.getInstance().getPlayer(name);
	}

	/**
	 * Return bossbar manager instance if the feature is enabled. Returns null otherwise.
	 * @return bossbar manager
	 */
	public BossBarManager getBossBarManager() {
		return (BossBarManager) TAB.getInstance().getFeatureManager().getFeature("bossbar");
	}
	
	/**
	 * Returns scoreboard manager instance if the feature is enabled. Returns null otherwise.
	 * @return scoreboard manager
	 */
	public ScoreboardManager getScoreboardManager() {
		return (ScoreboardManager) TAB.getInstance().getFeatureManager().getFeature("scoreboard");
	}
	
	/**
	 * Returns PlaceholderManager instance
	 * @return PlaceholderManager instance
	 */
	public PlaceholderManager getPlaceholderManager() {
		return TAB.getInstance().getPlaceholderManager();
	}
}