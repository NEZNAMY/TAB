package me.neznamy.tab.shared.features.belowname;

import me.neznamy.tab.shared.Property;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class for storing belowname data of players.
 */
public class BelowNamePlayerData {

    /** Player's score value (1.20.2-) */
    public Property score;

    /** Player's score number format (1.20.3+) */
    public Property numberFormat;

    /** Scoreboard title */
    public Property text;

    /** Default number format for NPCs (1.20.3+) */
    public Property defaultNumberFormat;

    /** Flag tracking whether this feature is disabled for the player with condition or not */
    public final AtomicBoolean disabled = new AtomicBoolean();
}
