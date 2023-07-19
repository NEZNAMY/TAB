package me.neznamy.tab.shared.backend;

import me.neznamy.tab.shared.GroupManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.hook.LuckPermsHook;
import me.neznamy.tab.shared.platform.Platform;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import org.jetbrains.annotations.NotNull;

public interface BackendPlatform extends Platform {

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
        new BackendPlaceholderRegistry(this).registerPlaceholders(TAB.getInstance().getPlaceholderManager());
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
