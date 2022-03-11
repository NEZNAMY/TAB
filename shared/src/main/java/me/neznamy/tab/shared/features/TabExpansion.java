package me.neznamy.tab.shared.features;

import me.neznamy.tab.api.TabPlayer;

public interface TabExpansion {

    void setScoreboardVisible(TabPlayer player, boolean visible);

    void setScoreboardName(TabPlayer player, String name);

    void setBossBarVisible(TabPlayer player, boolean visible);

    void setNameTagPreview(TabPlayer player, boolean previewing);

    void setPlaceholderValue(TabPlayer player, String placeholder, String value);

    void setPropertyValue(TabPlayer player, String property, String value);

    void setRawPropertyValue(TabPlayer player, String property, String value);
}
