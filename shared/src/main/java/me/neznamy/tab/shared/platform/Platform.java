package me.neznamy.tab.shared.platform;

import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.permission.PermissionPlugin;
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
    @NotNull PermissionPlugin detectPermissionPlugin();

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

    @Nullable PipelineInjector createPipelineInjector();

    @NotNull NameTag getUnlimitedNameTags();

    @NotNull TabExpansion createTabExpansion();

    @Nullable RedisSupport getRedisSupport();

    @Nullable TabFeature getPerWorldPlayerList();

    void sendConsoleMessage(@NotNull IChatBaseComponent message);
}