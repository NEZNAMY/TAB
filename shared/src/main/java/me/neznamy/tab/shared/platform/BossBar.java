package me.neznamy.tab.shared.platform;

import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Interface for sending BossBars to players.
 */
public interface BossBar {

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
    void create(@NotNull UUID id, @NotNull String title, float progress, @NotNull BarColor color, @NotNull BarStyle style);

    /**
     * Updates title
     *
     * @param   id
     *          Unique identifier to match the bossbar with other functions
     * @param   title
     *          New title
     */
    void update(@NotNull UUID id, @NotNull String title);

    /**
     * Updates progress
     *
     * @param   id
     *          Unique identifier to match the bossbar with other functions
     * @param   progress
     *          New progress (0-1)
     */
    void update(@NotNull UUID id, float progress);

    /**
     * Updates style
     *
     * @param   id
     *          Unique identifier to match the bossbar with other functions
     * @param   style
     *          New style
     */
    void update(@NotNull UUID id, @NotNull BarStyle style);

    /**
     * Updates color
     *
     * @param   id
     *          Unique identifier to match the bossbar with other functions
     * @param   color
     *          New color
     */
    void update(@NotNull UUID id, @NotNull BarColor color);

    /**
     * Removes boss bar
     *
     * @param   id
     *          Unique identifier to match the bossbar with other functions
     */
    void remove(@NotNull UUID id);
}
