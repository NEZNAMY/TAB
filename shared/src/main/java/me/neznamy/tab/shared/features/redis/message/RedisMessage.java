package me.neznamy.tab.shared.features.redis.message;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.NonNull;
import me.neznamy.tab.shared.features.redis.RedisSupport;

import java.util.UUID;

public abstract class RedisMessage {

    public void writeUUID(@NonNull ByteArrayDataOutput out, @NonNull UUID id) {
        out.writeLong(id.getMostSignificantBits());
        out.writeLong(id.getLeastSignificantBits());
    }

    public UUID readUUID(@NonNull ByteArrayDataInput in) {
        return new UUID(in.readLong(), in.readLong());
    }

    public abstract void write(@NonNull ByteArrayDataOutput out);

    public abstract void read(@NonNull ByteArrayDataInput in);

    public abstract void process(@NonNull RedisSupport redisSupport);
}
