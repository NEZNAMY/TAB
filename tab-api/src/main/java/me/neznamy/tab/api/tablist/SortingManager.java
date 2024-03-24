package me.neznamy.tab.api.tablist;

import lombok.NonNull;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for managing player sorting by directly configuring
 * team names of players. Instance can be obtained using
 * {@link TabAPI#getSortingManager()}. If the feature is disabled,
 * it will return {@code null}.
 */
@SuppressWarnings("unused") // API class
public interface SortingManager {

    /**
     * Forces new team name for the player until this method is called again with null argument and
     * performs all actions to change player's team name
     *
     * @param   player
     *          player to set team name of
     * @param   teamName
     *          forced team name
     */
    void forceTeamName(@NonNull TabPlayer player, @Nullable String teamName);

    /**
     * Returns forced team name of player or null if not forced
     *
     * @param   player
     *          player to check forced team name of
     * @return  forced team name of player or null if not forced
     */
    @Nullable String getForcedTeamName(@NonNull TabPlayer player);

    /**
     * Returns original team name of the player set by the plugin.
     *
     * @param   player
     *          Player to get team name of
     * @return  Player's team name based on configuration.
     */
    @NotNull String getOriginalTeamName(@NonNull TabPlayer player);
}
