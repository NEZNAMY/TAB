package me.neznamy.tab.platforms.sponge7;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendPlatform;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

/**
 * Platform implementation for Sponge 7 and lower
 */
@RequiredArgsConstructor
public class SpongePlatform implements BackendPlatform {

    /** Plugin reference */
    private final Sponge7TAB plugin;

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
        plugin.getLogger().info(message.toLegacyText());
    }

    @Override
    public void logWarn(@NotNull IChatBaseComponent message) {
        plugin.getLogger().warn(EnumChatFormat.RED.getFormat() + message.toLegacyText());
    }

    @Override
    public String getServerVersionInfo() {
        return "[Sponge] " + Sponge.getPlatform().getMinecraftVersion().getName();
    }

    @Override
    public double getTPS() {
        return Sponge.getServer().getTicksPerSecond();
    }

    @Override
    public double getMSPT() {
        return -1; // Not available
    }
}
