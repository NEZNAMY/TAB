package me.neznamy.tab.shared.backend;

import lombok.Getter;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.shared.Platform;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.permission.LuckPerms;
import me.neznamy.tab.shared.permission.None;
import me.neznamy.tab.shared.permission.PermissionPlugin;

public abstract class BackendPlatform extends Platform {

    @Getter private final RedisSupport redisSupport = null;

    @Override
    public PermissionPlugin detectPermissionPlugin() {
        if (getPluginVersion(TabConstants.Plugin.LUCKPERMS) != null) {
            return new LuckPerms(getPluginVersion(TabConstants.Plugin.LUCKPERMS));
        }
        return new None();
    }
}
