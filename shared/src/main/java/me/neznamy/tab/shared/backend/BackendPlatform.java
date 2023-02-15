package me.neznamy.tab.shared.backend;

import lombok.Getter;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.shared.Platform;
import me.neznamy.tab.shared.features.redis.RedisSupport;

public abstract class BackendPlatform extends Platform {

    @Getter private final TabFeature globalPlayerlist = null;
    @Getter private final RedisSupport redisSupport = null;
}
