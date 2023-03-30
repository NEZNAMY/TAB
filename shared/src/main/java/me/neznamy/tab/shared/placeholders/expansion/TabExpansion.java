package me.neznamy.tab.shared.placeholders.expansion;

import me.neznamy.tab.api.TabPlayer;

public interface TabExpansion {

    default void setScoreboardVisible(TabPlayer player, boolean visible) {
        setValue(player, "scoreboard_visible", visible ? "Enabled" : "Disabled");
    }

    default void setScoreboardName(TabPlayer player, String name) {
        setValue(player, "scoreboard_name", name);
    }

    default void setBossBarVisible(TabPlayer player, boolean visible) {
        setValue(player, "bossbar_visible", visible ? "Enabled" : "Disabled");
    }

    default void setNameTagPreview(TabPlayer player, boolean previewing) {
        setValue(player, "nametag_preview", previewing ? "Enabled" : "Disabled");
    }

    default void setNameTagVisibility(TabPlayer player, boolean visible) {
        setValue(player, "nametag_visibility", visible ? "Enabled" : "Disabled");
    }

    default void setPlaceholderValue(TabPlayer player, String placeholder, String value) {
        setValue(player, "placeholder_" + placeholder.substring(1, placeholder.length()-1), value);
    }

    default void setPropertyValue(TabPlayer player, String property, String value) {
        setValue(player, property, value);
    }

    default void setRawPropertyValue(TabPlayer player, String property, String value) {
        setValue(player, property + "_raw", value);
    }

    void setValue(TabPlayer player, String key, String value);

    boolean unregister();
}
