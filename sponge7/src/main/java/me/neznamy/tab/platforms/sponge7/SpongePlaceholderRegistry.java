package me.neznamy.tab.platforms.sponge7;

import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

public final class SpongePlaceholderRegistry extends UniversalPlaceholderRegistry {

    @Override
    public void registerPlaceholders(PlaceholderManager manager) {
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.DISPLAY_NAME, 500,
                p -> ((Player) p.getPlayer()).getDisplayNameData().displayName().get().toPlain());
        manager.registerServerPlaceholder(TabConstants.Placeholder.TPS, 1000, () -> formatTPS(Sponge.getServer().getTicksPerSecond()));
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.HEALTH, 100, p -> (int) Math.ceil(((Player) p.getPlayer()).health().get()));
        super.registerPlaceholders(manager);
    }
}
