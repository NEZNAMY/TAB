package me.neznamy.tab.shared.features.injection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import java.util.NoSuchElementException;
import java.util.function.Function;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.ProtocolVersion;
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
        Channel channel = getChannel(player);
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
        Channel channel = getChannel(player);
        if (player.getVersion().getMinorVersion() < 8 || channel == null) return; //hello A248
        try {
            if (channel.pipeline().names().contains(TabConstants.PIPELINE_HANDLER_NAME)) channel.pipeline().remove(TabConstants.PIPELINE_HANDLER_NAME);
        } catch (NoSuchElementException e) {
            //for whatever reason this rarely throws
            //java.util.NoSuchElementException: TAB
        }
    }

    public abstract boolean isLogin(@NotNull Object packet);

    @RequiredArgsConstructor
    public class TabChannelDuplexHandler extends ChannelDuplexHandler {

        /** Injected player */
        protected final TabPlayer player;

        @Override
        public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) {
            try {
                if (player.getVersion().getMinorVersion() >= 8)
                    player.getTabList().onPacketSend(packet);
                if (player.getScoreboard().isDisplayObjective(packet))
                    player.getScoreboard().onDisplayObjective(packet);
                if (player.getScoreboard().isObjective(packet))
                    player.getScoreboard().onObjective(packet);
                if (antiOverrideTeams && player.getScoreboard().isTeamPacket(packet)) {
                    long time = System.nanoTime();
                    player.getScoreboard().onTeamPacket(packet);
                    TAB.getInstance().getCPUManager().addTime("NameTags", TabConstants.CpuUsageCategory.ANTI_OVERRIDE, System.nanoTime()-time);
                }
                if (isLogin(packet)) {
                    player.getScoreboard().freeze();
                    super.write(context, packet, channelPromise);
                    TAB.getInstance().getCPUManager().runTaskLater(200, getFeatureName(),
                            TabConstants.CpuUsageCategory.PACKET_LOGIN, () -> {
                        TAB.getInstance().getFeatureManager().onLoginPacket(player);
                        if (player.getVersion().getNetworkId() >= ProtocolVersion.V1_20_2.getNetworkId()) {
                            // For 1.20.2+ we need to do this, because server switch event is called before tablist is cleared
                            TAB.getInstance().getFeatureManager().onTabListClear(player);
                        }
                    });
                    return;
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
