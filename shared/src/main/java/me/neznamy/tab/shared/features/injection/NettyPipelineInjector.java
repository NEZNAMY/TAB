package me.neznamy.tab.shared.features.injection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import java.util.NoSuchElementException;
import java.util.function.Function;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A pipeline injector for Netty connections. As most servers use Netty, this avoids code duplication.
 */
@RequiredArgsConstructor
public abstract class NettyPipelineInjector extends PipelineInjector {

    //handler to inject before
    private final @NotNull String injectPosition;

    @Getter private final Function<TabPlayer, ChannelDuplexHandler> channelFunction = TabChannelDuplexHandler::new;

    protected abstract @Nullable Channel getChannel(@NotNull TabPlayer player);

    /**
     * Injects custom channel duplex handler to prevent other plugins from overriding this one
     *
     * @param   player
     *          player to inject
     */
    @Override
    public void inject(@NotNull TabPlayer player) {
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
    public void uninject(@NotNull TabPlayer player) {
        final Channel channel = getChannel(player);
        if (player.getVersion().getMinorVersion() < 8 || channel == null) return; //hello A248
        try {
            if (channel.pipeline().names().contains(TabConstants.PIPELINE_HANDLER_NAME)) channel.pipeline().remove(TabConstants.PIPELINE_HANDLER_NAME);
        } catch (NoSuchElementException e) {
            //for whatever reason this rarely throws
            //java.util.NoSuchElementException: TAB
        }
    }

    public @Nullable TabPlayer getPlayer(@NotNull String name) {
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            if (p.getNickname().equals(name))
                return p; // Nicked name
        }
        return TAB.getInstance().getPlayer(name); // Try original name
    }

    public abstract void onDisplayObjective(@NotNull TabPlayer player, @NotNull Object packet);

    public abstract void onObjective(@NotNull TabPlayer player, @NotNull Object packet);

    public abstract boolean isDisplayObjective(@NotNull Object packet);

    public abstract boolean isObjective(@NotNull Object packet);

    public abstract boolean isTeam(@NotNull Object packet);

    public abstract boolean isPlayerInfo(@NotNull Object packet);

    public abstract void onPlayerInfo(@NotNull TabPlayer receiver, @NotNull Object packet);

    /**
     * Removes all real players from team if packet does not come from TAB and reports this to override log
     *
     * @param   teamPacket
     *          team packet
     */
    public abstract void modifyPlayers(@NotNull Object teamPacket);

    @RequiredArgsConstructor
    public class TabChannelDuplexHandler extends ChannelDuplexHandler {

        /** Injected player */
        protected final TabPlayer player;

        @Override
        public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) {
            try {
                if (isPlayerInfo(packet) && player.getVersion().getMinorVersion() >= 8)
                                                onPlayerInfo(player, packet);
                if (isDisplayObjective(packet)) onDisplayObjective(player, packet);
                if (isObjective(packet))        onObjective(player, packet);
                if (antiOverrideTeams && isTeam(packet)) {
                    long time = System.nanoTime();
                    modifyPlayers(packet);
                    TAB.getInstance().getCPUManager().addTime("NameTags", TabConstants.CpuUsageCategory.ANTI_OVERRIDE, System.nanoTime()-time);
                }
                TAB.getInstance().getFeatureManager().onPacketSend(player, packet);
            } catch (Throwable e) {
                TAB.getInstance().getErrorManager().printError("An error occurred when reading packets", e);
            }
            try {
                super.write(context, packet, channelPromise);
            } catch (Throwable e) {
                TAB.getInstance().getErrorManager().printError(String.format("Failed to forward packet %s to %s", packet.getClass().getSimpleName(), player.getName()), e);
            }
        }
    }
}
