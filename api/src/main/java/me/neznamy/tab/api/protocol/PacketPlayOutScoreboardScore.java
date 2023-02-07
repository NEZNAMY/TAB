package me.neznamy.tab.api.protocol;

import lombok.Data;
import lombok.NonNull;

/**
 * A class representing platform specific packet class
 */
@Data
public class PacketPlayOutScoreboardScore implements TabPacket {

    /** Packet action */
    @NonNull private final Action action;

    /** Objective name */
    @NonNull private final String objectiveName;

    /** Affected player */
    @NonNull private final String player;

    /** Player's score */
    private final int score;

    /**
     * An enum representing action types
     */
    public enum Action {

        CHANGE,
        REMOVE
    }
}