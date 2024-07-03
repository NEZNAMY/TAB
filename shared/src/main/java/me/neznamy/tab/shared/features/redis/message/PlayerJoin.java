package me.neznamy.tab.shared.features.redis.message;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@NoArgsConstructor
public class PlayerJoin extends RedisMessage {

    @Getter private RedisPlayer decodedPlayer;
    private TabPlayer encodedPlayer;

    public PlayerJoin(@NotNull TabPlayer encodedPlayer) {
        this.encodedPlayer = encodedPlayer;
    }

    @Override
    public void write(@NotNull ByteArrayDataOutput out) {
        writeUUID(out, encodedPlayer.getTablistId());
        out.writeUTF(encodedPlayer.getName());
        out.writeUTF(encodedPlayer.server);
        out.writeBoolean(encodedPlayer.isVanished());
        out.writeBoolean(encodedPlayer.hasPermission(TabConstants.Permission.STAFF));
        out.writeBoolean(encodedPlayer.getSkin() != null);

        // Load skin immediately to make global playerlist stuff not too complicated
        if (encodedPlayer.getSkin() != null) {
            out.writeUTF(encodedPlayer.getSkin().getValue());
            out.writeBoolean(encodedPlayer.getSkin().getSignature() != null);
            if (encodedPlayer.getSkin().getSignature() != null) {
                out.writeUTF(encodedPlayer.getSkin().getSignature());
            }
        }
    }

    @Override
    public void read(@NotNull ByteArrayDataInput in) {
        UUID uniqueId = readUUID(in);
        String name = in.readUTF();
        String server = in.readUTF();
        boolean vanished = in.readBoolean();
        boolean staff = in.readBoolean();
        decodedPlayer = new RedisPlayer(uniqueId, name, name, server, vanished, staff);

        // Load skin immediately to make global playerlist stuff not too complicated
        if (in.readBoolean()) {
            String value = in.readUTF();
            String signature = null;
            if (in.readBoolean()) {
                signature = in.readUTF();
            }
            decodedPlayer.setSkin(new TabList.Skin(value, signature));
        }
    }

    @Override
    public void process(@NotNull RedisSupport redisSupport) {
        TAB.getInstance().debug("Processing join of redis player " + decodedPlayer.getName());
        redisSupport.getRedisPlayers().put(decodedPlayer.getUniqueId(), decodedPlayer);
        TAB.getInstance().getFeatureManager().onJoin(decodedPlayer);
    }
}
