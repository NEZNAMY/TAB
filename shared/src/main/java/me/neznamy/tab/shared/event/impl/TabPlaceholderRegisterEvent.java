package me.neznamy.tab.shared.event.impl;

import lombok.Data;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.plugin.PlaceholderRegisterEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@Data
public class TabPlaceholderRegisterEvent implements PlaceholderRegisterEvent {

    @NotNull private final String identifier;
    private Supplier<String> serverPlaceholder;
    private Function<TabPlayer, String> playerPlaceholder;
    private BiFunction<TabPlayer, TabPlayer, String> relationalPlaceholder;
}
