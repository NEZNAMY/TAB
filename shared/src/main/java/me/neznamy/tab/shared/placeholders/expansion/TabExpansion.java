package me.neznamy.tab.shared.placeholders.expansion;

import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public interface TabExpansion {

    default void setScoreboardVisible(@NotNull TabPlayer player, boolean visible) {
        setValue(player, "scoreboard_visible", visible ? "Enabled" : "Disabled");
    }

    default void setScoreboardName(@NotNull TabPlayer player, @NotNull String name) {
        setValue(player, "scoreboard_name", name);
    }

    default void setBossBarVisible(@NotNull TabPlayer player, boolean visible) {
        setValue(player, "bossbar_visible", visible ? "Enabled" : "Disabled");
    }

    default void setNameTagPreview(@NotNull TabPlayer player, boolean previewing) {
        setValue(player, "nametag_preview", previewing ? "Enabled" : "Disabled");
    }

    default void setNameTagVisibility(@NotNull TabPlayer player, boolean visible) {
        setValue(player, "nametag_visibility", visible ? "Enabled" : "Disabled");
    }

    default void setPlaceholderValue(@NotNull TabPlayer player, @NotNull String placeholder, @NotNull String value) {
        setValue(player, "placeholder_" + placeholder.substring(1, placeholder.length()-1), value);
    }

    default void setPropertyValue(@NotNull TabPlayer player, @NotNull String property, @NotNull String value) {
        setValue(player, property, value);
    }

    default void setRawPropertyValue(@NotNull TabPlayer player, @NotNull String property, @NotNull String value) {
        setValue(player, property + "_raw", value);
    }

    void setValue(@NotNull TabPlayer player, @NotNull String key, @NotNull String value);

    boolean unregister();
}
