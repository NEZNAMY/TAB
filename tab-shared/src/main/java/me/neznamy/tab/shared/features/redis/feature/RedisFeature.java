package me.neznamy.tab.shared.features.redis.feature;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public abstract class RedisFeature {

    public abstract void onJoin(@NotNull TabPlayer player);

    public abstract void onJoin(@NotNull RedisPlayer player);

    public void onServerSwitch(@NotNull TabPlayer player) {/* Do nothing by default */}

    public void onServerSwitch(@NotNull RedisPlayer player) {/* Do nothing by default */}

    public void onQuit(@NotNull RedisPlayer player) {/* Do nothing by default */}

    public abstract void write(@NotNull ByteArrayDataOutput out, @NotNull TabPlayer player);

    public abstract void read(@NotNull ByteArrayDataInput in, @NotNull RedisPlayer player);

    public void onLoginPacket(@NotNull TabPlayer player) {/* Do nothing by default */}

    public void onTabListClear(@NotNull TabPlayer player) {/* Do nothing by default */}

    public void onVanishStatusChange(@NotNull RedisPlayer player) {/* Do nothing by default */}
}
