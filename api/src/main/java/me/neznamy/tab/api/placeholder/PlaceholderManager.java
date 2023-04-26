package me.neznamy.tab.api.placeholder;

import lombok.NonNull;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface PlaceholderManager {

    @NotNull ServerPlaceholder registerServerPlaceholder(@NonNull String identifier, int refresh, @NonNull Supplier<Object> supplier);

    @NotNull PlayerPlaceholder registerPlayerPlaceholder(@NonNull String identifier, int refresh, @NonNull Function<TabPlayer, Object> function);

    @NotNull RelationalPlaceholder registerRelationalPlaceholder(@NonNull String identifier, int refresh, @NonNull BiFunction<TabPlayer, TabPlayer, Object> function);

    @NotNull Placeholder getPlaceholder(@NonNull String identifier);
}
