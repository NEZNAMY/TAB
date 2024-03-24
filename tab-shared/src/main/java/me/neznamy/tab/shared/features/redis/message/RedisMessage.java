package me.neznamy.tab.shared.features.redis.message;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class RedisMessage {

    public void writeUUID(@NotNull ByteArrayDataOutput out, @NotNull UUID id) {
        out.writeLong(id.getMostSignificantBits());
        out.writeLong(id.getLeastSignificantBits());
    }

    public UUID readUUID(@NotNull ByteArrayDataInput in) {
        return new UUID(in.readLong(), in.readLong());
    }

    public abstract void write(@NotNull ByteArrayDataOutput out);

    public abstract void read(@NotNull ByteArrayDataInput in);

    public abstract void process(@NotNull RedisSupport redisSupport);
}
