package me.neznamy.tab.shared.placeholders.expansion;

import me.neznamy.tab.api.TabPlayer;

/**
 * Dummy implementation when expansion is disabled or not supported by platform
 */
public class EmptyTabExpansion implements TabExpansion {

    @Override
    public void setValue(TabPlayer player, String key, String value) {}

    @Override
    public boolean unregister() { return false; }
}
