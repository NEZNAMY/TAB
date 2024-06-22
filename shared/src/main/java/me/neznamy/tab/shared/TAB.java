package me.neznamy.tab.shared;

import lombok.Getter;
import lombok.Setter;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.bossbar.BossBarManager;
import me.neznamy.tab.api.tablist.SortingManager;
import me.neznamy.tab.api.tablist.layout.LayoutManager;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;
import me.neznamy.tab.api.tablist.HeaderFooterManager;
import me.neznamy.tab.api.tablist.TabListFormatManager;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.SimpleComponent;
import me.neznamy.tab.shared.config.helper.ConfigHelper;
import me.neznamy.tab.shared.cpu.CpuManager;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.platform.Platform;
import me.neznamy.tab.shared.command.DisabledCommand;
import me.neznamy.tab.shared.command.TabCommand;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.event.EventBusImpl;
import me.neznamy.tab.shared.event.impl.TabLoadEventImpl;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.proxy.ProxyPlatform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main class of the plugin storing data and implementing API
 */
@Getter
public class TAB extends TabAPI {

    /** Instance of this class */
    @Getter
    private static TAB instance;

    /** Player data storage */
    private final Map<UUID, TabPlayer> data = new ConcurrentHashMap<>();

    /** Players by their TabList UUID for faster lookup */
    private final Map<UUID, TabPlayer> playersByTabListId = new ConcurrentHashMap<>();

    /** Online player array to avoid memory allocation when iterating */
    private volatile TabPlayer[] onlinePlayers = new TabPlayer[0];

    /** Instance of plugin's main command */
    private TabCommand command;

    /** Command executor to use when the plugin is disabled due to an error */
    private final DisabledCommand disabledCommand = new DisabledCommand();

    /** Implementation of platform the plugin is installed on for platform-specific calls */
    private final Platform platform;

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
    private EventBusImpl eventBus;

    /**
     * Error manager for printing any and all errors that may
     * occur in any part of the code including hooks into other plugins
     * into files instead of flooding the already flooded console.
     */
    private final ErrorManager errorManager;

    /** Feature manager forwarding events into all loaded features */
    private FeatureManager featureManager;

    /** Placeholder manager for fast access */
    private PlaceholderManagerImpl placeholderManager;

    /** Group manager for getting groups of players */
    private GroupManager groupManager;

    /** Plugin's configuration files and values storage */
    private Configs configuration;

    /**
     * Boolean tracking whether this plugin is enabled or not,
     * which is due to either internal error on load or yaml syntax error
     */
    private boolean pluginDisabled;

    /** TAB's data folder */
    private final File dataFolder;

    /** File with YAML syntax error, which prevented plugin from loading */
    @Setter private String brokenFile;

    /** Helper for detecting misconfiguration in configs and send it to user */
    private final ConfigHelper configHelper = new ConfigHelper();

    /**
     * Creates new instance using given platform and loads it
     *
     * @param   platform
     *          Platform interface
     */
    public static void create(@NotNull Platform platform) {
        instance = new TAB(platform);
        instance.load();
    }

    /**
     * Constructs new instance with given parameters and sets this
     * new instance as {@link me.neznamy.tab.api.TabAPI} instance.
     *
     * @param   platform
     *          Platform interface
     */
    private TAB(@NotNull Platform platform) {
        this.platform = platform;
        dataFolder = platform.getDataFolder();
        errorManager = new ErrorManager(dataFolder);
        try {
            eventBus = new EventBusImpl();
        } catch (NoSuchMethodError e) {
            //1.7.10 or lower
        }
        TabAPI.setInstance(this);
        platform.registerListener();
        platform.registerCommand();
        platform.startMetrics();
        if (platform instanceof ProxyPlatform) {
            ((ProxyPlatform) platform).registerChannel();
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
    public @Nullable TabPlayer getPlayerByTabListUUID(UUID tabListId) {
        return playersByTabListId.get(tabListId);
    }

    /**
     * Loads all classes, configuration files, features, players
     * and then calls events on success. If it fails for any reason,
     * plugin will be marked as disabled and error message will be
     * printed into the console.
     * Returns load status message, which is either success or failure.
     *
     * @return  Load status message.
     */
    public String load() {
        try {
            long time = System.currentTimeMillis();
            cpu = new CpuManager();
            configuration = new Configs();
            featureManager = new FeatureManager();
            placeholderManager = new PlaceholderManagerImpl(cpu);
            featureManager.registerFeature(TabConstants.Feature.PLACEHOLDER_MANAGER, placeholderManager);
            groupManager = platform.detectPermissionPlugin();
            platform.registerPlaceholders();
            featureManager.loadFeaturesFromConfig();
            platform.loadPlayers();
            command = new TabCommand();
            featureManager.load();
            for (TabPlayer p : onlinePlayers) p.markAsLoaded(false);
            if (eventBus != null) eventBus.fire(TabLoadEventImpl.getInstance());
            pluginDisabled = false;
            cpu.enable();
            configHelper.startup().checkErrorLog();
            configHelper.startup().printWarnCount();
            platform.logInfo(new SimpleComponent(EnumChatFormat.GREEN + "Enabled in " + (System.currentTimeMillis()-time) + "ms"));
            return configuration.getMessages().getReloadSuccess();
        } catch (YAMLException e) {
            platform.logWarn(new SimpleComponent(EnumChatFormat.RED + "Did not enable due to a broken configuration file."));
            kill();
            return (configuration == null ? "&4Failed to reload, file %file% has broken syntax. Check console for more info."
                    : configuration.getMessages().getReloadFailBrokenFile()).replace("%file%", brokenFile);
        } catch (Throwable e) {
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
            platform.logInfo(new SimpleComponent(EnumChatFormat.GREEN + "Disabled in " + (System.currentTimeMillis()-time) + "ms"));
        } catch (Throwable e) {
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
    public void addPlayer(@NotNull TabPlayer player) {
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
    public void removePlayer(@NotNull TabPlayer player) {
        data.remove(player.getUniqueId());
        playersByTabListId.remove(player.getTablistId());
        onlinePlayers = data.values().toArray(new TabPlayer[0]);
    }

    /**
     * Returns {@link #cpu}
     *
     * @return  {@link #cpu}
     */
    public @NotNull CpuManager getCPUManager() {
        return cpu;
    }

    @Override
    public @Nullable BossBarManager getBossBarManager() {
        return featureManager.getFeature(TabConstants.Feature.BOSS_BAR);
    }

    @Override
    public @Nullable ScoreboardManager getScoreboardManager() {
        return featureManager.getFeature(TabConstants.Feature.SCOREBOARD);
    }

    @Override
    public @Nullable NameTag getNameTagManager() {
        if (featureManager.isFeatureEnabled(TabConstants.Feature.NAME_TAGS)) return featureManager.getFeature(TabConstants.Feature.NAME_TAGS);
        return featureManager.getFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS);
    }

    @Override
    public @Nullable TabPlayer getPlayer(@NotNull String name) {
        for (TabPlayer p : data.values()) {
            if (p.getName().equalsIgnoreCase(name)) return p;
        }
        return null;
    }

    @Override
    public @Nullable TabPlayer getPlayer(@NotNull UUID uniqueId) {
        return data.get(uniqueId);
    }

    @Override
    public @Nullable HeaderFooterManager getHeaderFooterManager() {
        return featureManager.getFeature(TabConstants.Feature.HEADER_FOOTER);
    }

    @Override
    public @NotNull Collection<? extends TabPlayer> onlinePlayers() {
        return Collections.unmodifiableCollection(data.values());
    }

    @Override
    public @Nullable TabListFormatManager getTabListFormatManager() {
        return featureManager.getFeature(TabConstants.Feature.PLAYER_LIST);
    }

    @Override
    public @Nullable LayoutManager getLayoutManager() {
        return featureManager.getFeature(TabConstants.Feature.LAYOUT);
    }

    @Override
    public @Nullable SortingManager getSortingManager() {
        return featureManager.getFeature(TabConstants.Feature.SORTING);
    }

    /**
     * Sends a debug message into console if the option
     * is enabled in config.
     *
     * @param   message
     *          Message to send
     */
    public void debug(@NotNull String message) {
        if (configuration != null && configuration.isDebugMode())
            platform.logInfo(new SimpleComponent(EnumChatFormat.BLUE + "[DEBUG] " + message));
    }
}
