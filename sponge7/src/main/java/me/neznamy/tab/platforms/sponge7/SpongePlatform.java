package me.neznamy.tab.platforms.sponge7;

import lombok.Getter;
import me.neznamy.tab.api.feature.TabFeature;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendPlatform;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;

public final class SpongePlatform extends BackendPlatform {

    @Getter private final PipelineInjector pipelineInjector = null;
    @Getter private final TabExpansion tabExpansion = new EmptyTabExpansion();
    @Getter private final TabFeature petFix = null;
    @Getter private final TabFeature perWorldPlayerlist = null;

    @Override
    public String getPluginVersion(String plugin) {
        return Sponge.getPluginManager().getPlugin(plugin.toLowerCase()).flatMap(PluginContainer::getVersion).orElse(null);
    }

    @Override
    public void registerUnknownPlaceholder(String identifier) {
        TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(identifier, -1, () -> identifier);
    }

    @Override
    public void loadPlayers() {
        for (final Player player : Sponge.getServer().getOnlinePlayers()) {
            TAB.getInstance().addPlayer(new SpongeTabPlayer(player));
        }
    }

    @Override
    public void registerPlaceholders() {
        new SpongePlaceholderRegistry().registerPlaceholders(TAB.getInstance().getPlaceholderManager());
    }

    @Override
    public NameTag getUnlimitedNametags() {
        return new NameTag();
    }
}
