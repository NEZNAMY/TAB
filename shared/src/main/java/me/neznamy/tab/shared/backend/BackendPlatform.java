package me.neznamy.tab.shared.backend;

import lombok.Getter;
import me.neznamy.tab.shared.platform.Platform;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.permission.LuckPerms;
import me.neznamy.tab.shared.permission.None;
import me.neznamy.tab.shared.permission.PermissionPlugin;
import me.neznamy.tab.shared.util.ReflectionUtils;

public abstract class BackendPlatform extends Platform {

    @Getter private final RedisSupport redisSupport = null;

    @Override
    public PermissionPlugin detectPermissionPlugin() {
        if (ReflectionUtils.classExists("net.luckperms.api.LuckPerms")) {
            return new LuckPerms();
        }
        return new None();
    }
}
