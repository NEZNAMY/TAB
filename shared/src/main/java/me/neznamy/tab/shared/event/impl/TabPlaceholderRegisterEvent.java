package me.neznamy.tab.shared.event.impl;

import me.neznamy.tab.api.event.plugin.PlaceholderRegisterEvent;
import me.neznamy.tab.api.placeholder.Placeholder;

public class TabPlaceholderRegisterEvent implements PlaceholderRegisterEvent {

    private final String identifier;
    private Placeholder placeholder;

    public TabPlaceholderRegisterEvent(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void setPlaceholder(Placeholder placeholder) {
        this.placeholder = placeholder;
    }

    public Placeholder getPlaceholder() {
        return placeholder;
    }
}
