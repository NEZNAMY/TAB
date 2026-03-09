package me.neznamy.tab.shared.features.belowname;

import me.neznamy.tab.shared.Property;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class for storing belowname data of players.
 */
public class BelowNamePlayerData {

    /** Player's value (1.20.2-) */
    public Property value;

    /** Player's number format (1.20.3+) */
    public Property fancyValue;

    /** Objective title */
    public Property title;

    /** Default number format for NPCs (1.20.3+) */
    public Property defaultNumberFormat;

    /** Flag tracking whether this feature is disabled for the player with condition or not */
    public final AtomicBoolean disabled = new AtomicBoolean();
}
