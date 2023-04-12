package me.neznamy.tab.platforms.sponge8;

import lombok.Getter;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendPlatform;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.features.nametags.NameTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public final class SpongePlatform extends BackendPlatform {

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
    public @Nullable PipelineInjector getPipelineInjector() {
        return null;
    }

    @Override
    public NameTag getUnlimitedNametags() {
        return new NameTag();
    }

    @Override
    public void sendConsoleMessage(String message, boolean translateColors) {
        Sponge.systemSubject().sendMessage(
                Component.text()
                .append(Component.text("[TAB] "))
                .append(LegacyComponentSerializer.legacySection().deserialize(
                        translateColors ? EnumChatFormat.color(message) : message)
                ).build()
        );
    }
}
