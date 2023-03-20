package me.neznamy.tab.api.feature;

import me.neznamy.tab.api.TabPlayer;

/**
 * Interface for features listening to Login packet
 * usually sent by proxy on server switch
 */
public interface LoginPacketListener {

    /**
     * Called when player receives Login packet
     *
     * @param   packetReceiver
     *          Player who received the packet
     */
    void onLoginPacket(TabPlayer packetReceiver);
}
