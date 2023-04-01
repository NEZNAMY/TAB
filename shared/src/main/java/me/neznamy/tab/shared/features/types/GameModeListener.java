package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.player.TabPlayer;

import java.util.UUID;

/**
 * Interface for features listening to gamemode change packet
 */
public interface GameModeListener {

    /**
     * Called when player receives packet to change gamemode
     *
     * @param   packetReceiver
     *          Player who received the packet
     * @param   id
     *          UUID of affected player
     * @param   gameMode
     *          gamemode in the packet
     * @return  New gamemode to write to the packet
     */
    int onGameModeChange(TabPlayer packetReceiver, UUID id, int gameMode);
}
