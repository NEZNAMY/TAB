package me.neznamy.tab.platforms.sponge8;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendPlatform;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

/**
 * Platform implementation for Sponge 8 and up
 */
public class SpongePlatform implements BackendPlatform {

    @Override
    public void registerUnknownPlaceholder(@NotNull String identifier) {
        TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(identifier, -1, () -> identifier);
    }

    @Override
    public void loadPlayers() {
        for (ServerPlayer player : Sponge.server().onlinePlayers()) {
            TAB.getInstance().addPlayer(new SpongeTabPlayer(player));
        }
    }

    @Override
    public @Nullable PipelineInjector createPipelineInjector() {
        return null;
    }

    @Override
    public @NotNull NameTag getUnlimitedNameTags() {
        return new NameTag();
    }

    @Override
    public @NotNull TabExpansion createTabExpansion() {
        return new EmptyTabExpansion();
    }

    @Override
    public @Nullable TabFeature getPerWorldPlayerList() {
        return null;
    }

    @Override
    public void logInfo(@NotNull IChatBaseComponent message) {
        Sponge.systemSubject().sendMessage(Component.text("[TAB] ").append(
                message.toAdventureComponent(TAB.getInstance().getServerVersion())));
    }

    @Override
    public void logWarn(@NotNull IChatBaseComponent message) {
        Sponge.systemSubject().sendMessage(Component.text("[TAB] [WARN] ").append(
                message.toAdventureComponent(TAB.getInstance().getServerVersion()))); // Sponge console does not support colors
    }

    @Override
    public String getServerVersionInfo() {
        return "[Sponge] " + Sponge.platform().minecraftVersion().name();
    }

    @Override
    public double getTPS() {
        return Sponge.server().ticksPerSecond();
    }

    @Override
    public double getMSPT() {
        return Sponge.server().averageTickTime();
    }
}
