package me.neznamy.tab.platforms.bukkit.provider.reflection;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.platforms.bukkit.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.provider.ComponentConverter;
import me.neznamy.tab.platforms.bukkit.provider.ImplementationProvider;
import me.neznamy.tab.platforms.bukkit.provider.viaversion.ViaScoreboard;
import me.neznamy.tab.platforms.bukkit.provider.viaversion.ViaTabList;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.util.ReflectionUtils;
import me.neznamy.tab.shared.util.function.FunctionWithException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * Implementation provider using reflection for NMS for 1.19.3+.
 */
@Getter
public class ReflectionImplementationProvider implements ImplementationProvider {

    @Nullable
    private final FunctionWithException<BukkitTabPlayer, Channel> channelFunction = findChannelFunction();

    @Nullable
    private final ComponentConverter componentConverter = new ModernComponentConverter();

    /**
     * Constructs new instance and loads all NMS classes.
     *
     * @throws ReflectiveOperationException
     *         If reflection fails
     */
    public ReflectionImplementationProvider() throws ReflectiveOperationException {
        PacketScoreboard.load();
        PacketTabList.load();
    }

    @NotNull
    private FunctionWithException<BukkitTabPlayer, Channel> findChannelFunction() {
        Class<?> NetworkManager = BukkitReflection.getClass("network.Connection", "network.NetworkManager");
        Class<?> PlayerConnection = BukkitReflection.getClass("server.network.ServerGamePacketListenerImpl",
                "server.network.PlayerConnection");
        Class<?> EntityPlayer = BukkitReflection.getClass("server.level.ServerPlayer", "server.level.EntityPlayer");
        Field PLAYER_CONNECTION = ReflectionUtils.getOnlyField(EntityPlayer, PlayerConnection);
        Field NETWORK_MANAGER;
        if (BukkitReflection.is1_20_2Plus()) {
            NETWORK_MANAGER = ReflectionUtils.getOnlyField(PlayerConnection.getSuperclass(), NetworkManager);
        } else {
            NETWORK_MANAGER = ReflectionUtils.getOnlyField(PlayerConnection, NetworkManager);
        }
        Field CHANNEL = ReflectionUtils.getOnlyField(NetworkManager, Channel.class);
        return player -> (Channel) CHANNEL.get(NETWORK_MANAGER.get(PLAYER_CONNECTION.get(player.getHandle())));
    }

    @Override
    @NotNull
    public Scoreboard newScoreboard(@NotNull BukkitTabPlayer player) {
        return new PacketScoreboard(player);
    }

    @Override
    public void onPacketSend(@NonNull Object packet, @NonNull ViaScoreboard scoreboard) {
        PacketScoreboard.onPacketSend(packet, scoreboard);
    }

    @Override
    public void onPacketSend(@NonNull Object packet, @NonNull ViaTabList tabList) {
        PacketTabList.onPacketSend(packet, tabList);
    }

    @Override
    @NotNull
    public TabList newTabList(@NotNull BukkitTabPlayer player) {
        return new PacketTabList(player);
    }

    @Override
    @Nullable
    public TabList.Skin getSkin(@NotNull BukkitTabPlayer player) {
        return PacketTabList.getSkin(player);
    }

    @Override
    public int getPing(@NotNull BukkitTabPlayer player) {
        return player.getPlayer().getPing();
    }
}
