package me.neznamy.tab.platforms.sponge7;

import lombok.Getter;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendPlatform;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

public final class SpongePlatform extends BackendPlatform {

    @Getter private final PipelineInjector pipelineInjector = null;
    @Getter private final TabExpansion tabExpansion = new EmptyTabExpansion();
    @Getter private final TabFeature perWorldPlayerlist = null;

    @Override
    public void registerUnknownPlaceholder(@NotNull String identifier) {
        TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(identifier, -1, () -> identifier);
    }

    @Override
    public void loadPlayers() {
        for (Player player : Sponge.getServer().getOnlinePlayers()) {
            TAB.getInstance().addPlayer(new SpongeTabPlayer(player));
        }
    }

    @Override
    public void registerPlaceholders() {
        new SpongePlaceholderRegistry().registerPlaceholders(TAB.getInstance().getPlaceholderManager());
    }

    @Override
    public @NotNull NameTag getUnlimitedNametags() {
        return new NameTag();
    }
}
