package me.neznamy.tab.shared.placeholders.types;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.placeholders.PlaceholderIdentifier;
import me.neznamy.tab.shared.placeholders.PlaceholderReference;
import me.neznamy.tab.shared.placeholders.PlaceholderReplacementPattern;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;

/**
 * General collection of variables and functions shared between all placeholder types
 */
@Getter
public abstract class TabPlaceholder implements Placeholder {

    /**
     * Internal constant used to detect if placeholder threw an error.
     * If so, placeholder's last known value is displayed.
     */
    protected final String ERROR_VALUE = "<ERROR>";

    /** Refresh interval of the placeholder */
    private final int refresh;

    /** Placeholder's identifier including % or <> */
    @NonNull protected final String identifier;

    /** Configured placeholder output replacements */
    @NonNull protected final PlaceholderReplacementPattern replacements;

    @Setter
    protected PlaceholderReference reference;

    /**
     * Constructs new instance with given parameters and loads placeholder output replacements
     *
     * @param   identifier
     *          placeholder's identifier, must start and end with % or <>
     * @param   refresh
     *          refresh interval in milliseconds, must be divisible by {@link TabConstants.Placeholder#MINIMUM_REFRESH_INTERVAL}
     *          or equal to -1 to disable automatic refreshing
     */
    protected TabPlaceholder(@NonNull String identifier, int refresh) {
        if (refresh % TabConstants.Placeholder.MINIMUM_REFRESH_INTERVAL != 0 && refresh != -1)
            throw new IllegalArgumentException("Refresh interval must be divisible by " + TabConstants.Placeholder.MINIMUM_REFRESH_INTERVAL);
        if (!PlaceholderIdentifier.isValid(identifier))
            throw new IllegalArgumentException("Identifier must start and end with % or <> (attempted to use \"" + identifier + "\")");
        this.identifier = identifier;
        this.refresh = refresh;
        Map<Object, Object> map = TAB.getInstance().getConfiguration().getConfig().getReplacements().getValues().get(identifier);
        replacements = map == null ? PlaceholderReplacementPattern.EMPTY : PlaceholderReplacementPattern.create(identifier, map);
    }

    /**
     * Parses the placeholder for defined player, applying all nested placeholders
     * found in returned placeholder output as well as output replacements.
     *
     * @param   player
     *          player to parse placeholder for
     * @return  parsed placeholder with all nested placeholders applied
     */
    @NotNull
    public String parse(@Nullable TabPlayer player) {
        return evaluate(getLastReturnedValue(player), player);
    }

    /**
     * Evaluates given placeholder result string by applying all nested placeholders and output replacements for defined player.
     *
     * @param   returnedValue
     *          placeholder result string to evaluate
     * @param   player
     *          player to parse placeholders for
     * @return  evaluated string with all nested placeholders and output replacements applied
     */
    @NotNull
    protected String evaluate(@NotNull String returnedValue, @Nullable TabPlayer player) {
        String value = returnedValue;
        if (value.contains("%") || value.contains("<")) {
            value = setPlaceholders(value, player);
        }
        value = replacements.findReplacement(value);
        if (value.contains("%") || value.contains("<")) {
            value = setPlaceholders(value, player);
        }
        return value;
    }

    /**
     * Internal method to set placeholders in a given value for a player
     *
     * @param   value
     *          value to set placeholders in
     * @param   player
     *          player to parse placeholders for
     * @return  value with placeholders set
     */
    @NotNull
    protected String setPlaceholders(@NonNull String value, @Nullable TabPlayer player) {
        String string = value;
        if (identifier.equals(string)) return string; // Placeholder returned itself (probably invalid)

        for (String s : PlaceholderManagerImpl.detectPlaceholders(string)) {
            if (s.equals(identifier)) continue; // Prevent infinite loop when placeholder returns itself
            if (PlaceholderIdentifier.isRelational(s)) continue; // Relational placeholders are handled separately
            if ((identifier.startsWith("%sync:") && ("%" + identifier.substring(6)).equals(s))) continue; // Self, but as sync variant
            TabPlaceholder nested = TAB.getInstance().getPlaceholderManager().getPlaceholder(s);
            nested.reference.addParent(reference);
            string = string.replace(s, nested.parse(player));
        }
        return string;
    }

    /**
     * Updates all placeholders that use this placeholder
     * as a nested placeholder
     *
     * @param   player
     *          Player to update placeholders for.
     */
    public void updateParents(@NonNull TabPlayer player) {
        if (reference.getParents().isEmpty()) return;
        for (PlaceholderReference pl : new ArrayList<>(reference.getParents())) {
            pl.getHandle().updateFromNested(player);
            pl.getHandle().updateParents(player);
        }
    }

    /**
     * Updates the placeholder with force mark for requested player
     *
     * @param   player
     *          player to update placeholder for
     */
    public abstract void updateFromNested(@NonNull TabPlayer player);

    /**
     * Returns last known value of defined player
     *
     * @param   player
     *          player to get value of
     * @return  last known value for specified player
     */
    public abstract @NotNull String getLastValue(@Nullable TabPlayer player);

    /**
     * Returns the last returned value of the placeholder for the given player, without applying any replacements or nested placeholders.
     * This is the last known raw result of the given placeholder function.
     *
     * @param   player
     *          player to get value of
     * @return  Last known raw value for specified player
     */
    @NotNull
    public abstract String getLastReturnedValue(@Nullable TabPlayer player);

    /**
     * Returns last known value of defined player without refreshing it if not present.
     *
     * @param   player
     *          player to get value of
     * @return  last known value for specified player or identifier if not available
     */
    @NotNull
    public abstract String getLastValueSafe(@NotNull TabPlayer player);
}