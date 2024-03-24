package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.platform.TabPlayer;

/**
 * Interface for features that resend content on Login packet.
 */
public interface LoginPacketListener {

    /**
     * Called after Login packet is received by the player to resend all
     * scoreboard content as the packet has cleared it.
     *
     * @param   player
     *          Player who received the packet
     */
    void onLoginPacket(TabPlayer player);
}
