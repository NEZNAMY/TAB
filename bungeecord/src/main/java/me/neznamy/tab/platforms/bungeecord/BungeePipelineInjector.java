package me.neznamy.tab.platforms.bungeecord;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.lang.reflect.Field;
import me.neznamy.tab.api.feature.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.shared.features.injection.NettyPipelineInjector;
import me.neznamy.tab.shared.features.redis.RedisPlayer;
import me.neznamy.tab.shared.features.sorting.Sorting;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.*;

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

    private static Field wrapperField;

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
    protected Channel getChannel(TabPlayer player) {
        final BungeeTabPlayer bungee = (BungeeTabPlayer) player;
        try {
            return ((ChannelWrapper) wrapperField.get(bungee.getPlayer().getPendingConnection())).getHandle();
        } catch (final IllegalAccessException exception) {
            TAB.getInstance().getErrorManager().criticalError("Failed to get channel of " + bungee.getPlayer().getName(), exception);
        }
        return null;
    }

    @Override
    public void onDisplayObjective(TabPlayer player, Object packet) {
        TAB.getInstance().getFeatureManager().onDisplayObjective(player,
                ((ScoreboardDisplay) packet).getPosition(), ((ScoreboardDisplay) packet).getName());
    }

    @Override
    public void onObjective(TabPlayer player, Object packet) {
        TAB.getInstance().getFeatureManager().onObjective(player,
                ((ScoreboardObjective) packet).getAction(), ((ScoreboardObjective) packet).getName());
    }

    @Override
    public boolean isDisplayObjective(Object packet) {
        return packet instanceof ScoreboardDisplay;
    }

    @Override
    public boolean isObjective(Object packet) {
        return packet instanceof ScoreboardObjective;
    }

    @Override
    public boolean isTeam(Object packet) {
        return packet instanceof Team;
    }

    @Override
    public boolean isPlayerInfo(Object packet) {
        return packet instanceof PlayerListItem || packet instanceof PlayerListItemUpdate;
    }

    @Override
    public void modifyPlayers(Object team) {
        Team packet = (Team) team;
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
                if (col.contains(p.getNickname()) && !packet.getName().equals(p.getTeamName())) {
                    logTeamOverride(packet.getName(), p.getName(), p.getTeamName());
                    col.remove(p.getNickname());
                }
            }
        }
        packet.setPlayers(col.toArray(new String[0]));
    }

    @Override
    public boolean isLogin(Object packet) {
        return packet instanceof Login;
    }

    @Override
    public void onPlayerInfo(TabPlayer receiver, Object packet) {
        PlayerListItem.Item[] items;
        if (packet instanceof PlayerListItemUpdate) {
            PlayerListItemUpdate update = (PlayerListItemUpdate) packet;
            for (PlayerListItem.Item item : update.getItems()) {
                if (update.getActions().contains(PlayerListItemUpdate.Action.UPDATE_GAMEMODE)) {
                    item.setGamemode(TAB.getInstance().getFeatureManager().onGameModeChange(receiver, item.getUuid(), item.getGamemode()));
                }
                if (update.getActions().contains(PlayerListItemUpdate.Action.UPDATE_LATENCY)) {
                    item.setPing(TAB.getInstance().getFeatureManager().onLatencyChange(receiver, item.getUuid(), item.getPing()));
                }
                if (update.getActions().contains(PlayerListItemUpdate.Action.UPDATE_DISPLAY_NAME)) {
                    IChatBaseComponent displayName = IChatBaseComponent.deserialize(item.getDisplayName());
                    displayName = TAB.getInstance().getFeatureManager().onDisplayNameChange(receiver, item.getUuid(), displayName);
                    item.setDisplayName(displayName == null ? null : displayName.toString(receiver.getVersion()));
                }
                if (update.getActions().contains(PlayerListItemUpdate.Action.ADD_PLAYER)) {
                    TAB.getInstance().getFeatureManager().onEntryAdd(receiver, item.getUuid(), item.getUsername());
                }
            }
        } else {
            PlayerListItem listItem = (PlayerListItem) packet;
            for (PlayerListItem.Item item : listItem.getItems()) {
                if (listItem.getAction() == PlayerListItem.Action.UPDATE_GAMEMODE || listItem.getAction() == PlayerListItem.Action.ADD_PLAYER) {
                    item.setGamemode(TAB.getInstance().getFeatureManager().onGameModeChange(receiver, item.getUuid(), item.getGamemode()));
                }
                if (listItem.getAction() == PlayerListItem.Action.UPDATE_LATENCY || listItem.getAction() == PlayerListItem.Action.ADD_PLAYER) {
                    item.setPing(TAB.getInstance().getFeatureManager().onLatencyChange(receiver, item.getUuid(), item.getPing()));
                }
                if (listItem.getAction() == PlayerListItem.Action.UPDATE_DISPLAY_NAME || listItem.getAction() == PlayerListItem.Action.ADD_PLAYER) {
                    IChatBaseComponent displayName = IChatBaseComponent.deserialize(item.getDisplayName());
                    displayName = TAB.getInstance().getFeatureManager().onDisplayNameChange(receiver, item.getUuid(), displayName);
                    item.setDisplayName(displayName == null ? null : displayName.toString(receiver.getVersion()));
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