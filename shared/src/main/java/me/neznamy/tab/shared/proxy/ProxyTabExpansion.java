package me.neznamy.tab.shared.proxy;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.TabExpansion;

public class ProxyTabExpansion implements TabExpansion {

    private final PluginMessageHandler plm;
    
    public ProxyTabExpansion(PluginMessageHandler plm) {
        this.plm = plm;
    }

    @Override
    public void setValue(TabPlayer player, String key, String value) {
        plm.sendMessage(player, "Expansion", key, value);
    }
}
