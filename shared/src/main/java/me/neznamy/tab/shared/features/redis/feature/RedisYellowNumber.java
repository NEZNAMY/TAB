package me.neznamy.tab.shared.features.redis.feature;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.YellowNumber;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.redis.message.RedisMessage;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class RedisYellowNumber extends RedisFeature {

    private final RedisSupport redisSupport;
    @Getter private final YellowNumber yellowNumber;

    @Getter private final Map<RedisPlayer, Integer> values = new WeakHashMap<>();
    @Getter private final Map<RedisPlayer, String> fancyValues = new WeakHashMap<>();

    public RedisYellowNumber(@NotNull RedisSupport redisSupport, @NotNull YellowNumber yellowNumber) {
        this.redisSupport = redisSupport;
        this.yellowNumber = yellowNumber;
        redisSupport.registerMessage("yellow-number", Update.class, Update::new);
    }

    @Override
    public void onJoin(@NotNull TabPlayer player) {
        for (RedisPlayer redis : redisSupport.getRedisPlayers().values()) {
            player.getScoreboard().setScore(
                    YellowNumber.OBJECTIVE_NAME,
                    redis.getNickname(),
                    values.get(redis),
                    null, // Unused by this objective slot
                    IChatBaseComponent.emptyToNullOptimizedComponent(fancyValues.get(redis))
            );
        }
    }

    @Override
    public void onJoin(@NotNull RedisPlayer player) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            viewer.getScoreboard().setScore(
                    YellowNumber.OBJECTIVE_NAME,
                    player.getNickname(),
                    values.get(player),
                    null, // Unused by this objective slot
                    IChatBaseComponent.emptyToNullOptimizedComponent(fancyValues.get(player))
            );
        }
    }

    @Override
    public void write(@NotNull ByteArrayDataOutput out, @NotNull TabPlayer player) {
        out.writeInt(TAB.getInstance().getErrorManager().parseInteger(player.getProperty(yellowNumber.getPROPERTY_VALUE()).get(), 0));
        out.writeUTF(player.getProperty(yellowNumber.getPROPERTY_VALUE_FANCY()).get());
    }

    @Override
    public void read(@NotNull ByteArrayDataInput in, @NotNull RedisPlayer player) {
        values.put(player, in.readInt());
        fancyValues.put(player, in.readUTF());
    }

    @Override
    public void onLoginPacket(@NotNull TabPlayer player) {
        onJoin(player);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public class Update extends RedisMessage {

        private UUID playerId;
        private int value;
        private String fancyValue;

        @Override
        public void write(@NotNull ByteArrayDataOutput out) {
            writeUUID(out, playerId);
            out.writeInt(value);
            out.writeUTF(fancyValue);
        }

        @Override
        public void read(@NotNull ByteArrayDataInput in) {
            playerId = readUUID(in);
            value = in.readInt();
            fancyValue = in.readUTF();
        }

        @Override
        public void process(@NotNull RedisSupport redisSupport) {
            RedisPlayer target = redisSupport.getRedisPlayers().get(playerId);
            if (target == null) return; // Print warn?
            values.put(target, value);
            fancyValues.put(target, fancyValue);
            onJoin(target);
        }
    }
}
