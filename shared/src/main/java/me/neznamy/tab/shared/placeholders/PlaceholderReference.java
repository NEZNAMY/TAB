package me.neznamy.tab.shared.placeholders;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.neznamy.tab.shared.placeholders.types.TabPlaceholder;
import org.jetbrains.annotations.NotNull;

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
     * Returns refresh interval of the placeholder.
     *
     * @return  refresh interval of the placeholder
     */
    public int getRefresh() {
        return handle.getRefresh();
    }
}
