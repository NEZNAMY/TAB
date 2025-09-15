package me.neznamy.tab.shared.features.playerlistobjective;

import me.neznamy.tab.shared.Property;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class for storing playerlist objective data of players.
 */
public class PlayerListObjectivePlayerData {

    /** Player's score value (1.20.2-) */
    public Property valueLegacy;

    /** Player's score number format (1.20.3+) */
    public Property valueModern;

    /** Objective title (only visible on Bedrock Edition) */
    public Property title;

    /** Flag tracking whether this feature is disabled for the player with condition or not */
    public final AtomicBoolean disabled = new AtomicBoolean();
}
