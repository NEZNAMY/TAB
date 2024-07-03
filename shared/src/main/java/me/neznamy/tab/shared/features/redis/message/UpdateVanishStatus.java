package me.neznamy.tab.shared.features.redis.message;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class UpdateVanishStatus extends RedisMessage {

    private UUID playerId;
    private boolean vanished;

    @Override
    public void write(@NotNull ByteArrayDataOutput out) {
        writeUUID(out, playerId);
        out.writeBoolean(vanished);
    }

    @Override
    public void read(@NotNull ByteArrayDataInput in) {
        playerId = readUUID(in);
        vanished = in.readBoolean();
    }

    @Override
    public void process(@NotNull RedisSupport redisSupport) {
        RedisPlayer target = redisSupport.getRedisPlayers().get(playerId);
        if (target == null) {
            TAB.getInstance().getErrorManager().printError("Unable to process vanish status update of redis player " + playerId + ", because no such player exists", null);
            return;
        }
        TAB.getInstance().debug("Processing vanish status update of redis player " + target.getName());
        target.setVanished(vanished);
        TAB.getInstance().getFeatureManager().onVanishStatusChange(target);
    }
}
