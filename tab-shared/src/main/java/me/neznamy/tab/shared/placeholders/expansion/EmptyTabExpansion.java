package me.neznamy.tab.shared.placeholders.expansion;

import lombok.NonNull;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Dummy implementation when expansion is disabled or not supported by platform
 */
public class EmptyTabExpansion implements TabExpansion {

    @Override
    public void setRawPropertyValue(@NotNull TabPlayer player, @NotNull String property, @NotNull String value) {
        // Do nothing
    }

    @Override
    public void setPlaceholderValue(@NotNull TabPlayer player, @NotNull String placeholder, @NotNull String value) {
        // Do nothing
    }

    @Override
    public void setValue(@NonNull TabPlayer player, @NonNull String key, @NonNull String value) {/*Do nothing*/}

    @Override
    public void unregisterExpansion() {/* Do nothing */}
}
