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

    /** Instance of the API */
    private static TabAPI instance;

    /**
     * Returns API instance. If instance was not set by the plugin, throws
     * {@code IllegalStateException}. This is usually caused by shading the API
     * into own project, which is not allowed. Another option is calling the method
     * before plugin was able to load.
     *
     * @return  API instance
     * @throws  IllegalStateException
     *          If instance is {@code null}
     */
    public static TabAPI getInstance() {
        if (instance == null) throw new IllegalStateException("API instance is null. This likely means you shaded TAB's API into your project" +
                " instead of only using it, which is not allowed.");
        return instance;
    }

    /**
     * Instance setter for internal use by the plugin only.
     *
     * @param   instance
     *          API instance
     */
    public static void setInstance(TabAPI instance) {
        TabAPI.instance = instance;
    }

    /**
     * Returns player object from given UUID
     *
     * @param   id
     *          Player UUID
     * @return  player object from given UUID
     */
    public abstract TabPlayer getPlayer(UUID id);

    /**
     * Returns player object from given name
     *
     * @param   name
     *          Player name
     * @return  player object from given name
     */
    public abstract TabPlayer getPlayer(String name);

    /**
     * Returns collection of all online players. Will return empty list if plugin is disabled (due to a broken configuration file for example).
     *
     * @return  collection of online players
     */
    public abstract TabPlayer[] getOnlinePlayers();

    /**
     * Return BossBar manager instance if the feature is enabled. If not, returns {@code null}.
     *
     * @return  BossBar manager
     */
    public abstract BossBarManager getBossBarManager();

    /**
     * Returns scoreboard manager instance if the feature is enabled. If not, returns {@code null}.
     *
     * @return  scoreboard manager
     */
    public abstract ScoreboardManager getScoreboardManager();

    /**
     * Returns team manager instance if the feature is enabled. If not, returns {@code null}.
     *
     * @return  team manager
     */
    public abstract TeamManager getTeamManager();

    /**
     * Returns header/footer manager instance if the feature is enabled. If not, returns {@code null}.
     *
     * @return  Header/footer manager
     */
    public abstract HeaderFooterManager getHeaderFooterManager();

    /**
     * Returns PlaceholderManager instance
     *
     * @return  PlaceholderManager instance
     */
    public abstract PlaceholderManager getPlaceholderManager();

    /**
     * Returns feature manager instance
     *
     * @return  feature manager instance
     */
    public abstract FeatureManager getFeatureManager();

    /**
     * Returns Tablist name format manager instance if the feature is enabled. If not, returns {@code null}.
     *
     * @return  Tablist name format manager
     */
    public abstract TablistFormatManager getTablistFormatManager();

    /**
     * Gets the event bus for registering listeners for TAB events.
     *
     * @return  the event bus
     */
    public abstract EventBus getEventBus();

    /**
     * Returns server version. On proxy installation returns PROXY.
     *
     * @return  server version
     */
    public abstract ProtocolVersion getServerVersion();

    /**
     * Prints message into console
     *
     * @param   message
     *          message to print
     * @param   translateColors
     *          {@code true} if colors should be translated, {@code false} if not
     */
    public abstract void sendConsoleMessage(String message, boolean translateColors);

    /**
     * Returns TAB's Thread manager, which allows task submitting
     *
     * @return  ThreadManager instance
     */
    public abstract ThreadManager getThreadManager();

    /**
     * Returns TAB's cache file used to store player toggle data
     *
     * @return  TAB's player cache file
     */
    public abstract ConfigurationFile getPlayerCache();

    /**
     * Returns TAB's config.yml file
     *
     * @return  config.yml file
     */
    public abstract ConfigurationFile getConfig();

    /**
     * Returns TAB's group configuration
     *
     * @return  TAB's group configuration
     */
    public abstract PropertyConfiguration getGroups();

    /**
     * Returns TAB's user configuration
     *
     * @return  TAB's user configuration
     */
    public abstract PropertyConfiguration getUsers();

    /**
     * Sends a debug message into console if the option
     * is enabled in config.
     *
     * @param   message
     *          Message to send
     */
    public abstract void debug(String message);

    /**
     * Logs an error into errors.log file
     *
     * @param   message
     *          Error message
     * @param   t
     *          Thrown error
     */
    public abstract void logError(String message, Throwable t);

    /**
     * Sets name of file with syntax error, which prevented
     * the plugin from enabling. Internal use only.
     *
     * @param   file
     *          Name of file with syntax error
     */
    public abstract void setBrokenFile(String file);
}
