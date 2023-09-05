package me.neznamy.tab.shared.backend;

import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.shared.GroupManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.hook.LuckPermsHook;
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry;
import me.neznamy.tab.shared.platform.Platform;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import org.jetbrains.annotations.NotNull;

public interface BackendPlatform<T> extends Platform<T> {

    @Override
    @NotNull default GroupManager detectPermissionPlugin() {
        if (LuckPermsHook.getInstance().isInstalled()) {
            return new GroupManager("LuckPerms", LuckPermsHook.getInstance().getGroupFunction());
        }
        return new GroupManager("None", p -> TabConstants.NO_GROUP);
    }

    default RedisSupport getRedisSupport() { return null; }

    @Override
    default void registerPlaceholders() {
        UniversalPlaceholderRegistry registry = new UniversalPlaceholderRegistry();
        PlaceholderManager manager = TAB.getInstance().getPlaceholderManager();
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.HEALTH, 100,
                p -> (int) Math.ceil(((BackendTabPlayer)p).getHealth()));
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.DISPLAY_NAME, 500,
                p -> ((BackendTabPlayer)p).getDisplayName());
        manager.registerServerPlaceholder(TabConstants.Placeholder.TPS, 1000,
                () -> registry.formatTPS(getTPS()));
        manager.registerServerPlaceholder(TabConstants.Placeholder.MSPT, 1000,
                () -> registry.format(getMSPT()));
        registry.registerPlaceholders(manager);
    }

    /**
     * Returns server's TPS for {@link TabConstants.Placeholder#TPS} placeholder
     *
     * @return  server's TPS
     */
    double getTPS();

    /**
     * Returns server's MSPT for {@link TabConstants.Placeholder#MSPT} placeholder
     *
     * @return  server's MSPT
     */
    double getMSPT();
}
