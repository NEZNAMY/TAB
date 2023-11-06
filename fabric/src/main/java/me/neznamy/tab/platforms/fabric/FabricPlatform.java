package me.neznamy.tab.platforms.fabric;

import me.neznamy.tab.platforms.fabric.features.FabricNameTagX;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.backend.BackendPlatform;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Platform implementation for Fabric
 */
public record FabricPlatform(MinecraftServer server) implements BackendPlatform {

    @Override
    public void registerUnknownPlaceholder(@NotNull String identifier) {
        registerDummyPlaceholder(identifier);
    }

    @Override
    public void loadPlayers() {
        for (ServerPlayer player : PlayerLookup.all(server)) {
            TAB.getInstance().addPlayer(new FabricTabPlayer(this, player));
        }
    }

    @Override
    @NotNull
    public PipelineInjector createPipelineInjector() {
        return new FabricPipelineInjector();
    }

    @Override
    @NotNull
    public NameTag getUnlimitedNameTags() {
        return new FabricNameTagX();
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
        MinecraftServer.LOGGER.info("[TAB] " + message.toRawText());
    }

    @Override
    public void logWarn(@NotNull IChatBaseComponent message) {
        MinecraftServer.LOGGER.warn("[TAB] " + message.toRawText()); // Fabric console does not support colors
    }

    @Override
    @NotNull
    public String getServerVersionInfo() {
        return "[Fabric] " + SharedConstants.getCurrentVersion().getName();
    }

    @Override
    public void registerListener() {
        new FabricEventListener().register();
    }

    @Override
    public void registerCommand() {
        // Event listener must be registered in main class
    }

    @Override
    public void startMetrics() {
        // Not available
    }

    @Override
    @NotNull
    public ProtocolVersion getServerVersion() {
        return ProtocolVersion.fromFriendlyName(SharedConstants.getCurrentVersion().getName());
    }

    @Override
    @NotNull
    public File getDataFolder() {
        return FabricLoader.getInstance().getConfigDir().resolve(TabConstants.PLUGIN_ID).toFile();
    }

    /**
     * Converts internal component class to platform's component class
     *
     * @param   component
     *          Component to convert
     * @param   version
     *          Game version to convert component for
     * @return  Converted component
     */
    public Component toComponent(@NotNull IChatBaseComponent component, @NotNull ProtocolVersion version) {
        return Component.Serializer.fromJson(component.toString(version));
    }

    @Override
    public double getTPS() {
        return -1; // Not available
    }

    @Override
    public double getMSPT() {
        return server.getAverageTickTime();
    }
}
