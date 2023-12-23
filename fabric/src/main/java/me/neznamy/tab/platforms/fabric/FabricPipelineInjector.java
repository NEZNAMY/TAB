package me.neznamy.tab.platforms.fabric;

import io.netty.channel.Channel;
import lombok.SneakyThrows;
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Pipeline injector for Fabric.
 */
public class FabricPipelineInjector extends NettyPipelineInjector {

    /**
     * Constructs new instance.
     */
    public FabricPipelineInjector() {
        super("packet_handler");
    }

    @Override
    @NotNull
    @SneakyThrows
    protected Channel getChannel(@NotNull TabPlayer player) {
        return FabricMultiVersion.getChannel.apply(((FabricTabPlayer)player).getPlayer());
    }

    @Override
    public boolean isLogin(@NotNull Object packet) {
        return false;
    }
}
