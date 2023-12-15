package me.neznamy.tab.platforms.bungeecord;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.lang.reflect.Field;

import lombok.SneakyThrows;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.redis.feature.RedisTeams;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.packet.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Pipeline injection to secure proper functionality
 * of some features by preventing other plugins
 * from overriding it.
 */
@SuppressWarnings("unchecked")
public class BungeePipelineInjector extends NettyPipelineInjector {

    /** Inaccessible bungee internals */
    @Nullable
    private static Field wrapperField;

    @Nullable
    private static Object directionData;

    @Nullable
    private static Method getId;

    static {
        try {
            (wrapperField = InitialHandler.class.getDeclaredField("ch")).setAccessible(true);
            directionData = ReflectionUtils.setAccessible(Protocol.class.getDeclaredField("TO_CLIENT")).get(Protocol.GAME);
            getId = ReflectionUtils.setAccessible(Protocol.DirectionData.class.getDeclaredMethod("getId", Class.class, int.class));
        } catch (ReflectiveOperationException exception) {
            TAB.getInstance().getErrorManager().criticalError("Failed to initialize bungee internal fields", exception);
        }
    }

    /** Packets used by the plugin that must be deserialized and BungeeCord does not do it automatically */
    @NotNull
    private final Class<? extends DefinedPacket>[] extraPacketClasses = new Class[]{Team.class, ScoreboardDisplay.class, ScoreboardObjective.class};

    @NotNull
    private final Supplier<DefinedPacket>[] extraPacketSuppliers = new Supplier[]{Team::new, ScoreboardDisplay::new, ScoreboardObjective::new};

    @Override
    @NotNull
    public Function<TabPlayer, ChannelDuplexHandler> getChannelFunction() {
        return byteBufDeserialization ? DeserializableBungeeChannelDuplexHandler::new : TabChannelDuplexHandler::new;
    }

    @Override
    @Nullable
    @SneakyThrows
    protected Channel getChannel(@NotNull TabPlayer player) {
        if (wrapperField == null) return null;
        return ((ChannelWrapper) wrapperField.get(((BungeeTabPlayer) player).getPlayer().getPendingConnection())).getHandle();
    }

    @Override
    public void onDisplayObjective(@NotNull TabPlayer player, @NotNull Object packet) {
        TAB.getInstance().getFeatureManager().onDisplayObjective(player,
                ((ScoreboardDisplay)packet).getPosition(), ((ScoreboardDisplay) packet).getName());
    }

    @Override
    public void onObjective(@NotNull TabPlayer player, @NotNull Object packet) {
        TAB.getInstance().getFeatureManager().onObjective(player,
                ((ScoreboardObjective) packet).getAction(), ((ScoreboardObjective) packet).getName());
    }

    @Override
    public boolean isDisplayObjective(@NotNull Object packet) {
        return packet instanceof ScoreboardDisplay;
    }

    @Override
    public boolean isObjective(@NotNull Object packet) {
        return packet instanceof ScoreboardObjective;
    }

    @Override
    public boolean isTeam(@NotNull Object packet) {
        return packet instanceof Team;
    }

    @Override
    public void modifyPlayers(@NotNull Object team) {
        if (TAB.getInstance().getNameTagManager() == null) return;
        Team packet = (Team) team;
        if (packet.getMode() == 1 || packet.getMode() == 2 || packet.getMode() == 4) return;
        Collection<String> col = Lists.newArrayList(packet.getPlayers());
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            Sorting sorting = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SORTING);
            String expectedTeam = sorting.getShortTeamName(p);
            if (expectedTeam != null && (col.contains(p.getNickname()) || col.contains(p.getName())) &&
                    !((NameTag)TAB.getInstance().getNameTagManager()).getDisableChecker().isDisabledPlayer(p) &&
                    !TAB.getInstance().getNameTagManager().hasTeamHandlingPaused(p) && !packet.getName().equals(expectedTeam)) {
                logTeamOverride(packet.getName(), p.getName(), expectedTeam);
                col.remove(p.getNickname());
                col.remove(p.getName());
            }
        }
        RedisSupport redis = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
        if (redis != null) {
            RedisTeams teams = redis.getRedisTeams();
            if (teams != null) {
                for (RedisPlayer p : redis.getRedisPlayers().values()) {
                    if (col.contains(p.getNickname()) && !packet.getName().equals(teams.getTeamNames().get(p))) {
                        logTeamOverride(packet.getName(), p.getNickname(), teams.getTeamNames().get(p));
                        col.remove(p.getNickname());
                    }
                }
            }
        }
        packet.setPlayers(col.toArray(new String[0]));
    }

    @Override
    public boolean isLogin(@NotNull Object packet) {
        return packet instanceof Login;
    }

    /**
     * Constructs new instance of the feature
     */
    public BungeePipelineInjector() {
        super("inbound-boss");
    }

    /**
     * Channel duplex handler override if features using packets that must be
     * deserialized manually are used. If they are disabled, deserialization is
     * disabled for better performance.
     */
    public class DeserializableBungeeChannelDuplexHandler extends TabChannelDuplexHandler {

        /**
         * Constructs new instance with given player
         *
         * @param   player
         *          player to inject
         */
        public DeserializableBungeeChannelDuplexHandler(@NotNull TabPlayer player) {
            super(player);
        }

        @Override
        public void write(@NotNull ChannelHandlerContext context, @NotNull Object packet, @NotNull ChannelPromise channelPromise) {
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
        @NotNull
        private Object deserialize(@NotNull ByteBuf buf) {
            if (wrapperField == null) return buf;
            int marker = buf.readerIndex();
            try {
                int packetId = buf.readByte();
                for (int i=0; i<extraPacketClasses.length; i++) {
                    ChannelWrapper ch = (ChannelWrapper) wrapperField.get(((BungeeTabPlayer) player).getPlayer().getPendingConnection());
                    if (!ch.getEncodeProtocol().TO_CLIENT.hasPacket(extraPacketClasses[i], ((ProxiedPlayer)player.getPlayer()).getPendingConnection().getVersion())) {
                        continue;
                    }
                    if (packetId == getPacketId(((BungeeTabPlayer)player).getPlayer().getPendingConnection().getVersion(), extraPacketClasses[i])) {
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

        /**
         * Returns packet ID of specified packet on the protocol version
         *
         * @param   protocolVersion
         *          Protocol version to get packet id for
         * @param   clazz
         *          packet class
         * @return  packet ID
         */
        @SneakyThrows
        private int getPacketId(int protocolVersion, @NotNull Class<? extends DefinedPacket> clazz) {
            if (getId == null) return -1;
            return (int) getId.invoke(directionData, clazz, protocolVersion);
        }
    }
}