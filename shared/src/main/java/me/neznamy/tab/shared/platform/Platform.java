package me.neznamy.tab.shared.platform;

import me.neznamy.tab.shared.GroupManager;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.config.files.config.PerWorldPlayerListConfiguration;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.hook.PremiumVanishHook;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ConcurrentModificationException;

/**
 * An interface with methods that are called in universal code,
 * but require platform-specific API calls.
 */
public interface Platform {

    /**
     * Detects permission plugin and returns its representing object
     *
     * @return  the interface representing the permission hook
     */
    @NotNull GroupManager detectPermissionPlugin();

    /**
     * Creates an instance of {@link me.neznamy.tab.api.placeholder.Placeholder}
     * to handle this unknown placeholder (typically a PAPI placeholder)
     *
     * @param   identifier
     *          placeholder's identifier
     */
    void registerUnknownPlaceholder(@NotNull String identifier);

    /**
     * Creates instance for all online players and adds them to the plugin
     */
    void loadPlayers();

    /**
     * Registers all placeholders, including universal and platform-specific ones
     */
    void registerPlaceholders();

    /**
     * Creates pipeline injection instance and returns it
     *
     * @return  new pipeline injection or null if not available
     */
    @Nullable PipelineInjector createPipelineInjector();

    /**
     * Creates tab expansion instance and returns it
     *
     * @return  Created expansion
     */
    @NotNull TabExpansion createTabExpansion();

    /**
     * Creates RedisSupport feature, registers listeners and returns it
     *
     * @return  Created instance
     */
    @Nullable RedisSupport getRedisSupport();

    /**
     * Returns per world player list feature handler.
     *
     * @param   configuration
     *          Feature configuration
     * @return  Created feature or null if not available on platform
     */
    @Nullable TabFeature getPerWorldPlayerList(@NotNull PerWorldPlayerListConfiguration configuration);

    /**
     * Sends a console message with TAB's prefix using logger if available,
     * otherwise platform's method for sending console message.
     *
     * @param   message
     *          Message to send
     */
    void logInfo(@NotNull TabComponent message);

    /**
     * Sends a red console message with TAB's prefix using logger with warn type if available,
     * otherwise platform's method for sending console message.
     *
     * @param   message
     *          Message to send
     */
    void logWarn(@NotNull TabComponent message);

    /**
     * Returns information about server version, which is displayed in debug command
     *
     * @return  Server version information
     */
    String getServerVersionInfo();

    /**
     * Registers event listener for platform's events
     */
    void registerListener();

    /**
     * Registers plugin's command
     */
    void registerCommand();

    /**
     * Starts metrics
     */
    void startMetrics();

    /**
     * Returns plugin's data folder for configuration files
     *
     * @return  plugin's data folder
     */
    File getDataFolder();

    /**
     * Returns {@code true} if this platform is a proxy, {@code false} if not.
     *
     * @return  {@code true} if this platform is a proxy, {@code false} if not
     */
    boolean isProxy();

    /**
     * Converts TAB component into platform's component.
     *
     * @param   component
     *          Component to convert
     * @param   modern
     *          Whether clients supports RGB or not
     * @return  Converted component
     */
    @NotNull
    Object convertComponent(@NotNull TabComponent component, boolean modern);

    /**
     * Creates new scoreboard instance for given player.
     *
     * @param   player
     *          Player to create scoreboard for
     * @return  Scoreboard implementation for given player
     */
    @NotNull
    Scoreboard createScoreboard(@NotNull TabPlayer player);

    /**
     * Creates new bossbar instance for given player.
     *
     * @param   player
     *          Player to create bossbar for
     * @return  Bossbar implementation for given player
     */
    @NotNull
    BossBar createBossBar(@NotNull TabPlayer player);

    /**
     * Creates new tablist instance for given player.
     *
     * @param   player
     *          Player to create tablist for
     * @return  TabList implementation for given player
     */
    @NotNull
    TabList createTabList(@NotNull TabPlayer player);

    /**
     * Returns server version. On proxies, {@link ProtocolVersion#LATEST_KNOWN_VERSION} is returned.
     *
     * @return  server version on backend, {@link ProtocolVersion#LATEST_KNOWN_VERSION} on proxies
     */
    @NotNull
    ProtocolVersion getServerVersion();

    /**
     * Returns {@code true} if the viewer can see the target, {@code false} otherwise.
     * This includes all vanish, permission & plugin API checks.
     *
     * @param   viewer
     *          Player who is viewing
     * @param   target
     *          Player who is being viewed
     * @return  {@code true} if can see, {@code false} if not.
     */
    default boolean canSee(@NotNull TabPlayer viewer, @NotNull TabPlayer target) {
        try {
            if (PremiumVanishHook.getInstance() != null && PremiumVanishHook.getInstance().canSee(viewer, target)) return true;
        } catch (ConcurrentModificationException e) {
            // PV error, try again
            return canSee(viewer, target);
        }
        return !target.isVanished() || viewer.hasPermission(TabConstants.Permission.SEE_VANISHED);
    }
}