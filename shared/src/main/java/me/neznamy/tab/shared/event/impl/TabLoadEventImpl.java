package me.neznamy.tab.shared.event.impl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.neznamy.tab.api.event.plugin.TabLoadEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TabLoadEventImpl implements TabLoadEvent {

    @Getter private static final TabLoadEvent instance = new TabLoadEventImpl();
}
