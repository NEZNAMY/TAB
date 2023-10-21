package me.neznamy.tab.shared.features.redis.feature;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.PlayerList;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.redis.message.RedisMessage;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RedisPlayerList extends RedisFeature {

    private final RedisSupport redisSupport;
    @Getter private final PlayerList playerList;
    private final Map<RedisPlayer, String> values = new WeakHashMap<>();

    public RedisPlayerList(@NotNull RedisSupport redisSupport, @NotNull PlayerList playerList) {
        this.redisSupport = redisSupport;
        this.playerList = playerList;
        redisSupport.registerMessage("tabformat", Update.class, Update::new);
    }

    @Override
    public void onJoin(@NotNull TabPlayer player) {
        if (player.getVersion().getMinorVersion() < 8) return;
        for (RedisPlayer redis : redisSupport.getRedisPlayers().values()) {
            player.getTabList().updateDisplayName(redis.getUniqueId(), IChatBaseComponent.optimizedComponent(values.get(redis)));
        }
    }

    @Override
    public void onJoin(@NotNull RedisPlayer player) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (viewer.getVersion().getMinorVersion() < 8) continue;
            viewer.getTabList().updateDisplayName(player.getUniqueId(), IChatBaseComponent.optimizedComponent(values.get(player)));
        }
    }

    @Override
    public void onServerSwitch(@NotNull TabPlayer player) {
        onJoin(player);
    }

    @Override
    public void write(@NotNull ByteArrayDataOutput out, @NotNull TabPlayer player) {
        out.writeUTF(player.getProperty(TabConstants.Property.TABPREFIX).get() +
                player.getProperty(TabConstants.Property.CUSTOMTABNAME).get() +
                player.getProperty(TabConstants.Property.TABSUFFIX).get());
    }

    @Override
    public void read(@NotNull ByteArrayDataInput in, @NotNull RedisPlayer player) {
        values.put(player, in.readUTF());
    }

    public String getFormat(@NotNull RedisPlayer player) {
        return values.get(player);
    }

    @Override
    public void onVanishStatusChange(@NotNull RedisPlayer player) {
        if (player.isVanished()) return;
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (viewer.getVersion().getMinorVersion() < 8) continue;
            viewer.getTabList().updateDisplayName(player.getUniqueId(), IChatBaseComponent.optimizedComponent(getFormat(player)));
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public class Update extends RedisMessage {

        private UUID playerId;
        private String format;

        @Override
        public void write(@NotNull ByteArrayDataOutput out) {
            writeUUID(out, playerId);
            out.writeUTF(format);
        }

        @Override
        public void read(@NotNull ByteArrayDataInput in) {
            playerId = readUUID(in);
            format = in.readUTF();
        }

        @Override
        public void process(@NotNull RedisSupport redisSupport) {
            RedisPlayer target = redisSupport.getRedisPlayers().get(playerId);
            if (target == null) return; // Print warn?
            values.put(target, format);
            onJoin(target);
        }
    }
}
