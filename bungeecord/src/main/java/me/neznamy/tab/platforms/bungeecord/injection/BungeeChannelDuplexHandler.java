package me.neznamy.tab.platforms.bungeecord.injection;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.cpu.CpuManager;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector.TabChannelDuplexHandler;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.SafeBossBar;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import net.md_5.bungee.protocol.packet.Login;
import org.jetbrains.annotations.NotNull;

/**
 * Channel duplex handler for BungeeCord, which in addition checks for Login packet.
 */
public class BungeeChannelDuplexHandler extends TabChannelDuplexHandler {

    /**
     * Constructs new instance with given player
     *
     * @param   player
     *          player to inject
     */
    public BungeeChannelDuplexHandler(TabPlayer player) {
        super(player);
    }

    @Override
    public void write(@NotNull ChannelHandlerContext context, @NotNull Object packet, @NotNull ChannelPromise channelPromise) {
        if (packet instanceof Login) {
            ((SafeScoreboard<?>)player.getScoreboard()).setFrozen(true);
            CpuManager cpu = TAB.getInstance().getCpu();
            cpu.getProcessingThread().executeLater(new TimedCaughtTask(cpu, () -> {
                ((SafeScoreboard<?>)player.getScoreboard()).setFrozen(false);
                player.getScoreboard().resend();
                if (player.getVersionId() >= ProtocolVersion.V1_20_2.getNetworkId()) {
                    // For 1.20.2+ we need to do this, because server switch event is called before tablist is cleared
                    TAB.getInstance().getFeatureManager().onTabListClear(player);
                    ((SafeBossBar<?>)player.getBossBar()).unfreezeAndResend();
                }
            }, "Pipeline injection", TabConstants.CpuUsageCategory.PACKET_LOGIN), 200);
        }
        super.write(context, packet, channelPromise);
    }
}