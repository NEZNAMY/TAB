package me.neznamy.tab.shared.event.impl;

import me.neznamy.tab.api.event.plugin.TabLoadEvent;

public final class TabLoadEventImpl implements TabLoadEvent {

    private static final TabLoadEvent INSTANCE = new TabLoadEventImpl();

    public static TabLoadEvent getInstance() {
        return INSTANCE;
    }

    private TabLoadEventImpl() {
    }
}
