package me.neznamy.tab.shared.placeholders.expansion;

import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Interface representing TAB's PlaceholderAPI expansion.
 */
public interface TabExpansion {

    /**
     * Sets scoreboard visibility placeholder to specified value.
     *
     * @param   player
     *          Player to set placeholder value for
     * @param   visible
     *          Whether scoreboard is visible or not
     */
    default void setScoreboardVisible(@NotNull TabPlayer player, boolean visible) {
        setValue(player, "scoreboard_visible", visible ? "Enabled" : "Disabled");
    }

    /**
     * Sets scoreboard name placeholder to specified value.
     *
     * @param   player
     *          Player to set placeholder value for
     * @param   name
     *          Name of currently visible scoreboard
     */
    default void setScoreboardName(@NotNull TabPlayer player, @NotNull String name) {
        setValue(player, "scoreboard_name", name);
    }

    /**
     * Sets bossbar visibility placeholder to specified value.
     *
     * @param   player
     *          Player to set placeholder value for
     * @param   visible
     *          Whether bossbar is visible or not
     */
    default void setBossBarVisible(@NotNull TabPlayer player, boolean visible) {
        setValue(player, "bossbar_visible", visible ? "Enabled" : "Disabled");
    }

    /**
     * Sets nametag preview placeholder to specified value.
     *
     * @param   player
     *          Player to set placeholder value for
     * @param   previewing
     *          Whether player is previewing nametag or not
     */
    default void setNameTagPreview(@NotNull TabPlayer player, boolean previewing) {
        setValue(player, "nametag_preview", previewing ? "Enabled" : "Disabled");
    }

    /**
     * Sets nametag visibility toggle choice placeholder to specified value.
     *
     * @param   player
     *          Player to set placeholder value for
     * @param   visible
     *          Whether player wants to see nametags or not
     */
    default void setNameTagVisibility(@NotNull TabPlayer player, boolean visible) {
        setValue(player, "nametag_visibility", visible ? "Enabled" : "Disabled");
    }

    /**
     * Sets internal placeholder value.
     *
     * @param   player
     *          Player to set placeholder value for
     * @param   placeholder
     *          Identifier of internal placeholder
     * @param   value
     *          Placeholder value
     */
    default void setPlaceholderValue(@NotNull TabPlayer player, @NotNull String placeholder, @NotNull String value) {
        setValue(player, "placeholder_" + placeholder.substring(1, placeholder.length()-1), value);
    }

    /**
     * Sets property value placeholder to specified placeholder.
     *
     * @param   player
     *          Player to set placeholder value for
     * @param   property
     *          Name of property
     * @param   value
     *          Value of property
     */
    default void setPropertyValue(@NotNull TabPlayer player, @NotNull String property, @NotNull String value) {
        setValue(player, property, value);
    }

    /**
     * Sets raw property value placeholder to specified placeholder.
     *
     * @param   player
     *          Player to set placeholder value for
     * @param   property
     *          Name of property
     * @param   value
     *          Raw value of property
     */
    default void setRawPropertyValue(@NotNull TabPlayer player, @NotNull String property, @NotNull String value) {
        setValue(player, property + "_raw", value);
    }

    /**
     * Sets expansion placeholder to specified value.
     *
     * @param   player
     *          Player to set placeholder value for
     * @param   key
     *          Name of placeholder
     * @param   value
     *          Placeholder value
     */
    void setValue(@NotNull TabPlayer player, @NotNull String key, @NotNull String value);

    /**
     * Unregisters the expansion from PlaceholderAPI.
     */
    void unregisterExpansion();
}
