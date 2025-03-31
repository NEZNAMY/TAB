package me.neznamy.tab.platforms.forge;

import io.netty.channel.Channel;
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Pipeline injector for Forge.
 */
public class ForgePipelineInjector extends NettyPipelineInjector {

    /**
     * Constructs new instance.
     */
    public ForgePipelineInjector() {
        super("packet_handler");
    }

    @Override
    @NotNull
    protected Channel getChannel(@NotNull TabPlayer player) {
        return ((ForgeTabPlayer)player).getPlayer().connection.getConnection().channel();
    }
}
