package me.neznamy.tab.platforms.fabric;

import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.placeholders.UniversalPlaceholderRegistry;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class FabricPlaceholderRegistry extends UniversalPlaceholderRegistry {

    @Override
    public void registerPlaceholders(@NotNull PlaceholderManager manager) {
        manager.registerPlayerPlaceholder(TabConstants.Placeholder.HEALTH, 100, p -> (int) Math.ceil(((ServerPlayer) p.getPlayer()).getHealth()));
        super.registerPlaceholders(manager);
    }
}
