package me.neznamy.tab.shared.features.sorting;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class storing sorting data for players.
 */
public class SortingPlayerData {

    /** Short team name (16 chars), used for teams */
    public String shortTeamName;

    /** Full sorting string, used for sorting in Layout (and maybe for 1.18+ in the future) */
    public String fullTeamName;

    /** Note explaining player's current team name */
    public String teamNameNote;

    /** Forced team name using API */
    @Nullable
    public String forcedTeamName;

    /**
     * Returns short team name. If forced using API, that value is returned.
     *
     * @return  short team name to use
     */
    @NotNull
    public String getShortTeamName() {
        return forcedTeamName != null ? forcedTeamName : shortTeamName;
    }

    /**
     * Returns full team name. If forced using API, that value is returned.
     *
     * @return  full team name to use
     */
    @NotNull
    public String getFullTeamName() {
        return forcedTeamName != null ? forcedTeamName : fullTeamName;
    }
}