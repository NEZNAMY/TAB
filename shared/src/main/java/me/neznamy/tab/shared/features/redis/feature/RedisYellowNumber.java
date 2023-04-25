package me.neznamy.tab.shared.features.redis.feature;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.YellowNumber;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.redis.message.RedisMessage;
import me.neznamy.tab.shared.platform.TabPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class RedisYellowNumber extends RedisFeature {

    private final RedisSupport redisSupport;
    private final Map<RedisPlayer, Integer> values = new WeakHashMap<>();

    public RedisYellowNumber(RedisSupport redisSupport) {
        this.redisSupport = redisSupport;
        redisSupport.registerMessage("yellow-number", Update.class, Update::new);
    }

    @Override
    public void onJoin(TabPlayer player) {
        for (RedisPlayer redis : redisSupport.getRedisPlayers().values()) {
            player.getScoreboard().setScore(YellowNumber.OBJECTIVE_NAME, redis.getNickname(), values.get(redis));
        }
    }

    @Override
    public void onJoin(RedisPlayer player) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            viewer.getScoreboard().setScore(YellowNumber.OBJECTIVE_NAME, player.getNickname(), values.get(player));
        }
    }

    @Override
    public void onServerSwitch(TabPlayer player) {
        onJoin(player);
    }

    @Override
    public void onServerSwitch(RedisPlayer player) {
        // No action is needed
    }

    @Override
    public void onQuit(RedisPlayer player) {
        // No action is needed
    }

    @Override
    public void write(@NonNull ByteArrayDataOutput out, TabPlayer player) {
        out.writeInt(TAB.getInstance().getErrorManager().parseInteger(
                player.getProperty(TabConstants.Property.YELLOW_NUMBER).get(), 0));
    }

    @Override
    public void read(@NonNull ByteArrayDataInput in, RedisPlayer player) {
        values.put(player, in.readInt());
    }

    public int getValue(RedisPlayer player) {
        return values.get(player);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public class Update extends RedisMessage {

        private UUID playerId;
        private int value;

        @Override
        public void write(@NonNull ByteArrayDataOutput out) {
            writeUUID(out, playerId);
            out.writeInt(value);
        }

        @Override
        public void read(@NonNull ByteArrayDataInput in) {
            playerId = readUUID(in);
            value = in.readInt();
        }

        @Override
        public void process(@NonNull RedisSupport redisSupport) {
            RedisPlayer target = redisSupport.getRedisPlayers().get(playerId);
            if (target == null) return; // Print warn?
            values.put(target, value);
            onJoin(target);
        }
    }
}
