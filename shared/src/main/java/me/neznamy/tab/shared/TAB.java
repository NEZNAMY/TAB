package me.neznamy.tab.shared;

import lombok.Getter;
import lombok.Setter;
import me.neznamy.tab.api.*;
import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.api.config.ConfigurationFile;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;
import me.neznamy.tab.api.team.TeamManager;
import me.neznamy.tab.api.util.ReflectionUtils;
import me.neznamy.tab.shared.command.DisabledCommand;
import me.neznamy.tab.shared.command.TabCommand;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.event.EventBusImpl;
import me.neznamy.tab.shared.event.impl.TabLoadEventImpl;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.nametags.NameTag;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main class of the plugin storing data and implementing API
 */
public class TAB extends TabAPI {

    /** Instance of this class */
    @Getter @Setter private static TAB instance;

    /** Player data storage */
    private final Map<UUID, TabPlayer> data = new ConcurrentHashMap<>();

    /** Players by their TabList UUID for faster lookup */
    private final Map<UUID, TabPlayer> playersByTabListId = new ConcurrentHashMap<>();
    
    /** Online player array to avoid memory allocation when iterating */
    @Getter private volatile TabPlayer[] onlinePlayers = new TabPlayer[0];

    /** Instance of plugin's main command */
    @Getter private TabCommand command;

    /** Command executor to use when the plugin is disabled due to an error */
    @Getter private final DisabledCommand disabledCommand = new DisabledCommand();

    /** Implementation of platform the plugin is installed on for platform-specific calls */
    @Getter private final Platform platform;

    /**
     * CPU manager for thread and task management as well as
     * measuring how long code takes to process to then display
     * it in /tab cpu
     */
    private CpuManager cpu;

    /**
     * Platform-independent event executor allowing other plugins
     * to listen to universal platform-independent event objects
     */
    @Getter private EventBusImpl eventBus;

    /**
     * Error manager for printing any and all errors that may
     * occur in any part of the code including hooks into other plugins
     * into files instead of flooding the already flooded console.
     */
    @Getter private final ErrorManager errorManager;

    /** Feature manager forwarding events into all loaded features */
    @Getter private FeatureManagerImpl featureManager;

    /** Plugin's configuration files and values storage */
    @Getter private Configs configuration;

    /**
     * Boolean tracking whether this plugin is enabled or not,
     * which is due to either internal error on load or yaml syntax error
     */
    @Getter private boolean pluginDisabled;

    /** Minecraft version the server is running on, always using the latest on proxies */
    @Getter private final ProtocolVersion serverVersion;

    /** Boolean checking floodgate plugin presence for hook */
    @Getter private final boolean floodgateInstalled = ReflectionUtils.classExists("org.geysermc.floodgate.api.FloodgateApi");

    /** Version string defined by the server */
    @Getter private final String serverVersionString;

    /** TAB's data folder */
    @Getter private final File dataFolder;

    /** Plugin's console logger provided by platform */
    @Getter private final Object logger;

    /** File with YAML syntax error, which prevented plugin from loading */
    @Getter @Setter private String brokenFile;

    /**
     * Constructs new instance with given parameters and sets this
     * new instance as {@link me.neznamy.tab.api.TabAPI} instance.
     *
     * @param   platform
     *          Platform interface
     * @param   serverVersion
     *          Version the server is running on
     */
    public TAB(Platform platform, ProtocolVersion serverVersion, String serverVersionString, File dataFolder, Object logger) {
        this.platform = platform;
        this.serverVersion = serverVersion;
        this.serverVersionString = serverVersionString;
        this.dataFolder = dataFolder;
        this.logger = logger;
        this.errorManager = new ErrorManager(this);
        try {

            eventBus = new EventBusImpl();
        } catch (NoSuchMethodError e) {
            //1.7.10 or lower
        }
    }

    /**
     * Returns player by TabList UUID. This is required due to Velocity
     * as player uuid and TabList uuid do not match there at some circumstances
     *
     * @param   tabListId
     *          TabList id of player
     * @return  player with provided id or null if player was not found
     */
    public TabPlayer getPlayerByTabListUUID(UUID tabListId) {
        return playersByTabListId.get(tabListId);
    }

    /**
     * Loads all classes, configuration files, features, players
     * and then calls events on success. If it fails for any reason,
     * plugin will be marked as disabled and error message will be
     * printed into the console.
     */
    public String load() {
        try {
            long time = System.currentTimeMillis();
            cpu = new CpuManager();
            configuration = new Configs();
            featureManager = new FeatureManagerImpl();
            featureManager.registerFeature(TabConstants.Feature.PLACEHOLDER_MANAGER, new PlaceholderManagerImpl());
            featureManager.registerFeature(TabConstants.Feature.GROUP_MANAGER, new GroupManager(platform.detectPermissionPlugin()));
            platform.registerPlaceholders();
            platform.loadFeatures();
            platform.loadPlayers();
            command = new TabCommand();
            featureManager.load();
            for (TabPlayer p : onlinePlayers) ((ITabPlayer)p).markAsLoaded(false);
            if (eventBus != null) eventBus.fire(TabLoadEventImpl.getInstance());
            pluginDisabled = false;
            sendConsoleMessage("&aEnabled in " + (System.currentTimeMillis()-time) + "ms", true);
            return configuration.getMessages().getReloadSuccess();
        } catch (YAMLException e) {
            sendConsoleMessage("&cDid not enable due to a broken configuration file.", true);
            kill();
            return (configuration == null ? "&4Failed to reload, file %file% has broken syntax. Check console for more info."
                    : configuration.getMessages().getReloadFailBrokenFile()).replace("%file%", brokenFile);
        } catch (Exception e) {
            errorManager.criticalError("Failed to enable. Did you just invent a new way to break the plugin by misconfiguring it?", e);
            kill();
            return "&cFailed to enable due to an internal plugin error. Check console for more info.";
        }
    }

    /**
     * Unloads all features by sending clear packets, resets variables
     * and cancels all tasks.
     */
    public void unload() {
        if (pluginDisabled) return;
        try {
            long time = System.currentTimeMillis();
            if (configuration.getMysql() != null) configuration.getMysql().closeConnection();
            featureManager.unload();
            sendConsoleMessage("&aDisabled in " + (System.currentTimeMillis()-time) + "ms", true);
        } catch (Exception | NoClassDefFoundError e) {
            errorManager.criticalError("Failed to disable", e);
        }
        kill();
    }

    /**
     * Clears online player maps and arrays and cancels all tasks
     */
    private void kill() {
        pluginDisabled = true;
        data.clear();
        playersByTabListId.clear();
        onlinePlayers = new TabPlayer[0];
        cpu.cancelAllTasks();
    }

    /**
     * Adds specified player to online players
     *
     * @param   player
     *          Player to add
     */
    public void addPlayer(TabPlayer player) {
        data.put(player.getUniqueId(), player);
        playersByTabListId.put(player.getTablistId(), player);
        onlinePlayers = data.values().toArray(new TabPlayer[0]);
    }

    /**
     * Removes specified player from online players
     *
     * @param   player
     *          Player to remove
     */
    public void removePlayer(TabPlayer player) {
        data.remove(player.getUniqueId());
        playersByTabListId.remove(player.getTablistId());
        onlinePlayers = data.values().toArray(new TabPlayer[0]);
    }

    /**
     * Returns TAB's group manager used to refresh player groups from other plugins
     *
     * @return  group manager instance
     */
    public GroupManager getGroupManager() {
        return (GroupManager) featureManager.getFeature(TabConstants.Feature.GROUP_MANAGER);
    }

    /**
     * Returns {@link #cpu}
     *
     * @return  {@link #cpu}
     */
    public CpuManager getCPUManager() {
        return cpu;
    }

    @Override
    public BossBarManager getBossBarManager() {
        return (BossBarManager) featureManager.getFeature(TabConstants.Feature.BOSS_BAR);
    }

    @Override
    public ScoreboardManager getScoreboardManager() {
        return (ScoreboardManager) featureManager.getFeature(TabConstants.Feature.SCOREBOARD);
    }

    @Override
    public TeamManager getTeamManager() {
        if (featureManager.isFeatureEnabled(TabConstants.Feature.NAME_TAGS)) return (NameTag) featureManager.getFeature(TabConstants.Feature.NAME_TAGS);
        return (NameTag) featureManager.getFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS);
    }

    @Override
    public PlaceholderManagerImpl getPlaceholderManager() {
        return (PlaceholderManagerImpl) featureManager.getFeature(TabConstants.Feature.PLACEHOLDER_MANAGER);
    }

    @Override
    public TabPlayer getPlayer(String name) {
        for (TabPlayer p : data.values()) {
            if (p.getName().equalsIgnoreCase(name)) return p;
        }
        return null;
    }

    @Override
    public TabPlayer getPlayer(UUID uniqueId) {
        return data.get(uniqueId);
    }

    @Override
    public void sendConsoleMessage(String message, boolean translateColors) {
        platform.sendConsoleMessage(message, translateColors);
    }

    @Override
    public HeaderFooterManager getHeaderFooterManager() {
        return (HeaderFooterManager) featureManager.getFeature(TabConstants.Feature.HEADER_FOOTER);
    }

    @Override
    public ConfigurationFile getPlayerCache() {
        return configuration.getPlayerDataFile();
    }

    @Override
    public ConfigurationFile getConfig() {
        return configuration.getConfig();
    }

    @Override
    public PropertyConfiguration getGroups() {
        return configuration.getGroups();
    }

    @Override
    public PropertyConfiguration getUsers() {
        return configuration.getUsers();
    }

    @Override
    public void logError(String message, Throwable t) {
        errorManager.printError(message, t);
    }

    @Override
    public TablistFormatManager getTablistFormatManager() {
        return (TablistFormatManager) featureManager.getFeature(TabConstants.Feature.PLAYER_LIST);
    }

    @Override
    public void debug(String message) {
        if (configuration != null && configuration.isDebugMode()) sendConsoleMessage("&9[DEBUG] " + message, true);
    }
}
