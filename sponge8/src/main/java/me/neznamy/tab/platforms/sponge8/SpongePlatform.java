package me.neznamy.tab.platforms.sponge8;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.feature.TabFeature;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.platforms.sponge8.features.PetFix;
import me.neznamy.tab.platforms.sponge8.features.unlimitedtags.SpongeNameTagX;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendPlatform;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.features.nametags.NameTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

@RequiredArgsConstructor
public final class SpongePlatform extends BackendPlatform {

    private final Sponge8TAB plugin;
    @Getter private final PipelineInjector pipelineInjector = new SpongePipelineInjector();
    @Getter private final TabExpansion tabExpansion = new EmptyTabExpansion();
    @Getter private final TabFeature perWorldPlayerlist = null;

    @Override
    public String getPluginVersion(String plugin) {
        return Sponge.pluginManager().plugin(plugin.toLowerCase()).map(container -> container.metadata().version().toString()).orElse(null);
    }

    @Override
    public void registerUnknownPlaceholder(String identifier) {
        TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(identifier, -1, () -> identifier);
    }

    @Override
    public void loadPlayers() {
        for (ServerPlayer player : Sponge.server().onlinePlayers()) {
            TAB.getInstance().addPlayer(new SpongeTabPlayer(player));
        }
    }

    @Override
    public void registerPlaceholders() {
        new SpongePlaceholderRegistry().registerPlaceholders(TAB.getInstance().getPlaceholderManager());
    }

    @Override
    public NameTag getUnlimitedNametags() {
        return new SpongeNameTagX(plugin);
    }

    @Override
    public TabFeature getPetFix() {
        return new PetFix();
    }

    @Override
    public void sendConsoleMessage(String message, boolean translateColors) {
        if (translateColors) message = EnumChatFormat.color(message);
        final Component actualMessage = Component.text()
                .append(Component.text("[TAB] "))
                .append(LegacyComponentSerializer.legacySection().deserialize(message))
                .build();
        Sponge.systemSubject().sendMessage(actualMessage);
    }
}
