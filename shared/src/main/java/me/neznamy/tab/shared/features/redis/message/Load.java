package me.neznamy.tab.shared.features.redis.message;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.platform.TabPlayer;

@NoArgsConstructor
public class Load extends RedisMessage {

    private RedisSupport redisSupport;
    private TabPlayer[] players;
    private PlayerJoin[] decodedPlayers;

    public Load(RedisSupport redisSupport, TabPlayer[] players) {
        this.redisSupport = redisSupport;
        this.players = players;
    }

    @Override
    public void write(@NonNull ByteArrayDataOutput out) {
        out.writeInt(players.length);
        for (TabPlayer player : players) {
            new PlayerJoin(redisSupport, player).write(out);
        }
    }

    @Override
    public void read(@NonNull ByteArrayDataInput in) {
        int count = in.readInt();
        decodedPlayers = new PlayerJoin[count];
        for (int i=0; i<count; i++) {
            PlayerJoin join = new PlayerJoin();
            join.read(in);
            decodedPlayers[i] = join;
        }
    }

    @Override
    public void process(@NonNull RedisSupport redisSupport) {
        for (PlayerJoin join : decodedPlayers) {
            if (!redisSupport.getRedisPlayers().containsKey(join.getDecodedPlayer().getUniqueId())) {
                join.process(redisSupport);
            }
        }
    }
}
