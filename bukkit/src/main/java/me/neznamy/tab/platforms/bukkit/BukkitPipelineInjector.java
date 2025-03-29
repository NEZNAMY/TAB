package me.neznamy.tab.platforms.bukkit;

import io.netty.channel.Channel;
import lombok.Setter;
import lombok.SneakyThrows;
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.function.FunctionWithException;
import org.jetbrains.annotations.NotNull;

/**
 * Pipeline injection for Bukkit 1.8+.
 */
public class BukkitPipelineInjector extends NettyPipelineInjector {

    /** Function for getting player's channel */
    @Setter
    private static FunctionWithException<BukkitTabPlayer, Channel> getChannel;

    /**
     * Constructs new instance
     */
    public BukkitPipelineInjector() {
        super("packet_handler");
    }

    /**
     * Returns {@code true} if pipeline injection is available, {@code false} if not.
     *
     * @return  {@code true} if pipeline injection is available, {@code false} if not
     */
    public static boolean isAvailable() {
        return getChannel != null;
    }

    @Override
    @NotNull
    @SneakyThrows
    protected Channel getChannel(@NotNull TabPlayer player) {
        return getChannel.apply((BukkitTabPlayer) player);
    }
}