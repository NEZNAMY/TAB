package me.neznamy.tab.platforms.sponge7;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
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
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.io.File;

/**
 * Platform implementation for Sponge 7 and lower
 */
@RequiredArgsConstructor
public class SpongePlatform implements BackendPlatform {

    /** Plugin reference */
    @NotNull
    private final Sponge7TAB plugin;

    @Override
    public void registerUnknownPlaceholder(@NotNull String identifier) {
        registerDummyPlaceholder(identifier);
    }

    @Override
    public void loadPlayers() {
        for (Player player : Sponge.getServer().getOnlinePlayers()) {
            TAB.getInstance().addPlayer(new SpongeTabPlayer(this, player));
        }
    }

    @Override
    @Nullable
    public PipelineInjector createPipelineInjector() {
        return null;
    }

    @Override
    @NotNull
    public NameTag getUnlimitedNameTags() {
        return new NameTag();
    }

    @Override
    @NotNull
    public TabExpansion createTabExpansion() {
        return new EmptyTabExpansion();
    }

    @Override
    @Nullable
    public TabFeature getPerWorldPlayerList() {
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
    @NotNull
    public String getServerVersionInfo() {
        return "[Sponge] " + Sponge.getPlatform().getMinecraftVersion().getName();
    }

    @Override
    public void registerListener() {
        Sponge.getGame().getEventManager().registerListeners(plugin, new SpongeEventListener());
    }

    @Override
    public void registerCommand() {
        SpongeTabCommand cmd = new SpongeTabCommand();
        Sponge.getGame().getCommandManager().register(plugin, CommandSpec.builder()
                .arguments(cmd, GenericArguments.remainingJoinedStrings(Text.of("arguments"))) // GenericArguments.none() doesn't work, so rip no-arg
                .executor(cmd)
                .build(), TabConstants.COMMAND_BACKEND);
    }

    @Override
    public void startMetrics() {
        // Not available
    }

    @Override
    @NotNull
    public ProtocolVersion getServerVersion() {
        return ProtocolVersion.fromFriendlyName(Sponge.getGame().getPlatform().getMinecraftVersion().getName());
    }

    @Override
    @NotNull
    public File getDataFolder() {
        return plugin.getConfigDir();
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
