package me.neznamy.tab.shared.event.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.neznamy.tab.api.event.plugin.TabLoadEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TabLoadEventImpl implements TabLoadEvent {

    private static final TabLoadEvent INSTANCE = new TabLoadEventImpl();

    public static TabLoadEvent getInstance() {
        return INSTANCE;
    }
}
