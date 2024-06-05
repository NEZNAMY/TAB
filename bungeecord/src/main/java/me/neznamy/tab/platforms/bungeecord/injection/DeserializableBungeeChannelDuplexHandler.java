package me.neznamy.tab.platforms.bungeecord.injection;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bungeecord.BungeeTabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.packet.ScoreboardDisplay;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * Channel duplex handler override if features using packets that must be
 * deserialized manually are used. If they are disabled, deserialization is
 * disabled for better performance.
 */
@SuppressWarnings("unchecked")
public class DeserializableBungeeChannelDuplexHandler extends BungeeChannelDuplexHandler {

    /** Inaccessible bungee internals */
    @Nullable
    private static Object directionData;

    @Nullable
    private static Method getId;

    static {
        try {
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
        int marker = buf.readerIndex();
        try {
            int packetId = buf.readByte();
            for (int i=0; i<extraPacketClasses.length; i++) {
                ChannelWrapper ch = ((UserConnection) ((BungeeTabPlayer) player).getPlayer()).getCh();
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