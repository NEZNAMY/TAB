package me.neznamy.tab.shared.proxy;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.TabExpansion;

public class ProxyTabExpansion implements TabExpansion {

    private final PluginMessageHandler plm;
    
    public ProxyTabExpansion(PluginMessageHandler plm) {
        this.plm = plm;
    }
    
    @Override
    public void setScoreboardVisible(TabPlayer player, boolean visible) {
        plm.sendMessage(player, "Expansion", "scoreboard_visible", visible ? "Enabled" : "Disabled");
    }

    @Override
    public void setScoreboardName(TabPlayer player, String name) {
        plm.sendMessage(player, "Expansion", "scoreboard_name", name);
    }

    @Override
    public void setBossBarVisible(TabPlayer player, boolean visible) {
        plm.sendMessage(player, "Expansion", "bossbar_visible", visible ? "Enabled" : "Disabled");
    }

    @Override
    public void setNameTagPreview(TabPlayer player, boolean previewing) {
        plm.sendMessage(player, "Expansion", "ntpreview", previewing ? "Enabled" : "Disabled");
    }

    @Override
    public void setPlaceholderValue(TabPlayer player, String placeholder, String value) {
        plm.sendMessage(player, "Expansion", "placeholder_" + placeholder.substring(1, placeholder.length()-1), value);
    }

    @Override
    public void setPropertyValue(TabPlayer player, String property, String value) {
        plm.sendMessage(player, "Expansion", property, value);
    }

    @Override
    public void setRawPropertyValue(TabPlayer player, String property, String value) {
        plm.sendMessage(player, "Expansion", property + "_raw", value);
    }
}
