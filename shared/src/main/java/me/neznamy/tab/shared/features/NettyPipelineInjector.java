package me.neznamy.tab.shared.features;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import java.util.NoSuchElementException;
import java.util.function.Function;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;

/**
 * A pipeline injector for Netty connections. As most servers use Netty, this avoids code duplication.
 */
public abstract class NettyPipelineInjector extends PipelineInjector {

    //handler to inject before
    private final String injectPosition;

    /**
     * Constructs new instance with given parameter
     *
     * @param   injectPosition
     *          position to inject handler before
     */
    protected NettyPipelineInjector(String injectPosition) {
        this.injectPosition = injectPosition;
    }

    protected abstract Channel getChannel(TabPlayer player);

    /**
     * Injects custom channel duplex handler to prevent other plugins from overriding this one
     *
     * @param   player
     *          player to inject
     */
    @Override
    public void inject(TabPlayer player) {
        final Channel channel = getChannel(player);
        if (player.getVersion().getMinorVersion() < 8 || channel == null) return; //hello A248
        if (!channel.pipeline().names().contains(injectPosition)) {
            //fake player or waterfall bug
            return;
        }
        uninject(player);
        try {
            channel.pipeline().addBefore(injectPosition, TabConstants.PIPELINE_HANDLER_NAME, getChannelFunction().apply(player));
        } catch (NoSuchElementException | IllegalArgumentException e) {
            //I don't really know how does this keep happening but whatever
        }
    }

    @Override
    public void uninject(TabPlayer player) {
        final Channel channel = getChannel(player);
        if (player.getVersion().getMinorVersion() < 8 || channel == null) return; //hello A248
        try {
            if (channel.pipeline().names().contains(TabConstants.PIPELINE_HANDLER_NAME)) channel.pipeline().remove(TabConstants.PIPELINE_HANDLER_NAME);
        } catch (NoSuchElementException e) {
            //for whatever reason this rarely throws
            //java.util.NoSuchElementException: TABReader
        }
    }

    public abstract Function<TabPlayer, ChannelDuplexHandler> getChannelFunction();
}
