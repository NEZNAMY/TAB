package me.neznamy.tab.shared.event.impl;

import lombok.Data;
import lombok.NonNull;
import me.neznamy.tab.api.event.plugin.PlaceholderRegisterEvent;
import me.neznamy.tab.api.placeholder.Placeholder;
import org.jetbrains.annotations.Nullable;

@Data
public class TabPlaceholderRegisterEvent implements PlaceholderRegisterEvent {

    @NonNull private final String identifier;
    @Nullable private Placeholder placeholder;
}
