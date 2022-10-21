package me.neznamy.tab.platforms.bungeecord;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.shared.features.PipelineInjector;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.sorting.Sorting;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.ScoreboardDisplay;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.Team;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Pipeline injection to secure proper functionality
 * of some features by preventing other plugins
 * from overriding it.
 */
@SuppressWarnings("unchecked")
public class BungeePipelineInjector extends PipelineInjector {

    /** Packets used by the plugin that must be deserialized and BungeeCord does not do it automatically */
    private final Class<? extends DefinedPacket>[] extraPacketClasses = new Class[]{Team.class, ScoreboardDisplay.class, ScoreboardObjective.class};
    private final Supplier<DefinedPacket>[] extraPacketSuppliers = new Supplier[]{Team::new, ScoreboardDisplay::new, ScoreboardObjective::new};

    /**
     * Constructs new instance of the feature
     */
    public BungeePipelineInjector() {
        super("inbound-boss");
    }

    @Override
    public Function<TabPlayer, ChannelDuplexHandler> getChannelFunction() {
        return byteBufDeserialization ? DeserializableBungeeChannelDuplexHandler::new : BungeeChannelDuplexHandler::new;
    }

    /**
     * Custom channel duplex handler override
     */
    public class BungeeChannelDuplexHandler extends ChannelDuplexHandler {

        /** Injected player */
        protected final TabPlayer player;

        /**
         * Constructs new instance with given player
         *
         * @param   player
         *          player to inject
         */
        public BungeeChannelDuplexHandler(TabPlayer player) {
            this.player = player;
        }

        @Override
        public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) {
            try {
                switch(packet.getClass().getSimpleName()) {
                case "PlayerListItem":
                    super.write(context, TAB.getInstance().getFeatureManager().onPacketPlayOutPlayerInfo(player, packet), channelPromise);
                    return;
                case "Team":
                    if (antiOverrideTeams) {
                        long time = System.nanoTime();
                        modifyPlayers((Team) packet);
                        TAB.getInstance().getCPUManager().addTime("NameTags", TabConstants.CpuUsageCategory.ANTI_OVERRIDE, System.nanoTime()-time);
                    }
                    break;
                case "ScoreboardDisplay":
                    TAB.getInstance().getFeatureManager().onDisplayObjective(player, packet);
                    break;
                case "ScoreboardObjective":
                    TAB.getInstance().getFeatureManager().onObjective(player, packet);
                    break;
                case "Login":
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
                TAB.getInstance().getErrorManager().printError(String.format("Failed to forward packet %s to %s", packet.getClass().getSimpleName(), player.getName()), e);
            }
        }

        /**
         * Removes all real players from packet if the packet doesn't come from TAB
         *
         * @param   packet
         *          packet to modify
         */
        private void modifyPlayers(Team packet){
            if (packet.getMode() == 1 || packet.getMode() == 2 || packet.getMode() == 4) return;
            Collection<String> col = Lists.newArrayList(packet.getPlayers());
            for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
                Sorting sorting = (Sorting) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SORTING);
                if (col.contains(p.getNickname()) && !((TabFeature)TAB.getInstance().getTeamManager()).isDisabledPlayer(p) &&
                        !TAB.getInstance().getTeamManager().hasTeamHandlingPaused(p) && !packet.getName().equals(sorting.getShortTeamName(p))) {
                    logTeamOverride(packet.getName(), p.getName(), sorting.getShortTeamName(p));
                    col.remove(p.getNickname());
                }
            }
            RedisBungeeSupport redis = (RedisBungeeSupport) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
            if (redis != null) {
                for (RedisPlayer p : redis.getRedisPlayers().values()) {
                    if (col.contains(p.getNickName()) && !packet.getName().equals(p.getTeamName())) {
                        logTeamOverride(packet.getName(), p.getName(), p.getTeamName());
                        col.remove(p.getNickName());
                    }
                }
            }
            packet.setPlayers(col.toArray(new String[0]));
        }
    }

    /**
     * Channel duplex handler override if features using packets that must be
     * deserialized manually are used. If they are disabled, deserialization is
     * disabled for better performance.
     */
    public class DeserializableBungeeChannelDuplexHandler extends BungeeChannelDuplexHandler {

        /**
         * Constructs new instance with given player
         *
         * @param   player
         *          player to inject
         */
        public DeserializableBungeeChannelDuplexHandler(TabPlayer player) {
            super(player);
        }

        @Override
        public void write(ChannelHandlerContext context, Object packet, ChannelPromise channelPromise) {
            long time = System.nanoTime();
            Object modifiedPacket = packet instanceof ByteBuf ? deserialize((ByteBuf) packet) : packet;
            TAB.getInstance().getCPUManager().addTime(TabConstants.Feature.PACKET_DESERIALIZING, TabConstants.CpuUsageCategory.BYTE_BUF, System.nanoTime()-time);
            super.write(context, modifiedPacket, channelPromise);
        }

        /**
         * Deserializes byte buf in case it is one of the tracked packets coming from backend server and returns it.
         * If the packet is not one of them, returns input
         *
         * @param   buf
         *          byte buf to deserialize
         * @return  deserialized packet or input byte buf if packet is not tracked
         */
        private Object deserialize(ByteBuf buf) {
            int marker = buf.readerIndex();
            try {
                int packetId = buf.readByte();
                for (int i=0; i<extraPacketClasses.length; i++) {
                    if (packetId == ((BungeeTabPlayer)player).getPacketId(extraPacketClasses[i])) {
                        DefinedPacket packet = extraPacketSuppliers[i].get();
                        packet.read(buf, null, ((ProxiedPlayer)player.getPlayer()).getPendingConnection().getVersion());
                        buf.release();
                        return packet;
                    }
                }
            } catch (Exception e) {
                //rare OverflowPacketException or IndexOutOfBoundsException
            }
            buf.readerIndex(marker);
            return buf;
        }
    }
}