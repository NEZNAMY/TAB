package me.neznamy.tab.platforms.bukkit;

import io.netty.channel.Channel;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Pipeline injection for Bukkit 1.8+.
 */
public class BukkitPipelineInjector extends NettyPipelineInjector {

    /**
     * Constructs new instance
     */
    public BukkitPipelineInjector() {
        super("packet_handler");
    }

    @Override
    @NotNull
    @SneakyThrows
    protected Channel getChannel(@NotNull TabPlayer player) {
        return ((BukkitPlatform)player.getPlatform()).getImplementationProvider().getChannelFunction().apply((BukkitTabPlayer) player);
    }
}