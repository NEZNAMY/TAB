package me.neznamy.tab.shared.event.impl;

import lombok.Data;
import me.neznamy.tab.api.event.plugin.PlaceholderRegisterEvent;
import me.neznamy.tab.api.placeholder.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public class TabPlaceholderRegisterEvent implements PlaceholderRegisterEvent {

    @NotNull private final String identifier;
    @Nullable private Placeholder placeholder;
}
