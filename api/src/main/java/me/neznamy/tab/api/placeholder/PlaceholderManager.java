package me.neznamy.tab.api.placeholder;

import lombok.NonNull;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An interface allowing placeholder registration. Instance can be
 * obtained using {@link TabAPI#getPlaceholderManager()}
 */
@SuppressWarnings("unused") // API class
public interface PlaceholderManager {

    /**
     * Registers a server placeholder (placeholder with same output for all players)
     *
     * @param   identifier
     *          Placeholder identifier
     * @param   refresh
     *          Refresh interval
     * @param   supplier
     *          Supplier for placeholder output
     * @return  Registered placeholder for further use
     * @throws  IllegalArgumentException
     *          If {@code identifier} does not start and end with {@code %} or
     *          {@code refresh} is not divisible by 50
     */
    @NotNull ServerPlaceholder registerServerPlaceholder(@NonNull String identifier, int refresh, @NonNull Supplier<Object> supplier);

    /**
     * Registers a player placeholder (placeholder different output per player)
     *
     * @param   identifier
     *          Placeholder identifier
     * @param   refresh
     *          Refresh interval
     * @param   function
     *          Function for placeholder output
     * @return  Registered placeholder for further use
     * @throws  IllegalArgumentException
     *          If {@code identifier} does not start and end with {@code %} or
     *          {@code refresh} is not divisible by 50
     */
    @NotNull PlayerPlaceholder registerPlayerPlaceholder(@NonNull String identifier, int refresh, @NonNull Function<TabPlayer, Object> function);

    /**
     * Registers a relational placeholder (placeholder with output different for each player duo)
     *
     * @param   identifier
     *          Placeholder identifier
     * @param   refresh
     *          Refresh interval
     * @param   function
     *          Function for placeholder output
     * @return  Registered placeholder for further use
     * @throws  IllegalArgumentException
     *          If {@code identifier} does not start and end with {@code %},
     *          does not start with {@code %rel_} or
     *          {@code refresh} is not divisible by 50
     */
    @NotNull RelationalPlaceholder registerRelationalPlaceholder(@NonNull String identifier, int refresh, @NonNull BiFunction<TabPlayer, TabPlayer, Object> function);

    /**
     * Returns placeholder from specified identifier. If it does not exist, it is registered
     * as a PlaceholderAPI placeholder and returned.
     *
     * @param   identifier
     *          Placeholder identifier
     * @return  Placeholder with specified identifier
     */
    @NotNull Placeholder getPlaceholder(@NonNull String identifier);

    /**
     * Unregisters placeholder and makes plugin no longer refresh it.
     *
     * @param   placeholder
     *          Placeholder to unregister
     */
    void unregisterPlaceholder(@NonNull Placeholder placeholder);

    /**
     * Unregisters placeholder and makes plugin no longer refresh it.
     *
     * @param   identifier
     *          identifier of placeholder to unregister
     */
    void unregisterPlaceholder(@NonNull String identifier);
}
