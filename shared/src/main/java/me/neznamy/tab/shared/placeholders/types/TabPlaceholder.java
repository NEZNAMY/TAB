package me.neznamy.tab.shared.placeholders.types;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.placeholders.PlaceholderReplacementPattern;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    protected final String ERROR_VALUE = "ERROR";

    /** Refresh interval of the placeholder */
    private final int refresh;

    /** Placeholder's identifier including % */
    @NonNull protected final String identifier;

    /** Configured placeholder output replacements */
    @NonNull protected final PlaceholderReplacementPattern replacements;

    /**
     * List of placeholders using this placeholder as a nested placeholder,
     * mutual tracking allows faster parent placeholder changes when a nested
     * placeholder changed value.
     */
    protected final List<String> parents = new ArrayList<>();

    /**
     * Constructs new instance with given parameters and loads placeholder output replacements
     *
     * @param   identifier
     *          placeholder's identifier, must start and end with %
     * @param   refresh
     *          refresh interval in milliseconds, must be divisible by {@link TabConstants.Placeholder#MINIMUM_REFRESH_INTERVAL}
     *          or equal to -1 to disable automatic refreshing
     */
    protected TabPlaceholder(@NonNull String identifier, int refresh) {
        if (refresh % TabConstants.Placeholder.MINIMUM_REFRESH_INTERVAL != 0 && refresh != -1)
            throw new IllegalArgumentException("Refresh interval must be divisible by " + TabConstants.Placeholder.MINIMUM_REFRESH_INTERVAL);
        if (!identifier.startsWith("%") || !identifier.endsWith("%"))
            throw new IllegalArgumentException("Identifier must start and end with % (attempted to use \"" + identifier + "\")");
        this.identifier = identifier;
        this.refresh = refresh;
        Map<String, Map<Object, Object>> map = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("placeholder-output-replacements");
        replacements = new PlaceholderReplacementPattern(identifier, map.getOrDefault(identifier, Collections.emptyMap()));
        for (String nested : getNestedPlaceholders("")) {
            TAB.getInstance().getPlaceholderManager().getPlaceholder(nested).addParent(identifier);
        }
        for (String nested : replacements.getNestedPlaceholders()) {
            TAB.getInstance().getPlaceholderManager().getPlaceholder(nested).addParent(identifier);
        }
    }

    /**
     * Replaces this placeholder in given string and returns output. If the entered string
     * is equal to the placeholder identifier or does not contain the identifier at all,
     * value is returned directly without calling {@code String#replace} for better performance.
     *
     * @param   string
     *          string to replace this placeholder in
     * @param   player
     *          player to set placeholder for
     * @return  string with this placeholder replaced
     */
    public String set(@NonNull String string, @Nullable TabPlayer player) {
        return replace(string, identifier, setPlaceholders(getLastValue(player), player));
    }

    /**
     * Returns all nested placeholders in provided output. If no placeholders are detected,
     * returns empty list.
     *
     * @param   output
     *          output to check
     * @return  List of nested placeholders in provided output
     */
    public List<String> getNestedPlaceholders(@NonNull String output) {
        return TAB.getInstance().getPlaceholderManager().detectPlaceholders(output);
    }

    /**
     * An alternative for {@code String#replace} function with better performance.
     * If the input string does not contain string to replace, it is returned immediately.
     * If the input string is equal to text to replace, output is returned directly.
     *
     * @param   string
     *          String to replace text in
     * @param   original
     *          Text to replace
     * @param   replacement
     *          Replacement text
     * @return  Replaced text
     */
    private String replace(@NonNull String string, @NonNull String original, @NonNull String replacement) {
        if (!string.contains(original)) return string;
        if (string.equals(original)) return replacement;
        return string.replace(original, replacement);
    }

    /**
     * Applies all nested placeholders in output
     *
     * @param   text
     *          replaced placeholder
     * @param   p
     *          player to replace for
     * @return  text with replaced placeholders in output
     */
    protected @NotNull String setPlaceholders(@NonNull String text, @Nullable TabPlayer p) {
        if (identifier.equals(text)) return text;
        String replaced = text;
        for (String s : getNestedPlaceholders(text)) {
            if (s.equals(identifier) || (identifier.startsWith("%sync:") && ("%" + identifier.substring(6)).equals(s)) || s.startsWith("%rel_")) continue;
            replaced = TAB.getInstance().getPlaceholderManager().getPlaceholder(s).set(replaced, p);
        }
        return replaced;
    }

    /**
     * Internal method used to mark placeholders as parents who use this placeholder
     * inside their outputs for faster updates.
     *
     * @param   parent
     *          parent placeholder using this placeholder in output
     */
    public void addParent(@NonNull String parent) {
        if (!parents.contains(parent)) parents.add(parent);
    }

    /**
     * Updates all placeholders that use this placeholder
     * as a nested placeholder
     *
     * @param   player
     *          Player to update placeholders for.
     */
    public void updateParents(@NonNull TabPlayer player) {
        if (parents.isEmpty()) return;
        for (String id : parents) {
            TabPlaceholder pl = TAB.getInstance().getPlaceholderManager().getPlaceholder(id);
            pl.updateFromNested(player);
            pl.updateParents(player);
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
     * Returns last known value of defined player without refreshing it if not present.
     *
     * @param   player
     *          player to get value of
     * @return  last known value for specified player or identifier if not available
     */
    @NotNull
    public abstract String getLastValueSafe(@NotNull TabPlayer player);
}