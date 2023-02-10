package me.neznamy.tab.shared.backend;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.protocol.PacketBuilder;
import me.neznamy.tab.shared.Platform;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import org.jetbrains.annotations.Nullable;

public abstract class BackendPlatform extends Platform {

    public BackendPlatform(PacketBuilder packetBuilder) {
        super(packetBuilder);
    }

    @Override
    public @Nullable TabFeature getGlobalPlayerlist() {
        return null;
    }

    @Override
    public @Nullable RedisSupport getRedisSupport() {
        return null;
    }
}
