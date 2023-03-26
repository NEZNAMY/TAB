package me.neznamy.tab.api;

import lombok.NonNull;

import java.util.Collection;

public interface Scoreboard {

    /**
     * Sets objective display slot of specified objective
     *
     * @param   slot
     *          Display slot
     * @param   objective
     *          Objective name
     */
    void setDisplaySlot(DisplaySlot slot, @NonNull String objective);

    /**
     * Sets scoreboard score
     * @param   objective
     *          Objective name
     * @param   player
     *          Affected player
     * @param   score
     *          New score value
     */
    void setScore(@NonNull String objective, @NonNull String player, int score);

    /**
     * Removes scoreboard score
     *
     * @param   objective
     *          Objective to remove from
     * @param   player
     *          Player to remove from sidebar
     */
    void removeScore(@NonNull String objective, @NonNull String player);

    void registerObjective(@NonNull String objectiveName, @NonNull String title, boolean hearts);

    void unregisterObjective(@NonNull String objectiveName);

    void updateObjective(@NonNull String objectiveName, @NonNull String title, boolean hearts);

    void registerTeam(@NonNull String name, String prefix, String suffix, String visibility,
                                String collision, Collection<String> players, int options);

    void unregisterTeam(@NonNull String name);

    void updateTeam(@NonNull String name, String prefix, String suffix, String visibility, String collision, int options);

    enum DisplaySlot {

        PLAYER_LIST, SIDEBAR, BELOW_NAME
    }
}
