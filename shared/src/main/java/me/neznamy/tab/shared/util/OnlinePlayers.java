package me.neznamy.tab.shared.util;

import lombok.Getter;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class for tracking online players and returning them as an array
 * to save resources when iterating.
 */
public class OnlinePlayers {

    /** Set tracking online players */
    private final Set<TabPlayer> playerSet;

    /** Player array for fast access without creating iterator */
    @Getter
    private TabPlayer[] players;

    /**
     * Constructs new instance with given players.
     *
     * @param   players
     *          Online players
     */
    public OnlinePlayers(@NotNull TabPlayer[] players) {
        playerSet = Arrays.stream(players).collect(Collectors.toSet());
        this.players = players.clone();
    }

    /**
     * Adds player to online players.
     *
     * @param   player
     *          Player to add
     */
    public void addPlayer(@NotNull TabPlayer player) {
        playerSet.add(player);
        players = playerSet.toArray(new TabPlayer[0]);
    }

    /**
     * Removes player from online players.
     *
     * @param   player
     *          Player to remove
     */
    public void removePlayer(@NotNull TabPlayer player) {
        playerSet.remove(player);
        players = playerSet.toArray(new TabPlayer[0]);
    }

    /**
     * Checks if given player is online.
     *
     * @param   player
     *          Player to check
     * @return  {@code true} if player is online, {@code false} if not
     */
    public boolean contains(@NotNull TabPlayer player) {
        return playerSet.contains(player);
    }
}
