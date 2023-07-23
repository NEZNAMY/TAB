package me.neznamy.tab.shared.features.types;

import me.neznamy.tab.shared.platform.TabPlayer;

import java.util.UUID;

/**
 * Interface allowing features to listen to entry add packet
 */
public interface EntryAddListener {

    /**
     * Called when entry is added into a player's tablist
     *
     * @param   packetReceiver
     *          Player who received the packet
     * @param   id
     *          UUID of added entry
     * @param   name
     *          Username of added entry
     */
    void onEntryAdd(TabPlayer packetReceiver, UUID id, String name);
}
