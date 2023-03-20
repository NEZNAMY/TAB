package me.neznamy.tab.api.feature;

import me.neznamy.tab.api.TabPlayer;

/**
 * Interface for features listening to packets sent by players
 */
public interface PacketReceiveListener {

    /**
     * Called when a player is about to send a packet to the server
     *
     * @param   sender
     *          Packet sender
     * @param   packet
     *          The packet
     * @return  Whether the packet should be cancelled or not
     * @throws  ReflectiveOperationException
     *          If thrown by reflective operation
     */
    boolean onPacketReceive(TabPlayer sender, Object packet) throws ReflectiveOperationException;
}
