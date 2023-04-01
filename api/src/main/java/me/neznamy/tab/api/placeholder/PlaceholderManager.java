package me.neznamy.tab.api.placeholder;

import me.neznamy.tab.api.TabPlayer;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface PlaceholderManager {

    ServerPlaceholder registerServerPlaceholder(String identifier, int refresh, Supplier<Object> supplier);

    PlayerPlaceholder registerPlayerPlaceholder(String identifier, int refresh, Function<TabPlayer, Object> function);

    RelationalPlaceholder registerRelationalPlaceholder(String identifier, int refresh, BiFunction<TabPlayer, TabPlayer, Object> function);

    Placeholder getPlaceholder(String identifier);
}
