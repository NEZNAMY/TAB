package me.neznamy.tab.api.event.plugin;

import me.neznamy.tab.api.event.TabEvent;
import me.neznamy.tab.api.placeholder.Placeholder;

public interface PlaceholderRegisterEvent extends TabEvent {

    String getIdentifier();

    void setPlaceholder(Placeholder placeholder);
}
