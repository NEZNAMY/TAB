package me.neznamy.tab.shared.proxy;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.TabExpansion;

public class ProxyTabExpansion implements TabExpansion {

    @Override
    public void setValue(TabPlayer player, String key, String value) {
        ((ProxyPlatform)TAB.getInstance().getPlatform()).getPluginMessageHandler().sendMessage(player, "Expansion", key, value);
    }
}
