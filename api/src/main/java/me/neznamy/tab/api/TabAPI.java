package me.neznamy.tab.api;

import java.util.UUID;

import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.api.event.EventBus;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;
import me.neznamy.tab.api.task.ThreadManager;
import me.neznamy.tab.api.team.TeamManager;

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
	public abstract TabPlayer[] getOnlinePlayers();

	/**
	 * Return BossBar manager instance if the feature is enabled. Returns null otherwise.
	 * @return BossBar manager
	 */
	public abstract BossBarManager getBossBarManager();
	
	/**
	 * Returns scoreboard manager instance if the feature is enabled. Returns null otherwise.
	 * @return scoreboard manager
	 */
	public abstract ScoreboardManager getScoreboardManager();
	
	/**
	 * Returns team manager instance if the feature is enabled, false otherwise
	 * @return team manager
	 */
	public abstract TeamManager getTeamManager();
	
	public abstract HeaderFooterManager getHeaderFooterManager();
	
	/**
	 * Returns PlaceholderManager instance
	 * @return PlaceholderManager instance
	 */
	public abstract PlaceholderManager getPlaceholderManager();

	/**
	 * Returns feature manager instance
	 * @return feature manager instance
	 */
	public abstract FeatureManager getFeatureManager();
	
	public abstract TablistFormatManager getTablistFormatManager();

	/**
	 * Gets the event bus for registering listeners for TAB events.
	 *
	 * @return the event bus
	 */
	public abstract EventBus getEventBus();

	/**
	 * Returns server version. On proxy installation returns PROXY.
	 * @return server version
	 */
	public abstract ProtocolVersion getServerVersion();
	
	/**
	 * Prints message into console
	 * @param message - message to print
	 * @param translateColors - true if colors should be translated, false if not
	 */
	public abstract void sendConsoleMessage(String message, boolean translateColors);
	
	public abstract ThreadManager getThreadManager();
	
	public abstract ConfigurationFile getPlayerCache();
	
	public abstract ConfigurationFile getConfig();
	
	public abstract PropertyConfiguration getGroups();
	
	public abstract PropertyConfiguration getUsers();
	
	public abstract void debug(String message);
	
	public abstract void logError(String message, Throwable t);
}
