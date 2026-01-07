package me.neznamy.tab.shared.features.layout.pattern;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Pattern for a fixed slot in layout. It contains: <p>
 * - Slot number <p>
 * - Text to display <p>
 * - Skin to use (optional) <p>
 * - Ping to display (optional)
 */
@Getter
@RequiredArgsConstructor
public class FixedSlotPattern {

    /** Slot to display this fixed slot in (1-80) */
    private final int slot;

    /** Text to display in raw format */
    @NotNull
    private final String text;

    /** Skin to use in raw format for placeholder support */
    @Nullable
    private final String skin;

    /** Ping to display in raw format, {@code null} means using global empty slot ping value */
    @Nullable
    private final String ping;
}