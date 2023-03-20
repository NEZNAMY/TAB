package me.neznamy.tab.api.feature;

import me.neznamy.tab.api.TabPlayer;

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
     * @throws  ReflectiveOperationException
     *          If thrown by reflective operation
     */
    void onPacketSend(TabPlayer receiver, Object packet) throws ReflectiveOperationException;
}
