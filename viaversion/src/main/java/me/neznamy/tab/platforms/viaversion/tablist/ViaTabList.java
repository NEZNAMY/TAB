package me.neznamy.tab.platforms.viaversion.tablist;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import lombok.NonNull;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.UUID;

/**
 * TabList abstract handler using ViaVersion packets,
 * focused to unlock new features on older versions,
 * and also optimize packet conversion between versions.
 *
 * @param <P> Platform's TabPlayer class
 */
public abstract class ViaTabList<P extends TabPlayer> extends TrackedTabList<P> {

    protected final Class<? extends Protocol> protocol;
    private final PacketType playerInfoUpdate;
    private final PacketType tabList;
    /** User connection this tablist belongs to */
    protected final UserConnection connection;

    /**
     *
     * @param player
     *        Player this tablist will belong to
     * @param protocol
     *        Protocol to be sent packets through
     * @param playerInfoUpdate
     *        Player information update packet
     * @param tabList
     *        Tab list packet
     */
    public ViaTabList(@NonNull P player, @NonNull Class<? extends Protocol> protocol, @NonNull PacketType playerInfoUpdate, @NonNull PacketType tabList) {
        super(player);
        this.protocol = protocol;
        this.playerInfoUpdate = playerInfoUpdate;
        this.tabList = tabList;
        this.connection = Via.getManager().getConnectionManager().getConnectedClient(player.getUniqueId());
    }

    @Override
    public void addEntry0(@NonNull Entry entry) {
        sendInfoUpdate(Action.ADD_PLAYER, entry.getUniqueId(), entry);
    }

    @Override
    public void updateDisplayName0(@NonNull UUID entry, @Nullable TabComponent displayName) {
        sendInfoUpdate(Action.UPDATE_DISPLAY_NAME, entry, displayName);
    }

    @Override
    public void updateLatency(@NonNull UUID entry, int latency) {
        sendInfoUpdate(Action.UPDATE_LATENCY, entry, latency);
    }

    @Override
    public void updateGameMode(@NonNull UUID entry, int gameMode) {
        sendInfoUpdate(Action.UPDATE_GAME_MODE, entry, gameMode);
    }

    @Override
    public boolean containsEntry(@NonNull UUID entry) {
        return true;
    }

    @Override
    public void setPlayerListHeaderFooter(@NonNull TabComponent header, @NonNull TabComponent footer) {
        final PacketWrapper packet = PacketWrapper.create(tabList, null, connection);

        writeComponent(packet, header);
        writeComponent(packet, footer);

        packet.scheduleSend(protocol);
    }

    protected void sendInfoUpdate(@NonNull Action action, @NonNull UUID uniqueId, Object value) {
        final PacketWrapper packet = PacketWrapper.create(playerInfoUpdate, null, connection);

        // Action
        writeAction(packet, action);
        // Players size
        packet.write(Types.VAR_INT, 1);
        // Players
        packet.write(Types.UUID, uniqueId);
        switch (action) {
            case ADD_PLAYER:
                writeEntry(packet, (Entry) value);
                break;
            case REMOVE_PLAYER:
                break;
            case UPDATE_GAME_MODE:
            case UPDATE_LATENCY:
            case UPDATE_LIST_ORDER:
                packet.write(Types.VAR_INT, (int) value);
                break;
            case UPDATE_LISTED:
            case UPDATE_HAT:
                packet.write(Types.BOOLEAN, (boolean) value);
                break;
            case UPDATE_DISPLAY_NAME:
                writeOptionalComponent(packet, (TabComponent) value);
                break;
            default:
                throw new IllegalArgumentException("Cannot send info update with action " + action.name());
        }

        packet.scheduleSend(protocol);
    }

    protected abstract void writeComponent(@NonNull PacketWrapper packet, @NonNull TabComponent component);

    protected abstract void writeOptionalComponent(@NonNull PacketWrapper packet, @Nullable TabComponent component);

    protected abstract void writeAction(@NonNull PacketWrapper packet, @NonNull Action action);

    protected abstract void writeEntry(@NonNull PacketWrapper packet, @NonNull Entry entry);

    @NotNull
    protected static BitSet bitSet(int nbits, int action) {
        final BitSet bitSet = new BitSet(nbits);
        bitSet.set(action);
        return bitSet;
    }

    @NotNull
    protected static BitSet bitSet(int nbits, int fromAction, int toAction) {
        final BitSet bit = new BitSet(nbits);
        bit.set(fromAction, toAction);
        return bit;
    }
}
