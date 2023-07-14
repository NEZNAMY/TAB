package me.neznamy.tab.shared.platform;

import me.neznamy.tab.shared.GroupManager;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An interface with methods that are called in universal code,
 * but require platform-specific API calls
 */
public interface Platform {

    /**
     * Detects permission plugin and returns its representing object
     *
     * @return  the interface representing the permission hook
     */
    @NotNull GroupManager detectPermissionPlugin();

    /**
     * Returns bossbar feature for servers 1.8 and lower
     *
     * @return  bossbar feature for 1.8 and lower
     */
    default @NotNull BossBarManagerImpl getLegacyBossBar() {
        return new BossBarManagerImpl();
    }

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
     * Returns nametag handler when unlimited nametag mode is enabled
     * in config file.
     *
     * @return  Nametag feature handler for unlimited name tags
     */
    @NotNull NameTag getUnlimitedNameTags();

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
     * @return  Created feature or null if not available on platform
     */
    @Nullable TabFeature getPerWorldPlayerList();

    /**
     * Sends a console message with TAB's prefix using logger if available,
     * otherwise platform's method for sending console message.
     *
     * @param   message
     *          Message to send
     */
    void logInfo(@NotNull IChatBaseComponent message);

    /**
     * Sends a red console message with TAB's prefix using logger with warn type if available,
     * otherwise platform's method for sending console message.
     *
     * @param   message
     *          Message to send
     */
    void logWarn(@NotNull IChatBaseComponent message);

    /**
     * Returns information about server version, which is displayed in debug command
     *
     * @return  Server version information
     */
    String getServerVersionInfo();
}