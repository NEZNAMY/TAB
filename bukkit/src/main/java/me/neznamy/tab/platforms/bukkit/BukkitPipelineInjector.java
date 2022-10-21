package me.neznamy.tab.platforms.bukkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PipelineInjector;
import me.neznamy.tab.shared.features.sorting.Sorting;
import org.jetbrains.annotations.NotNull;

/**
 * Pipeline injection for bukkit
 */
public class BukkitPipelineInjector extends PipelineInjector {

    /** NMS data storage */
    private final NMSStorage nms = NMSStorage.getInstance();

    /**
     * Constructs new instance
     */
    public BukkitPipelineInjector(){
        super("packet_handler");
    }

    @Override
    public Function<TabPlayer, ChannelDuplexHandler> getChannelFunction() {
        return BukkitChannelDuplexHandler::new;
    }

    /**
     * Custom channel duplex handler override
     */
    public class BukkitChannelDuplexHandler extends ChannelDuplexHandler {

        /** Injected player */
        private final TabPlayer player;

        /**
         * Constructs new instance with given player
         *
         * @param   player
         *          player to inject
         */
        public BukkitChannelDuplexHandler(TabPlayer player) {
            this.player = player;
        }

        @Override
        public void channelRead(@NotNull ChannelHandlerContext context, @NotNull Object packet) {
            try {
                if (TAB.getInstance().getFeatureManager().onPacketReceive(player, packet)) return;
                super.channelRead(context, packet);
            } catch (Exception e){
                TAB.getInstance().getErrorManager().printError("An error occurred when reading packets", e);
            }
        }

        @Override
        public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) {
            try {
                if (nms.PacketPlayOutPlayerInfo.isInstance(packet)) {
                    super.write(context, TAB.getInstance().getFeatureManager().onPacketPlayOutPlayerInfo(player, packet), channelPromise);
                    return;
                }
                if (antiOverrideTeams && nms.PacketPlayOutScoreboardTeam != null && nms.PacketPlayOutScoreboardTeam.isInstance(packet)) {
                    long time = System.nanoTime();
                    modifyPlayers(packet);
                    TAB.getInstance().getCPUManager().addTime("NameTags", TabConstants.CpuUsageCategory.ANTI_OVERRIDE, System.nanoTime()-time);
                    super.write(context, packet, channelPromise);
                    return;
                }
                if (nms.PacketPlayOutScoreboardDisplayObjective.isInstance(packet)){
                    TAB.getInstance().getFeatureManager().onDisplayObjective(player, packet);
                }
                if (nms.PacketPlayOutScoreboardObjective.isInstance(packet)) {
                    TAB.getInstance().getFeatureManager().onObjective(player, packet);
                }
                TAB.getInstance().getFeatureManager().onPacketSend(player, packet);
            } catch (Exception e){
                TAB.getInstance().getErrorManager().printError("An error occurred when reading packets", e);
            }
            try {
                super.write(context, packet, channelPromise);
            } catch (Exception e) {
                TAB.getInstance().getErrorManager().printError("Failed to forward packet " + packet.getClass().getSimpleName() + " to " + player.getName(), e);
            }
        }

        /**
         * Removes all real players from team if packet does not come from TAB and reports this to override log
         *
         * @param   packetPlayOutScoreboardTeam
         *          team packet
         * @throws  ReflectiveOperationException
         *          nmsGameMode
         */
        @SuppressWarnings("unchecked")
        private void modifyPlayers(Object packetPlayOutScoreboardTeam) throws ReflectiveOperationException {
            int action = nms.PacketPlayOutScoreboardTeam_ACTION.getInt(packetPlayOutScoreboardTeam);
            if (action == 1 || action == 2 || action == 4) return;
            Collection<String> players = (Collection<String>) nms.PacketPlayOutScoreboardTeam_PLAYERS.get(packetPlayOutScoreboardTeam);
            String teamName = (String) nms.PacketPlayOutScoreboardTeam_NAME.get(packetPlayOutScoreboardTeam);
            if (players == null) return;
            //creating a new list to prevent NoSuchFieldException in minecraft packet encoder when a player is removed
            Collection<String> newList = new ArrayList<>();
            for (String entry : players) {
                TabPlayer p = getPlayer(entry);
                if (p == null) {
                    newList.add(entry);
                    continue;
                }
                Sorting sorting = (Sorting) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SORTING);
                if (!((TabFeature)TAB.getInstance().getTeamManager()).isDisabledPlayer(p) &&
                        !TAB.getInstance().getTeamManager().hasTeamHandlingPaused(p) && !teamName.equals(sorting.getShortTeamName(p))) {
                    logTeamOverride(teamName, p.getName(), sorting.getShortTeamName(p));
                } else {
                    newList.add(entry);
                }
            }
            nms.setField(packetPlayOutScoreboardTeam, nms.PacketPlayOutScoreboardTeam_PLAYERS, newList);
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
        private TabPlayer getPlayer(String name) {
            for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
                if (p.getNickname().equals(name)) return p;
            }
            return null;
        }
    }
}