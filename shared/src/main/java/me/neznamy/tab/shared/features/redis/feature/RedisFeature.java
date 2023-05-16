package me.neznamy.tab.shared.features.redis.feature;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public abstract class RedisFeature {

    public abstract void onJoin(@NotNull TabPlayer player);

    public abstract void onJoin(@NotNull RedisPlayer player);

    public abstract void onServerSwitch(@NotNull TabPlayer player);

    public abstract void onServerSwitch(@NotNull RedisPlayer player);

    public abstract void onQuit(@NotNull RedisPlayer player);

    public abstract void write(@NotNull ByteArrayDataOutput out, @NotNull TabPlayer player);

    public abstract void read(@NotNull ByteArrayDataInput in, @NotNull RedisPlayer player);
}
