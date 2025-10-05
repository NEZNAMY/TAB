package me.neznamy.tab.shared.features.injection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 * A pipeline injector for Netty connections. As most servers use Netty, this avoids code duplication.
 */
@RequiredArgsConstructor
public abstract class NettyPipelineInjector extends PipelineInjector {

    //handler to inject before
    private final @NotNull String injectPosition;

    @Getter private final Function<TabPlayer, ChannelDuplexHandler> channelFunction = TabChannelDuplexHandler::new;

    @NotNull
    protected abstract Channel getChannel(@NotNull TabPlayer player);

    @Override
    public void inject(@NotNull TabPlayer player) {
        Channel channel = getChannel(player);
        if (!channel.pipeline().names().contains(injectPosition)) return; // Player got disconnected instantly or fake player
        uninject(player);
        try {
            channel.pipeline().addBefore(injectPosition, TabConstants.PIPELINE_HANDLER_NAME, getChannelFunction().apply(player));
        } catch (NoSuchElementException | IllegalArgumentException e) {
            //I don't really know how does this keep happening but whatever
        }
    }

    @Override
    public void uninject(@NotNull TabPlayer player) {
        Channel channel = getChannel(player);
        try {
            if (channel.pipeline().names().contains(TabConstants.PIPELINE_HANDLER_NAME)) channel.pipeline().remove(TabConstants.PIPELINE_HANDLER_NAME);
        } catch (NoSuchElementException e) {
            //for whatever reason this rarely throws
            //java.util.NoSuchElementException: TAB
        }
    }

    /**
     * TAB's custom channel duplex handler.
     */
    @RequiredArgsConstructor
    public static class TabChannelDuplexHandler extends ChannelDuplexHandler {

        /** Injected player */
        protected final TabPlayer player;

        @Override
        public void write(@NotNull ChannelHandlerContext context, @Nullable Object packet, @NotNull ChannelPromise channelPromise) {
            Object newPacket = packet;
            try {
                if (newPacket == null) {
                    return;
                }
                if (player.getVersion().getMinorVersion() >= 8) {
                    newPacket = ((TrackedTabList<?>)player.getTabList()).onPacketSend(newPacket);
                }
                newPacket = ((SafeScoreboard<?>)player.getScoreboard()).onPacketSend(newPacket);
            } catch (Throwable e) {
                TAB.getInstance().getErrorManager().printError("An error occurred when reading packets", e);
            }
            try {
                super.write(context, newPacket, channelPromise);
            } catch (Throwable e) {
                TAB.getInstance().getErrorManager().printError(String.format("Failed to forward packet %s to %s", newPacket, player.getName()), e);
            }
        }
    }
}
