package me.neznamy.tab.platforms.sponge;

import java.text.NumberFormat;
import java.util.Locale;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

public final class SpongePlaceholderRegistry extends UniversalPlaceholderRegistry {

    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);

    public SpongePlaceholderRegistry() {
        numberFormat.setMaximumFractionDigits(2);
    }

    @Override
    public void registerPlaceholders(PlaceholderManager manager) {
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.DISPLAY_NAME, 500,
                p -> ((Player) p.getPlayer()).getDisplayNameData().displayName().get().toPlain());
        manager.registerServerPlaceholder(TabConstants.Placeholder.TPS, 1000, () -> formatTPS(Sponge.getServer().getTicksPerSecond()));
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.HEALTH, 100, p -> (int) Math.ceil(((Player) p.getPlayer()).health().get()));
        super.registerPlaceholders(manager);
    }

    private String formatTPS(final double tps) {
        return numberFormat.format(Math.min(20, tps));
    }
}
