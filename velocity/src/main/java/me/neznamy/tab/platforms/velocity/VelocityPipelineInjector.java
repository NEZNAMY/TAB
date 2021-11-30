package me.neznamy.tab.platforms.velocity;

import com.google.common.collect.Lists;
import com.velocitypowered.proxy.protocol.packet.ScoreboardTeam;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.NickCompatibility;
import me.neznamy.tab.shared.features.PipelineInjector;

import java.util.List;

public class VelocityPipelineInjector extends PipelineInjector {

    /**
     * Constructs new instance of the feature
     */
    public VelocityPipelineInjector() {
        super("handler");
        channelFunction = VelocityChannelDuplexHandler::new;
    }

    /**
     * Custom channel duplex handler override
     */
    public class VelocityChannelDuplexHandler extends ChannelDuplexHandler {

        //injected player
        private TabPlayer player;

        /**
         * Constructs new instance with given player
         * @param player - player to inject
         */
        public VelocityChannelDuplexHandler(TabPlayer player) {
            this.player = player;
        }

        @Override
        public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) {
            try {
                switch(packet.getClass().getSimpleName()) {
                    //case "PlayerListItem":
                    //    super.write(context, TAB.getInstance().getFeatureManager().onPacketPlayOutPlayerInfo(player, packet), channelPromise);
                    //    return;
                    case "ScoreboardTeam":
                        if (antiOverrideTeams) {
                            modifyPlayers((ScoreboardTeam) packet);
                        }
                        break;
                    case "ScoreboardDisplay":
                        if (TAB.getInstance().getFeatureManager().onDisplayObjective(player, packet)) {
                            return;
                        }
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
        private void modifyPlayers(ScoreboardTeam packet){
            long time = System.nanoTime();
            if (packet.getPlayers() == null) return;
            List<String> col = Lists.newArrayList(packet.getPlayers());
            for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
                if (col.contains(getName(p)) && !((TabFeature)TAB.getInstance().getTeamManager()).isDisabledPlayer(p) &&
                        !TAB.getInstance().getTeamManager().hasTeamHandlingPaused(p) && !packet.getName().equals(p.getTeamName())) {
                    logTeamOverride(packet.getName(), p.getName(), p.getTeamName());
                    col.remove(getName(p));
                }
            }
            packet.setPlayers(col);
            TAB.getInstance().getCPUManager().addTime("Nametags", TabConstants.CpuUsageCategory.ANTI_OVERRIDE, System.nanoTime()-time);
        }

        private String getName(TabPlayer p) {
            NickCompatibility nick = (NickCompatibility) TAB.getInstance().getFeatureManager().getFeature("nick");
            if (nick != null) {
                return nick.getNickname(p);
            }
            return p.getName();
        }
    }
}
