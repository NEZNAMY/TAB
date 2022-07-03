package me.neznamy.tab.api.protocol;

import me.neznamy.tab.api.util.Preconditions;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutScoreboardScore implements TabPacket {

    /** Packet action */
    private final Action action;

    /** Objective name */
    private final String objectiveName;

    /** Affected player */
    private final String player;

    /** Player's score */
    private final int score;

    /**
     * Constructs new instance with given parameters
     *
     * @param   action
     *          Packet action
     * @param   objectiveName
     *          Objective name
     * @param   player
     *          Affected player
     * @param   score
     *          Player's score
     * @throws  IllegalArgumentException
     *          if {@code objectiveName} is null or longer than 16 characters
     */
    public PacketPlayOutScoreboardScore(Action action, String objectiveName, String player, int score) {
        Preconditions.checkNotNull(action, "action");
        Preconditions.checkNotNull(objectiveName, "objective name");
        Preconditions.checkMaxLength(objectiveName, 16, "objective name");
        Preconditions.checkNotNull(player, "player");
        this.action = action;
        this.objectiveName = objectiveName;
        this.player = player;
        this.score = score;
    }

    @Override
    public String toString() {
        return String.format("PacketPlayOutScoreboardScore{action=%s,objectiveName=%s,player=%s,score=%s}",
                action, objectiveName, player, score);
    }

    /**
     * Returns {@link #action}
     *
     * @return  packet action
     */
    public Action getAction() {
        return action;
    }

    /**
     * Returns {@link #objectiveName}
     *
     * @return  objective name
     */
    public String getObjectiveName() {
        return objectiveName;
    }

    /**
     * Returns {@link #player}
     *
     * @return  player
     */
    public String getPlayer() {
        return player;
    }

    /**
     * Returns {@link #score}
     *
     * @return  score
     */
    public int getScore() {
        return score;
    }

    /**
     * An enum representing action types
     */
    public enum Action {

        CHANGE,
        REMOVE
    }
}