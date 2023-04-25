package me.neznamy.tab.shared.features.redis.feature;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.NonNull;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.platform.TabPlayer;

public abstract class RedisFeature {

    public abstract void onJoin(TabPlayer player);

    public abstract void onJoin(RedisPlayer player);

    public abstract void onServerSwitch(TabPlayer player);

    public abstract void onServerSwitch(RedisPlayer player);

    public abstract void onQuit(RedisPlayer player);

    public abstract void write(@NonNull ByteArrayDataOutput out, TabPlayer player);

    public abstract void read(@NonNull ByteArrayDataInput in, RedisPlayer player);
}
