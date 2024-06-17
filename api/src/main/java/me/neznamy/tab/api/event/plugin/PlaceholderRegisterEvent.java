package me.neznamy.tab.api.event.plugin;

import lombok.NonNull;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.TabEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Called when an unknown placeholder is about to be registered.
 * It defaults to a PlaceholderAPI implementation, but can be overridden.
 */
public interface PlaceholderRegisterEvent extends TabEvent {

    /**
     * Returns placeholder identifier starting and ending with {@code %}
     *
     * @return  placeholder identifier starting and ending with {@code %}
     */
    @NotNull String getIdentifier();

    /**
     * Sets placeholder to specified server placeholder implementation.
     *
     * @param   supplier
     *          Placeholder replacer
     */
    void setServerPlaceholder(@NonNull Supplier<String> supplier);

    /**
     * Sets placeholder to specified player placeholder implementation.
     *
     * @param   function
     *          Placeholder replacer
     */
    void setPlayerPlaceholder(@NonNull Function<TabPlayer, String> function);

    /**
     * Sets placeholder to specified relational placeholder implementation.
     *
     * @param   function
     *          Placeholder replacer
     */
    void setRelationalPlaceholder(@NonNull BiFunction<TabPlayer, TabPlayer, String> function);
}
