package me.neznamy.tab.shared.backend;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * Placeholder registry for backend platforms containing placeholders
 * they all offer in common.
 */
@RequiredArgsConstructor
public class BackendPlaceholderRegistry extends UniversalPlaceholderRegistry {

    /** Platform reference to take TPS from */
    private final BackendPlatform platform;

    @Override
    public void registerPlaceholders(@NotNull PlaceholderManager manager) {
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.HEALTH, 100,
                p -> (int) Math.ceil(((BackendTabPlayer)p).getHealth()));
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.DISPLAY_NAME, 500,
                p -> ((BackendTabPlayer)p).getDisplayName());
        manager.registerServerPlaceholder(TabConstants.Placeholder.TPS, 1000,
                () -> formatTPS(platform.getTPS()));
        manager.registerServerPlaceholder(TabConstants.Placeholder.MSPT, 1000,
                () -> format(platform.getMSPT()));
        super.registerPlaceholders(manager);
    }
}
