package me.neznamy.tab.platforms.bungeecord;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.lang.reflect.Field;

import lombok.NonNull;
import me.neznamy.tab.shared.features.redis.RedisSupport;
import me.neznamy.tab.shared.features.redis.feature.RedisTeams;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.sorting.Sorting;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private static @Nullable Field wrapperField;

    static {
        try {
            (wrapperField = InitialHandler.class.getDeclaredField("ch")).setAccessible(true);
        } catch (ReflectiveOperationException exception) {
            TAB.getInstance().getErrorManager().criticalError("Failed to initialize bungee internal fields", exception);
        }
    }

    /** Packets used by the plugin that must be deserialized and BungeeCord does not do it automatically */
    private final Class<? extends DefinedPacket>[] extraPacketClasses = new Class[]{Team.class, ScoreboardDisplay.class, ScoreboardObjective.class};
    private final Supplier<DefinedPacket>[] extraPacketSuppliers = new Supplier[]{Team::new, ScoreboardDisplay::new, ScoreboardObjective::new};

    @Override
    public Function<TabPlayer, ChannelDuplexHandler> getChannelFunction() {
        return byteBufDeserialization ? DeserializableBungeeChannelDuplexHandler::new : TabChannelDuplexHandler::new;
    }

    @Override
    protected @Nullable Channel getChannel(@NonNull TabPlayer player) {
        if (wrapperField == null) return null;
        final BungeeTabPlayer bungee = (BungeeTabPlayer) player;
        try {
            return ((ChannelWrapper) wrapperField.get(bungee.getPlayer().getPendingConnection())).getHandle();
        } catch (final IllegalAccessException exception) {
            TAB.getInstance().getErrorManager().criticalError("Failed to get channel of " + bungee.getPlayer().getName(), exception);
        }
        return null;
    }

    @Override
    public void onDisplayObjective(@NonNull TabPlayer player, @NonNull Object packet) {
        TAB.getInstance().getFeatureManager().onDisplayObjective(player,
                ((ScoreboardDisplay) packet).getPosition(), ((ScoreboardDisplay) packet).getName());
    }

    @Override
    public void onObjective(@NonNull TabPlayer player, @NonNull Object packet) {
        TAB.getInstance().getFeatureManager().onObjective(player,
                ((ScoreboardObjective) packet).getAction(), ((ScoreboardObjective) packet).getName());
    }

    @Override
    public boolean isDisplayObjective(@NonNull Object packet) {
        return packet instanceof ScoreboardDisplay;
    }

    @Override
    public boolean isObjective(@NonNull Object packet) {
        return packet instanceof ScoreboardObjective;
    }

    @Override
    public boolean isTeam(@NonNull Object packet) {
        return packet instanceof Team;
    }

    @Override
    public boolean isPlayerInfo(@NonNull Object packet) {
        return packet instanceof PlayerListItem || packet instanceof PlayerListItemUpdate;
    }

    @Override
    public void modifyPlayers(@NonNull Object team) {
        if (TAB.getInstance().getTeamManager() == null) return;
        Team packet = (Team) team;
        if (packet.getMode() == 1 || packet.getMode() == 2 || packet.getMode() == 4) return;
        Collection<String> col = Lists.newArrayList(packet.getPlayers());
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            Sorting sorting = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.SORTING);
            String expectedTeam = sorting.getShortTeamName(p);
            if (expectedTeam != null && col.contains(p.getNickname()) && !((TabFeature)TAB.getInstance().getTeamManager()).isDisabledPlayer(p) &&
                    !TAB.getInstance().getTeamManager().hasTeamHandlingPaused(p) && !packet.getName().equals(expectedTeam)) {
                logTeamOverride(packet.getName(), p.getName(), expectedTeam);
                col.remove(p.getNickname());
            }
        }
        RedisSupport redis = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.REDIS_BUNGEE);
        if (redis != null) {
            RedisTeams teams = redis.getRedisTeams();
            if (teams != null) {
                for (RedisPlayer p : redis.getRedisPlayers().values()) {
                    if (col.contains(p.getNickname()) && !packet.getName().equals(teams.getTeamNames().get(p))) {
                        logTeamOverride(packet.getName(), p.getName(), teams.getTeamNames().get(p));
                        col.remove(p.getNickname());
                    }
                }
            }
        }
        packet.setPlayers(col.toArray(new String[0]));
    }

    @Override
    public void onPlayerInfo(@NonNull TabPlayer receiver, @NonNull Object packet) {
        if (packet instanceof PlayerListItemUpdate) {
            PlayerListItemUpdate update = (PlayerListItemUpdate) packet;
            for (PlayerListItem.Item item : update.getItems()) {
                if (update.getActions().contains(PlayerListItemUpdate.Action.UPDATE_DISPLAY_NAME)) {
                    IChatBaseComponent newDisplayName = TAB.getInstance().getFeatureManager().onDisplayNameChange(receiver, item.getUuid());
                    if (newDisplayName != null) item.setDisplayName(newDisplayName.toString(receiver.getVersion()));
                }
            }
        } else {
            PlayerListItem listItem = (PlayerListItem) packet;
            for (PlayerListItem.Item item : listItem.getItems()) {
                if (listItem.getAction() == PlayerListItem.Action.UPDATE_DISPLAY_NAME || listItem.getAction() == PlayerListItem.Action.ADD_PLAYER) {
                    IChatBaseComponent newDisplayName = TAB.getInstance().getFeatureManager().onDisplayNameChange(receiver, item.getUuid());
                    if (newDisplayName != null) item.setDisplayName(newDisplayName.toString(receiver.getVersion()));
                }
            }
        }
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
        private @NotNull Object deserialize(@NonNull ByteBuf buf) {
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