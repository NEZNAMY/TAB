package me.neznamy.tab.shared.features.redis.feature;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.features.YellowNumber;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.redis.message.RedisMessage;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RedisYellowNumber extends RedisFeature {

    private final RedisSupport redisSupport;
    @Getter private final YellowNumber yellowNumber;

    public RedisYellowNumber(@NotNull RedisSupport redisSupport, @NotNull YellowNumber yellowNumber) {
        this.redisSupport = redisSupport;
        this.yellowNumber = yellowNumber;
        redisSupport.registerMessage("yellow-number", Update.class, Update::new);
    }

    @Override
    public void load() {
        TAB.getInstance().getCpu().execute(yellowNumber.getCustomThread(), () -> {
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                redisSupport.sendMessage(new Update(all.getTablistId(), yellowNumber.getValueNumber(all), all.playerlistObjectiveData.valueModern.get()));
            }
        }, redisSupport.getFeatureName(), "Resending all Playerlist Objective data on reload");
    }

    @Override
    public void onJoin(@NotNull TabPlayer player) {
        for (RedisPlayer redis : redisSupport.getRedisPlayers().values()) {
            player.getScoreboard().setScore(
                    YellowNumber.OBJECTIVE_NAME,
                    redis.getNickname(),
                    redis.getPlayerlistNumber(),
                    null, // Unused by this objective slot
                    redis.getPlayerlistFancy()
            );
        }
        TAB.getInstance().getCpu().execute(yellowNumber.getCustomThread(),
                () -> redisSupport.sendMessage(new Update(player.getTablistId(), yellowNumber.getValueNumber(player), player.playerlistObjectiveData.valueModern.get())),
                redisSupport.getFeatureName(), "Updating Playerlist Objective");
    }

    @Override
    public void onJoin(@NotNull RedisPlayer player) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            viewer.getScoreboard().setScore(
                    YellowNumber.OBJECTIVE_NAME,
                    player.getNickname(),
                    player.getPlayerlistNumber(),
                    null, // Unused by this objective slot
                    player.getPlayerlistFancy()
            );
        }
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
            target.setPlayerlistNumber(value);
            target.setPlayerlistFancy(TabComponent.optimized(fancyValue));
            onJoin(target);
        }
    }
}
