package me.neznamy.tab.platforms.bukkit.provider.viaversion;

import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.Protocol1_15_2To1_16;
import com.viaversion.viaversion.protocols.v1_15_2to1_16.packet.ClientboundPackets1_16;
import lombok.NonNull;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * TabList handler using ViaVersion packets,
 * to unlock +1.16 player features on pre 1.16 servers.
 */
public class ViaTabList116 extends ViaTabList {

    private static final int ADD_PLAYER = 0;
    private static final int UPDATE_GAME_MODE = 1;
    private static final int UPDATE_LATENCY = 2;
    private static final int UPDATE_DISPLAY_NAME = 3;
    private static final int REMOVE_PLAYER = 4;

    /**
     * Constructs new instance with given parameters.
     *
     * @param   player
     *          Player this tablist will belong to
     */
    public ViaTabList116(@NotNull BukkitTabPlayer player) {
        this(player, Protocol1_15_2To1_16.class, ClientboundPackets1_16.PLAYER_INFO, ClientboundPackets1_16.TAB_LIST);
    }

    protected ViaTabList116(@NonNull BukkitTabPlayer player, @NonNull Class<? extends Protocol> protocol, @NonNull PacketType playerInfoUpdate, @NonNull PacketType tabList) {
        super(player, protocol, playerInfoUpdate, tabList);
    }

    @Override
    public void removeEntry(@NonNull UUID entry) {
        sendInfoUpdate(Action.REMOVE_PLAYER, entry, null);
    }

    @Override
    public void updateListed(@NonNull UUID entry, boolean listed) {
        // Added on 1.19.3
    }

    @Override
    public void updateListOrder(@NonNull UUID entry, int listOrder) {
        // Added in 1.21.2
    }

    @Override
    public void updateHat(@NonNull UUID entry, boolean showHat) {
        // Added in 1.21.4
    }

    @Override
    protected void writeComponent(@NonNull PacketWrapper packet, @NonNull TabComponent component) {
        packet.write(Types.COMPONENT, component.toViaVersion());
    }

    @Override
    protected void writeOptionalComponent(@NonNull PacketWrapper packet, @Nullable TabComponent component) {
        packet.write(Types.OPTIONAL_COMPONENT, component == null ? null : component.toViaVersion());
    }

    @Override
    protected void writeAction(@NonNull PacketWrapper packet, @NonNull Action action) {
        switch (action) {
            case ADD_PLAYER:
                packet.write(Types.VAR_INT, ADD_PLAYER);
                break;
            case REMOVE_PLAYER:
                packet.write(Types.VAR_INT, REMOVE_PLAYER);
                break;
            case UPDATE_DISPLAY_NAME:
                packet.write(Types.VAR_INT, UPDATE_DISPLAY_NAME);
                break;
            case UPDATE_LATENCY:
                packet.write(Types.VAR_INT, UPDATE_LATENCY);
                break;
            case UPDATE_GAME_MODE:
                packet.write(Types.VAR_INT, UPDATE_GAME_MODE);
                break;
            default:
                throw new IllegalStateException("Cannot write " + action.name() + " for 1.16 packet");
        }
    }

    @Override
    protected void writeEntry(@NonNull PacketWrapper packet, @NonNull Entry entry) {
        packet.write(Types.STRING, entry.getName());
        if (entry.getSkin() == null) {
            // Properties size
            packet.write(Types.VAR_INT, 0);
        } else {
            // Properties size
            packet.write(Types.VAR_INT, 1);
            // Properties
            packet.write(Types.STRING, entry.getName());
            packet.write(Types.STRING, entry.getSkin().getValue());
            packet.write(Types.OPTIONAL_STRING, entry.getSkin().getSignature());
        }
        packet.write(Types.VAR_INT, entry.getGameMode());
        packet.write(Types.VAR_INT, entry.getLatency());
        writeOptionalComponent(packet, entry.getDisplayName());
    }
}
