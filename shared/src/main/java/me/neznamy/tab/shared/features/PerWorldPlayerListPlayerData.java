package me.neznamy.tab.shared.features;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class for storing per-world playerlist data of players.
 */
public class PerWorldPlayerListPlayerData {

    /** Whether the feature is disabled for this player by condition. */
    public final AtomicBoolean disabled = new AtomicBoolean();
}
