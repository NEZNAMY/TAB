package me.neznamy.tab.shared.event.impl;

import lombok.Data;
import me.neznamy.tab.api.event.plugin.PlaceholderRegisterEvent;
import me.neznamy.tab.api.placeholder.Placeholder;

@Data
public class TabPlaceholderRegisterEvent implements PlaceholderRegisterEvent {

    private final String identifier;
    private Placeholder placeholder;
}
