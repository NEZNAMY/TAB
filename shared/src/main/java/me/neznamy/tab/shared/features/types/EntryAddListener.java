package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.player.TabPlayer;

import java.util.UUID;

/**
 * Interface for features listening to tablist entry add
 */
public interface EntryAddListener {

    /**
     * Called when player is about to receive packet for adding a tablist entry
     *
     * @param   packetReceiver
     *          Player who received the packet
     * @param   id
     *          UUID of added player
     * @param   name
     *          Username of added player
     */
    void onEntryAdd(TabPlayer packetReceiver, UUID id, String name);
}
