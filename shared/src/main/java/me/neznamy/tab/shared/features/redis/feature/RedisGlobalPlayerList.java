package me.neznamy.tab.shared.features.redis.feature;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.GlobalPlayerList;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.TabList;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class RedisGlobalPlayerList extends RedisFeature {

    private final RedisSupport redisSupport;
    private final GlobalPlayerList globalPlayerList;

    @Override
    public void onJoin(@NotNull TabPlayer player) {
        globalPlayerList.getCustomThread().execute(() -> {
            for (RedisPlayer redis : redisSupport.getRedisPlayers().values()) {
                if (!redis.server.equals(player.server) && shouldSee(player, redis)) {
                    player.getTabList().addEntry(getEntry(redis));
                }
            }
        }, redisSupport.getFeatureName(), TabConstants.CpuUsageCategory.PLAYER_JOIN);
    }

    @Override
    public void onJoin(@NotNull RedisPlayer player) {
        globalPlayerList.getCustomThread().execute(() -> {
            for (TabPlayer viewer : globalPlayerList.getOnlinePlayers().getPlayers()) {
                if (shouldSee(viewer, player) && !viewer.server.equals(player.server)) {
                    viewer.getTabList().addEntry(getEntry(player));
                }
            }
        }, redisSupport.getFeatureName(), TabConstants.CpuUsageCategory.PLAYER_JOIN);
    }

    @Override
    public void onServerSwitch(@NotNull RedisPlayer player) {
        globalPlayerList.getCustomThread().executeLater(() -> {
            for (TabPlayer viewer : globalPlayerList.getOnlinePlayers().getPlayers()) {
                if (viewer.server.equals(player.server)) continue;
                if (shouldSee(viewer, player)) {
                    viewer.getTabList().addEntry(getEntry(player));
                } else {
                    viewer.getTabList().removeEntry(player.getUniqueId());
                }
            }
        }, redisSupport.getFeatureName(), TabConstants.CpuUsageCategory.SERVER_SWITCH, 200);
    }

    @Override
    public void onQuit(@NotNull RedisPlayer player) {
        globalPlayerList.getCustomThread().execute(() -> {
            for (TabPlayer viewer : globalPlayerList.getOnlinePlayers().getPlayers()) {
                if (!player.server.equals(viewer.server)) {
                    viewer.getTabList().removeEntry(player.getUniqueId());
                }
            }
        }, redisSupport.getFeatureName(), TabConstants.CpuUsageCategory.PLAYER_QUIT);
    }

    @Override
    public void write(@NotNull ByteArrayDataOutput out, @NotNull TabPlayer player) {
        out.writeBoolean(player.getSkin() != null);
        if (player.getSkin() != null) {
            out.writeUTF(player.getSkin().getValue());
            out.writeBoolean(player.getSkin().getSignature() != null);
            if (player.getSkin().getSignature() != null) {
                out.writeUTF(player.getSkin().getSignature());
            }
        }
    }

    @Override
    public void read(@NotNull ByteArrayDataInput in, @NotNull RedisPlayer player) {
        if (in.readBoolean()) {
            String value = in.readUTF();
            String signature = null;
            if (in.readBoolean()) {
                signature = in.readUTF();
            }
            player.setSkin(new TabList.Skin(value, signature));
        }
    }

    @Override
    public void onTabListClear(@NotNull TabPlayer player) {
        onJoin(player);
    }

    private boolean shouldSee(@NotNull TabPlayer viewer, @NotNull RedisPlayer target) {
        if (target.isVanished() && !viewer.hasPermission(TabConstants.Permission.SEE_VANISHED)) return false;
        if (globalPlayerList.isSpyServer(viewer.server)) return true;
        return globalPlayerList.getServerGroup(viewer.server).equals(globalPlayerList.getServerGroup(target.server));
    }

    @NotNull
    private TabList.Entry getEntry(@NotNull RedisPlayer player) {
        return new TabList.Entry(player.getUniqueId(), player.getNickname(), player.getSkin(), true, 0, 0, player.getTabFormat());
    }

    @Override
    public void onVanishStatusChange(@NotNull RedisPlayer player) {
        globalPlayerList.getCustomThread().execute(() -> {
            if (player.isVanished()) {
                for (TabPlayer all : globalPlayerList.getOnlinePlayers().getPlayers()) {
                    if (!shouldSee(all, player)) {
                        all.getTabList().removeEntry(player.getUniqueId());
                    }
                }
            } else {
                for (TabPlayer viewer : globalPlayerList.getOnlinePlayers().getPlayers()) {
                    if (shouldSee(viewer, player)) {
                        viewer.getTabList().addEntry(getEntry(player));
                    }
                }
            }
        }, redisSupport.getFeatureName(), TabConstants.CpuUsageCategory.VANISH_CHANGE);
    }
}
