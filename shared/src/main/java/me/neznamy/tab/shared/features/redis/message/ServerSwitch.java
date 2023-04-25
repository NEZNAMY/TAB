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
public class ServerSwitch extends RedisMessage {

    private UUID playerId;
    private String newServer;

    @Override
    public void write(@NonNull ByteArrayDataOutput out) {
        writeUUID(out, playerId);
        out.writeUTF(newServer);
    }

    @Override
    public void read(@NonNull ByteArrayDataInput in) {
        playerId = readUUID(in);
        newServer = in.readUTF();
    }

    @Override
    public void process(@NonNull RedisSupport redisSupport) {
        RedisPlayer target = redisSupport.getRedisPlayers().get(playerId);
        if (target == null) return; // Print warn?
        target.setServer(newServer);
        redisSupport.getFeatures().forEach(f -> f.onServerSwitch(target));
    }
}
