package me.neznamy.tab.platforms.bukkit;

import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutPlayerInfoStorage;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutScoreboardDisplayObjectiveStorage;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutScoreboardObjectiveStorage;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.PacketPlayOutScoreboardTeamStorage;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector;
import me.neznamy.tab.shared.features.sorting.Sorting;
import org.jetbrains.annotations.NotNull;

/**
 * Pipeline injection for bukkit
 */
public class BukkitPipelineInjector extends NettyPipelineInjector {

    /** NMS data storage */
    private final NMSStorage nms = NMSStorage.getInstance();

    @Getter private final Function<TabPlayer, ChannelDuplexHandler> channelFunction = BukkitChannelDuplexHandler::new;

    /**
     * Constructs new instance
     */
    public BukkitPipelineInjector() {
        super("packet_handler");
    }

    @Override
    protected Channel getChannel(TabPlayer player) {
        final BukkitTabPlayer bukkit = (BukkitTabPlayer) player;
        try {
            if (nms.CHANNEL != null) return (Channel) nms.CHANNEL.get(nms.NETWORK_MANAGER.get(bukkit.getPlayerConnection()));
        } catch (final IllegalAccessException exception) {
            TAB.getInstance().getErrorManager().printError("Failed to get channel of " + bukkit.getName(), exception);
        }
        return null;
    }

    /**
     * Custom channel duplex handler override
     */
    @AllArgsConstructor
    public class BukkitChannelDuplexHandler extends ChannelDuplexHandler {

        /** Injected player */
        private final TabPlayer player;

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
                if (PacketPlayOutPlayerInfoStorage.CLASS.isInstance(packet) ||
                   (PacketPlayOutPlayerInfoStorage.ClientboundPlayerInfoRemovePacket != null &&
                   PacketPlayOutPlayerInfoStorage.ClientboundPlayerInfoRemovePacket.isInstance(packet))) {
                    super.write(context, TAB.getInstance().getFeatureManager().onPacketPlayOutPlayerInfo(player, packet), channelPromise);
                    return;
                }
                if (antiOverrideTeams && PacketPlayOutScoreboardTeamStorage.CLASS.isInstance(packet)) {
                    long time = System.nanoTime();
                    modifyPlayers(packet);
                    TAB.getInstance().getCPUManager().addTime("NameTags", TabConstants.CpuUsageCategory.ANTI_OVERRIDE, System.nanoTime()-time);
                    super.write(context, packet, channelPromise);
                    return;
                }
                if (PacketPlayOutScoreboardDisplayObjectiveStorage.CLASS.isInstance(packet)) {
                    TAB.getInstance().getFeatureManager().onDisplayObjective(player, packet);
                }
                if (PacketPlayOutScoreboardObjectiveStorage.CLASS.isInstance(packet)) {
                    TAB.getInstance().getFeatureManager().onObjective(player, packet);
                }
                TAB.getInstance().getFeatureManager().onPacketSend(player, packet);
            } catch (Throwable e) {
                TAB.getInstance().getErrorManager().printError("An error occurred when reading packets", e);
            }
            try {
                super.write(context, packet, channelPromise);
            } catch (Throwable e) {
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
            int action = PacketPlayOutScoreboardTeamStorage.ACTION.getInt(packetPlayOutScoreboardTeam);
            if (action == 1 || action == 2 || action == 4) return;
            Collection<String> players = (Collection<String>) PacketPlayOutScoreboardTeamStorage.PLAYERS.get(packetPlayOutScoreboardTeam);
            String teamName = (String) PacketPlayOutScoreboardTeamStorage.NAME.get(packetPlayOutScoreboardTeam);
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
            PacketPlayOutScoreboardTeamStorage.PLAYERS.set(packetPlayOutScoreboardTeam, newList);
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