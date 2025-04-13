package me.neznamy.tab.platforms.bukkit.provider.viaversion;

import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.Protocol1_21To1_21_2;
import lombok.NonNull;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.UUID;

/**
 * TabList handler using ViaVersion packets,
 * to unlock +1.21.2 player features on pre 1.21.2 servers.
 */
public class ViaTabList1212 extends ViaTabList1193 {

    private static final BitSet ADD_PLAYER = bitSet(7, 0, 7);
    //private static final BitSet INITIALIZE_CHAT = bitSet(7, 1);
    private static final BitSet UPDATE_GAME_MODE = bitSet(7, 2);
    private static final BitSet UPDATE_LISTED = bitSet(7, 3);
    private static final BitSet UPDATE_LATENCY = bitSet(7, 4);
    private static final BitSet UPDATE_DISPLAY_NAME = bitSet(7, 5);
    private static final BitSet UPDATE_LIST_ORDER = bitSet(7, 6);

    /**
     * Constructs new instance with given parameters.
     *
     * @param   player
     *          Player this tablist will belong to
     */
    public ViaTabList1212(@NotNull BukkitTabPlayer player) {
        super(player, Protocol1_21To1_21_2.class, ClientboundPackets1_21_2.PLAYER_INFO_REMOVE, ClientboundPackets1_21_2.PLAYER_INFO_UPDATE, ClientboundPackets1_21_2.TAB_LIST);
    }

    protected ViaTabList1212(@NonNull BukkitTabPlayer player, @NonNull Class<? extends Protocol> protocol, @Nullable PacketType playerInfoRemove, @NonNull PacketType playerInfoUpdate, @NonNull PacketType tabList) {
        super(player, protocol, playerInfoRemove, playerInfoUpdate, tabList);
    }

    @Override
    public void updateListOrder(@NonNull UUID entry, int listOrder) {
        sendInfoUpdate(Action.UPDATE_LIST_ORDER, entry, listOrder);
    }

    @Override
    protected void writeComponent(@NonNull PacketWrapper packet, @NonNull TabComponent component) {
        packet.write(Types.TAG, component.toViaVersionTag());
    }

    @Override
    protected void writeOptionalComponent(@NonNull PacketWrapper packet, @Nullable TabComponent component) {
        packet.write(Types.OPTIONAL_TAG, component == null ? null : component.toViaVersionTag());
    }

    @Override
    protected void writeAction(@NonNull PacketWrapper packet, @NonNull Action action) {
        switch (action) {
            case ADD_PLAYER:
                packet.write(Types.PROFILE_ACTIONS_ENUM1_21_2, ADD_PLAYER);
                break;
            case UPDATE_DISPLAY_NAME:
                packet.write(Types.PROFILE_ACTIONS_ENUM1_21_2, UPDATE_DISPLAY_NAME);
                break;
            case UPDATE_LATENCY:
                packet.write(Types.PROFILE_ACTIONS_ENUM1_21_2, UPDATE_LATENCY);
                break;
            case UPDATE_GAME_MODE:
                packet.write(Types.PROFILE_ACTIONS_ENUM1_21_2, UPDATE_GAME_MODE);
                break;
            case UPDATE_LISTED:
                packet.write(Types.PROFILE_ACTIONS_ENUM1_21_2, UPDATE_LISTED);
                break;
            case UPDATE_LIST_ORDER:
                packet.write(Types.PROFILE_ACTIONS_ENUM1_21_2, UPDATE_LIST_ORDER);
                break;
            default:
                throw new IllegalStateException("Cannot write " + action.name() + " for 1.21.2 packet");
        }
    }

    @Override
    protected void writeEntry(@NonNull PacketWrapper packet, @NonNull Entry entry) {
        super.writeEntry(packet, entry);
        packet.write(Types.VAR_INT, entry.getListOrder());
    }
}