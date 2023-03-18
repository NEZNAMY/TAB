package me.neznamy.tab.shared.features.injection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import java.util.NoSuchElementException;
import java.util.function.Function;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.NotNull;

/**
 * A pipeline injector for Netty connections. As most servers use Netty, this avoids code duplication.
 */
@RequiredArgsConstructor
public abstract class NettyPipelineInjector extends PipelineInjector {

    //handler to inject before
    private final String injectPosition;

    @Getter private final Function<TabPlayer, ChannelDuplexHandler> channelFunction = TabChannelDuplexHandler::new;

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
            //java.util.NoSuchElementException: TAB
        }
    }

    /**
     * Returns player with matching game profile name. This is different from
     * real name when a nick plugin changing names of players is used. If no
     * player was found, returns {@code null}.
     *
     * @param   name
     *          Game profile name
     * @return  Player with matching game profile name
     */
    public TabPlayer getPlayer(String name) {
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            if (p.getNickname().equals(name)) return p;
        }
        return null;
    }

    public abstract void onDisplayObjective(TabPlayer player, Object packet) throws IllegalAccessException;

    public abstract void onObjective(TabPlayer player, Object packet) throws IllegalAccessException;

    public abstract boolean isDisplayObjective(Object packet);

    public abstract boolean isObjective(Object packet);

    public abstract boolean isTeam(Object packet);

    public abstract boolean isPlayerInfo(Object packet);

    public abstract boolean isLogin(Object packet);

    public abstract void onPlayerInfo(TabPlayer receiver, Object packet) throws ReflectiveOperationException;

    /**
     * Removes all real players from team if packet does not come from TAB and reports this to override log
     *
     * @param   teamPacket
     *          team packet
     * @throws  ReflectiveOperationException
     *          if throws by reflective operation
     */
    public abstract void modifyPlayers(Object teamPacket) throws ReflectiveOperationException;

    @RequiredArgsConstructor
    public class TabChannelDuplexHandler extends ChannelDuplexHandler {

        /** Injected player */
        protected final TabPlayer player;

        @Override
        public void channelRead(@NotNull ChannelHandlerContext context, @NotNull Object packet) {
            try {
                if (TAB.getInstance().getFeatureManager().onPacketReceive(player, packet)) return;
                super.channelRead(context, packet);
            } catch (Throwable e) {
                TAB.getInstance().getErrorManager().printError("An error occurred when reading packets", e);
            }
        }

        @Override
        public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) {
            try {
                if (isLogin(packet)) {
                    //making sure to not send own packets before login packet is actually sent
                    super.write(context, packet, channelPromise);
                    TAB.getInstance().getFeatureManager().onLoginPacket(player);
                    return;
                }
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
