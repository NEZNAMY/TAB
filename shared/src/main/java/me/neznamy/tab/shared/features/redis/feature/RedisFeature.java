package me.neznamy.tab.shared.features.redis.feature;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.NonNull;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.platform.TabPlayer;

public abstract class RedisFeature {

    public abstract void onJoin(@NonNull TabPlayer player);

    public abstract void onJoin(@NonNull RedisPlayer player);

    public abstract void onServerSwitch(@NonNull TabPlayer player);

    public abstract void onServerSwitch(@NonNull RedisPlayer player);

    public abstract void onQuit(@NonNull RedisPlayer player);

    public abstract void write(@NonNull ByteArrayDataOutput out, @NonNull TabPlayer player);

    public abstract void read(@NonNull ByteArrayDataInput in, @NonNull RedisPlayer player);
}
