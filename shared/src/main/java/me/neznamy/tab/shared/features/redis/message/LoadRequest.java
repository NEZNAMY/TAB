package me.neznamy.tab.shared.features.redis.message;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import org.jetbrains.annotations.NotNull;

public class LoadRequest extends RedisMessage {

    @Override
    public void write(@NotNull ByteArrayDataOutput out) {
        // Nothing to write anymore
    }

    @Override
    public void read(@NotNull ByteArrayDataInput in) {
        // Nothing to read anymore
    }

    @Override
    public void process(@NotNull RedisSupport redisSupport) {
        redisSupport.sendMessage(new Load(redisSupport, TAB.getInstance().getOnlinePlayers()));
    }
}
