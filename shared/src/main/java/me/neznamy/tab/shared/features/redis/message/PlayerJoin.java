package me.neznamy.tab.shared.features.redis.message;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@NoArgsConstructor
public class PlayerJoin extends RedisMessage {

    private RedisSupport redisSupport;
    @Getter private RedisPlayer decodedPlayer;
    private TabPlayer encodedPlayer;

    public PlayerJoin(@NotNull RedisSupport redisSupport, @NotNull TabPlayer encodedPlayer) {
        this.redisSupport = redisSupport;
        this.encodedPlayer = encodedPlayer;
    }

    @Override
    public void write(@NotNull ByteArrayDataOutput out) {
        writeUUID(out, encodedPlayer.getTablistId());
        out.writeUTF(encodedPlayer.getName());
        out.writeUTF(encodedPlayer.getServer());
        out.writeBoolean(encodedPlayer.isVanished());
        out.writeBoolean(encodedPlayer.hasPermission(TabConstants.Permission.STAFF));
        redisSupport.getFeatures().forEach(f -> f.write(out, encodedPlayer));
    }

    @Override
    public void read(@NotNull ByteArrayDataInput in) {
        redisSupport = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE); // this is not ideal
        UUID uniqueId = readUUID(in);
        String name = in.readUTF();
        String server = in.readUTF();
        boolean vanished = in.readBoolean();
        boolean staff = in.readBoolean();
        decodedPlayer = new RedisPlayer(uniqueId, name, name, server, vanished, staff);
        redisSupport.getFeatures().forEach(f -> f.read(in, decodedPlayer));
    }

    @Override
    public void process(@NotNull RedisSupport redisSupport) {
        redisSupport.getRedisPlayers().put(decodedPlayer.getUniqueId(), decodedPlayer);
        redisSupport.getFeatures().forEach(f -> f.onJoin(decodedPlayer));
    }
}
