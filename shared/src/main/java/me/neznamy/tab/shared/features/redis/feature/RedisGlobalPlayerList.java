package me.neznamy.tab.shared.features.redis.feature;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.globalplayerlist.GlobalPlayerList;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.TabList;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.WeakHashMap;

@RequiredArgsConstructor
public class RedisGlobalPlayerList extends RedisFeature {

    private final RedisSupport redisSupport;
    private final GlobalPlayerList globalPlayerList;
    private final Map<RedisPlayer, TabList.Skin> skins = new WeakHashMap<>();

    @Override
    public void onJoin(@NonNull TabPlayer player) {
        for (RedisPlayer redis : redisSupport.getRedisPlayers().values()) {
            if (!redis.getServer().equals(player.getServer()) && shouldSee(player, redis)) {
                player.getTabList().addEntry(getEntry(redis));
            }
        }
    }

    @Override
    public void onJoin(@NonNull RedisPlayer player) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (shouldSee(viewer, player) && !viewer.getServer().equals(player.getServer())) {
                viewer.getTabList().addEntry(getEntry(player));
            }
        }
    }

    @Override
    public void onServerSwitch(@NonNull TabPlayer player) {
        onJoin(player);
    }

    @Override
    public void onServerSwitch(@NonNull RedisPlayer player) {
        TAB.getInstance().getCPUManager().runTaskLater(200, redisSupport.getFeatureName(), TabConstants.CpuUsageCategory.SERVER_SWITCH, () -> {
            for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
                if (viewer.getServer().equals(player.getServer())) continue;
                if (shouldSee(viewer, player)) {
                    viewer.getTabList().addEntry(getEntry(player));
                } else {
                    viewer.getTabList().removeEntry(player.getUniqueId());
                }
            }
        });
    }

    @Override
    public void onQuit(@NonNull RedisPlayer player) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            if (!player.getServer().equals(viewer.getServer())) {
                viewer.getTabList().removeEntry(player.getUniqueId());
            }
        }
    }

    @Override
    public void write(@NonNull ByteArrayDataOutput out, @NonNull TabPlayer player) {
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
    public void read(@NonNull ByteArrayDataInput in, @NonNull RedisPlayer player) {
        if (in.readBoolean()) {
            String value = in.readUTF();
            String signature = null;
            if (in.readBoolean()) {
                signature = in.readUTF();
            }
            skins.put(player, new TabList.Skin(value, signature));
        }
    }

    private boolean shouldSee(@NonNull TabPlayer viewer, @NonNull RedisPlayer target) {
        if (target.isVanished() && !viewer.hasPermission(TabConstants.Permission.SEE_VANISHED)) return false;
        if (globalPlayerList.isSpyServer(viewer.getServer())) return true;
        return globalPlayerList.getServerGroup(viewer.getServer()).equals(globalPlayerList.getServerGroup(target.getServer()));
    }

    private @NotNull TabList.Entry getEntry(@NonNull RedisPlayer player) {
        return new TabList.Entry(player.getUniqueId(), player.getNickname(), skins.get(player), 0, 0,
                redisSupport.getRedisPlayerList() == null ? null :
                        IChatBaseComponent.optimizedComponent(redisSupport.getRedisPlayerList().getFormat(player)));
    }
}
