package me.neznamy.tab.shared.placeholders;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.placeholders.types.TabPlaceholder;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class holds a reference to a placeholder.
 * Placeholder implementations may change at runtime, breaking all existing
 * pointers to the placeholder objects. This class is used to hold a stable
 * reference to a placeholder that will always point to the correct implementation.
 * 2 examples of why this is needed are:
 * - Conditions - Sides are parsed and placeholder references created,
 *                even before they were fully registered (typically another condition).
 * - API - Registering a placeholder via API occurs after plugin (re)loads, already creating
 *         placeholder references (using PlaceholderAPI implementation) in various features.
 */
@AllArgsConstructor
@Getter
@Setter
public class PlaceholderReference {

    /** Identifier of the placeholder */
    @NotNull
    private final String identifier;

    /** Current implementation of the placeholder by this identifier */
    @NotNull
    private TabPlaceholder handle;

    /**
     * List of placeholders using this placeholder as a nested placeholder,
     * mutual tracking allows faster parent placeholder changes when a nested
     * placeholder changed value.
     */
    private final List<PlaceholderReference> parents = new ArrayList<>();

    /** Set of features using this placeholder, used to call refresh on them */
    private final Set<RefreshableFeature> usedByFeatures = Collections.synchronizedSet(new HashSet<>());

    /**
     * Returns refresh interval of the placeholder.
     *
     * @return  refresh interval of the placeholder
     */
    public int getRefresh() {
        return handle.getRefresh();
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
        return usedByFeatures.add(feature);
    }

    /**
     * Internal method used to mark placeholders as parents who use this placeholder
     * inside their outputs for faster updates.
     *
     * @param   parent
     *          parent placeholder using this placeholder in output
     */
    public void addParent(@NonNull PlaceholderReference parent) {
        if (parent == this) return; // ???
        if (!parents.contains(parent)) {
            usedByFeatures.addAll(parent.usedByFeatures);
            parents.add(parent);
        }
    }
}
