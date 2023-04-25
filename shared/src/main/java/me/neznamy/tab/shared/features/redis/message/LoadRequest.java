package me.neznamy.tab.shared.features.redis.message;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.redis.RedisSupport;

public class LoadRequest extends RedisMessage {

    @Override
    public void write(@NonNull ByteArrayDataOutput out) {
        // Nothing to write anymore
    }

    @Override
    public void read(@NonNull ByteArrayDataInput in) {
        // Nothing to read anymore
    }

    @Override
    public void process(@NonNull RedisSupport redisSupport) {
        redisSupport.sendMessage(new Load(redisSupport, TAB.getInstance().getOnlinePlayers()));
    }
}
