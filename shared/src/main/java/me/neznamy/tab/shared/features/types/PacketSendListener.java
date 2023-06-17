package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for features listening to packets sent to players
 */
public interface PacketSendListener {

    /**
     * Called when a player is about to receive a packet from the server
     *
     * @param   receiver
     *          Player who is about to receive a packet
     * @param   packet
     *          The packet
     */
    void onPacketSend(@NotNull TabPlayer receiver, @NotNull Object packet);
}
