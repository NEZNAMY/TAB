package me.neznamy.tab.shared.features.redis.message;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.redis.RedisSupport;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class PlayerQuit extends RedisMessage {

    private UUID playerId;

    @Override
    public void write(@NonNull ByteArrayDataOutput out) {
        writeUUID(out, playerId);
    }

    @Override
    public void read(@NonNull ByteArrayDataInput in) {
        playerId = readUUID(in);
    }

    @Override
    public void process(@NonNull RedisSupport redisSupport) {
        RedisPlayer target = redisSupport.getRedisPlayers().get(playerId);
        if (target == null) return; // Print warn?
        redisSupport.getFeatures().forEach(f -> f.onQuit(target));
        redisSupport.getRedisPlayers().remove(target.getUniqueId());
    }
}
