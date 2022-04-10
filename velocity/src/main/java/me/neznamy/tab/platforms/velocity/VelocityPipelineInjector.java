package me.neznamy.tab.platforms.velocity;

import com.google.common.collect.Lists;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.util.Preconditions;
import me.neznamy.tab.platforms.velocity.storage.VelocityPacketStorage;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.PipelineInjector;

import java.util.List;
import java.util.function.Function;

public class VelocityPipelineInjector extends PipelineInjector {

    /** Packet storage */
    private final VelocityPacketStorage vps = VelocityPacketStorage.getInstance();

    /**
     * Constructs new instance of the feature
     */
    public VelocityPipelineInjector() {
        super("handler");
    }

    @Override
    public Function<TabPlayer, ChannelDuplexHandler> getChannelFunction() {
        return VelocityChannelDuplexHandler::new;
    }

    /**
     * Custom channel duplex handler override
     */
    public class VelocityChannelDuplexHandler extends ChannelDuplexHandler {

        /** Injected player */
        protected final TabPlayer player;

        /**
         * Constructs new instance with given player
         *
         * @param   player
         *          player to inject
         */
        public VelocityChannelDuplexHandler(TabPlayer player) {
            this.player = player;
        }

        @Override
        public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) {
            try {
                switch(packet.getClass().getSimpleName()) {
                case "PlayerListItem":
                    super.write(context, TAB.getInstance().getFeatureManager().onPacketPlayOutPlayerInfo(player, packet), channelPromise);
                    return;
                case "ScoreboardTeam":
                    if (antiOverrideTeams) {
                        long time = System.nanoTime();
                        modifyPlayers(packet);
                        TAB.getInstance().getCPUManager().addTime("NameTags", TabConstants.CpuUsageCategory.ANTI_OVERRIDE, System.nanoTime()-time);
                    }
                    break;
                case "ScoreboardDisplay":
                    TAB.getInstance().getFeatureManager().onDisplayObjective(player, packet);
                    break;
                case "ScoreboardObjective":
                    TAB.getInstance().getFeatureManager().onObjective(player, packet);
                    break;
                case "JoinGame":
                    //making sure to not send own packets before login packet is actually sent
                    super.write(context, packet, channelPromise);
                    TAB.getInstance().getFeatureManager().onLoginPacket(player);
                    return;
                default:
                    break;
                }
            } catch (Exception e){
                TAB.getInstance().getErrorManager().printError("An error occurred when analyzing packets for player " + player.getName() + " with client version " + player.getVersion().getFriendlyName(), e);
            }
            try {
                super.write(context, packet, channelPromise);
            } catch (Exception e) {
                TAB.getInstance().getErrorManager().printError("Failed to forward packet " + packet.getClass().getSimpleName() + " to " + player.getName(), e);
            }
        }

        /**
         * Removes all real players from packet if the packet doesn't come from TAB
         * @param packet - packet to modify
         */
        private void modifyPlayers(Object packetScoreboardTeam) throws ReflectiveOperationException {
            final byte mode = (byte) vps.ScoreboardTeam_getMode.invoke(packetScoreboardTeam);
            if (mode == 1 || mode == 2 || mode == 4) return;
            if (vps.ScoreboardTeam_getPlayers.invoke(packetScoreboardTeam) == null) return;
            List<String> col = Lists.newArrayList((List<String>) vps.ScoreboardTeam_getPlayers.invoke(packetScoreboardTeam));
            String name = (String) vps.ScoreboardTeam_getName.invoke(packetScoreboardTeam);
            for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
                if (col.contains(p.getNickname()) && !((TabFeature)TAB.getInstance().getTeamManager()).isDisabledPlayer(p) &&
                        !TAB.getInstance().getTeamManager().hasTeamHandlingPaused(p) && !name.equals(p.getTeamName())) {
                    logTeamOverride(name, p.getName(), p.getTeamName());
                    col.remove(p.getNickname());
                }
            }
            vps.ScoreboardTeam_setPlayers.invoke(packetScoreboardTeam, col);
        }
    }
}
