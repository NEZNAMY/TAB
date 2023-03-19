package me.neznamy.tab.api;

import lombok.NonNull;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;

import java.util.UUID;

public interface BossBarHandler {

    /**
     * Creates boss bar and sends it to the player
     *
     * @param   id
     *          Unique identifier to match the bossbar with other functions
     * @param   title
     *          BossBar title
     * @param   progress
     *          BossBar progress (0-1)
     * @param   color
     *          BossBar color
     * @param   style
     *          BossBar style
     */
    void create(@NonNull UUID id, @NonNull String title, float progress, @NonNull BarColor color, @NonNull BarStyle style);

    /**
     * Updates title
     *
     * @param   id
     *          Unique identifier to match the bossbar with other functions
     * @param   title
     *          New title
     */
    void update(@NonNull UUID id, @NonNull String title);

    /**
     * Updates progress
     *
     * @param   id
     *          Unique identifier to match the bossbar with other functions
     * @param   progress
     *          New progress (0-1)
     */
    void update(@NonNull UUID id, float progress);

    /**
     * Updates style
     *
     * @param   id
     *          Unique identifier to match the bossbar with other functions
     * @param   style
     *          New style
     */
    void update(@NonNull UUID id, @NonNull BarStyle style);

    /**
     * Updates color
     *
     * @param   id
     *          Unique identifier to match the bossbar with other functions
     * @param   color
     *          New color
     */
    void update(@NonNull UUID id, @NonNull BarColor color);

    /**
     * Removes boss bar
     *
     * @param   id
     *          Unique identifier to match the bossbar with other functions
     */
    void remove(@NonNull UUID id);
}
