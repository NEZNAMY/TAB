package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Interface for features listening to display name change
 */
public interface DisplayNameListener {

    /**
     * Called when player is about to receive a packet for changing display name
     *
     * @param   packetReceiver
     *          Player who is about to receive the packet
     * @param   id
     *          Affected entry
     * @return  New display name to write to the packet, {@code null} for keeping it
     */
    @Nullable
    TabComponent onDisplayNameChange(@NotNull TabPlayer packetReceiver, @NotNull UUID id);
}
