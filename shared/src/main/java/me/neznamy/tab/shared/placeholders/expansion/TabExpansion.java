package me.neznamy.tab.shared.placeholders.expansion;

import lombok.NonNull;
import me.neznamy.tab.shared.platform.TabPlayer;

public interface TabExpansion {

    default void setScoreboardVisible(@NonNull TabPlayer player, boolean visible) {
        setValue(player, "scoreboard_visible", visible ? "Enabled" : "Disabled");
    }

    default void setScoreboardName(@NonNull TabPlayer player, @NonNull String name) {
        setValue(player, "scoreboard_name", name);
    }

    default void setBossBarVisible(@NonNull TabPlayer player, boolean visible) {
        setValue(player, "bossbar_visible", visible ? "Enabled" : "Disabled");
    }

    default void setNameTagPreview(@NonNull TabPlayer player, boolean previewing) {
        setValue(player, "nametag_preview", previewing ? "Enabled" : "Disabled");
    }

    default void setNameTagVisibility(@NonNull TabPlayer player, boolean visible) {
        setValue(player, "nametag_visibility", visible ? "Enabled" : "Disabled");
    }

    default void setPlaceholderValue(@NonNull TabPlayer player, @NonNull String placeholder, @NonNull String value) {
        setValue(player, "placeholder_" + placeholder.substring(1, placeholder.length()-1), value);
    }

    default void setPropertyValue(@NonNull TabPlayer player, @NonNull String property, @NonNull String value) {
        setValue(player, property, value);
    }

    default void setRawPropertyValue(@NonNull TabPlayer player, @NonNull String property, @NonNull String value) {
        setValue(player, property + "_raw", value);
    }

    void setValue(@NonNull TabPlayer player, @NonNull String key, @NonNull String value);

    boolean unregister();
}
