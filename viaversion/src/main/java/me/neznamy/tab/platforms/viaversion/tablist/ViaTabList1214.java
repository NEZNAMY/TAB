package me.neznamy.tab.platforms.viaversion.tablist;

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_2to1_21_4.Protocol1_21_2To1_21_4;
import com.viaversion.viaversion.protocols.v1_21to1_21_2.packet.ClientboundPackets1_21_2;
import lombok.NonNull;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.BitSet;
import java.util.UUID;

/**
 * TabList handler using ViaVersion packets,
 * to unlock +1.21.4 player features on pre 1.21.4 servers.
 *
 * @param <P> Platform's TabPlayer class
 */
public class ViaTabList1214<P extends TabPlayer> extends ViaTabList1212<P> {

    private static final BitSet ADD_PLAYER = bitSet(8, 0, 8);
    //private static final BitSet INITIALIZE_CHAT = bitSet(8, 1);
    private static final BitSet UPDATE_GAME_MODE = bitSet(8, 2);
    private static final BitSet UPDATE_LISTED = bitSet(8, 3);
    private static final BitSet UPDATE_LATENCY = bitSet(8, 4);
    private static final BitSet UPDATE_DISPLAY_NAME = bitSet(8, 5);
    private static final BitSet UPDATE_LIST_ORDER = bitSet(8, 6);
    private static final BitSet UPDATE_HAT = bitSet(8, 7);

    /**
     * Constructs new instance with given parameters.
     *
     * @param   player
     *          Player this tablist will belong to
     */
    public ViaTabList1214(@NotNull P player) {
        super(player, Protocol1_21_2To1_21_4.class, ClientboundPackets1_21_2.PLAYER_INFO_REMOVE, ClientboundPackets1_21_2.PLAYER_INFO_UPDATE, ClientboundPackets1_21_2.TAB_LIST);
    }

    @Override
    public void updateHat(@NonNull UUID entry, boolean showHat) {
        sendInfoUpdate(Action.UPDATE_HAT, entry, showHat);
    }

    @Override
    protected void writeAction(@NonNull PacketWrapper packet, @NonNull Action action) {
        switch (action) {
            case ADD_PLAYER:
                packet.write(Types.PROFILE_ACTIONS_ENUM1_21_4, ADD_PLAYER);
                break;
            case UPDATE_DISPLAY_NAME:
                packet.write(Types.PROFILE_ACTIONS_ENUM1_21_4, UPDATE_DISPLAY_NAME);
                break;
            case UPDATE_LATENCY:
                packet.write(Types.PROFILE_ACTIONS_ENUM1_21_4, UPDATE_LATENCY);
                break;
            case UPDATE_GAME_MODE:
                packet.write(Types.PROFILE_ACTIONS_ENUM1_21_4, UPDATE_GAME_MODE);
                break;
            case UPDATE_LISTED:
                packet.write(Types.PROFILE_ACTIONS_ENUM1_21_4, UPDATE_LISTED);
                break;
            case UPDATE_LIST_ORDER:
                packet.write(Types.PROFILE_ACTIONS_ENUM1_21_4, UPDATE_LIST_ORDER);
                break;
            case UPDATE_HAT:
                packet.write(Types.PROFILE_ACTIONS_ENUM1_21_4, UPDATE_HAT);
                break;
            default:
                throw new IllegalStateException("Cannot write " + action.name() + " for 1.21.4 packet");
        }
    }

    @Override
    protected void writeEntry(@NonNull PacketWrapper packet, @NonNull Entry entry) {
        super.writeEntry(packet, entry);
        packet.write(Types.BOOLEAN, entry.isShowHat());
    }
}