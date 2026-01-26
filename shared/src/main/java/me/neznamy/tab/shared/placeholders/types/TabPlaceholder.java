package me.neznamy.tab.shared.placeholders.types;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.placeholder.Placeholder;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.placeholders.PlaceholderReplacementPattern;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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

    /** Placeholder's identifier including % */
    @NonNull protected final String identifier;

    /** Configured placeholder output replacements */
    @NonNull protected final PlaceholderReplacementPattern replacements;

    /**
     * List of placeholders using this placeholder as a nested placeholder,
     * mutual tracking allows faster parent placeholder changes when a nested
     * placeholder changed value.
     */
    protected final List<TabPlaceholder> parents = new ArrayList<>();

    private final List<TabPlaceholder> children = new ArrayList<>();

    /** Set of features using this placeholder, used to call refresh on them */
    private final Set<RefreshableFeature> usedByFeatures = Collections.synchronizedSet(new HashSet<>());

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
        Map<Object, Object> map = TAB.getInstance().getConfiguration().getConfig().getReplacements().getValues().get(identifier);
        replacements = map == null ? PlaceholderReplacementPattern.EMPTY : PlaceholderReplacementPattern.create(identifier, map);
        for (String nested : replacements.getNestedPlaceholders()) {
            TAB.getInstance().getPlaceholderManager().getPlaceholder(nested).addParent(this);
        }
    }

    /**
     * Parses the placeholder for defined player, applying all nested placeholders
     * found in output.
     *
     * @param   player
     *          player to parse placeholder for
     * @return  parsed placeholder with all nested placeholders applied
     */
    @NotNull
    public String parse(@Nullable TabPlayer player) {
        String value = getLastValue(player);
        if (!value.contains("%")) return value; // No nested placeholders, return immediately
        return setPlaceholders(value, player);
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

        // Known children first for better performance thanks to skipped checks
        for (TabPlaceholder child : children) {
            if (string.contains(child.identifier)) {
                string = string.replace(child.identifier, child.parse(player));
            }
        }

        for (String s : PlaceholderManagerImpl.detectPlaceholders(string)) {
            if (s.equals(identifier)) continue; // Prevent infinite loop when placeholder returns itself
            if (s.startsWith("%rel_")) continue; // Relational placeholders are handled separately
            if ((identifier.startsWith("%sync:") && ("%" + identifier.substring(6)).equals(s))) continue; // Self, but as sync variant
            TabPlaceholder nested = TAB.getInstance().getPlaceholderManager().getPlaceholder(s);
            nested.addParent(this);
            addChild(nested);
            string = string.replace(s, nested.parse(player));
        }
        return string;
    }

    /**
     * Internal method used to mark placeholders as parents who use this placeholder
     * inside their outputs for faster updates.
     *
     * @param   parent
     *          parent placeholder using this placeholder in output
     */
    public void addParent(@NonNull TabPlaceholder parent) {
        if (parent == this) return; // ???
        if (!parents.contains(parent)) {
            usedByFeatures.addAll(parent.usedByFeatures);
            parents.add(parent);
        }
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
        for (TabPlaceholder pl : new ArrayList<>(parents)) {
            pl.updateFromNested(player);
            pl.updateParents(player);
        }
    }

    /**
     * Registers a feature that is using this placeholder, to allow
     * refreshing it when this placeholder changes.
     *
     * @param   feature
     *          Feature using this placeholder
     * @return  true if feature was not already registered
     */
    public boolean addUsedFeature(@NonNull RefreshableFeature feature) {
        boolean added = usedByFeatures.add(feature);
        if (added) {
            for (TabPlaceholder child : children) {
                child.addUsedFeature(feature);
            }
        }
        return added;
    }

    public void addChild(@NonNull TabPlaceholder child) {
        if (!children.contains(child)) {
            children.add(child);
            for (RefreshableFeature feature : usedByFeatures) {
                child.addUsedFeature(feature);
            }
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