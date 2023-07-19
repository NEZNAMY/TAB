package me.neznamy.tab.platforms.fabric;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendPlatform;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.SharedConstants;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class FabricPlatform implements BackendPlatform {

    @NotNull private final MinecraftServer server;

    @Override
    public void registerUnknownPlaceholder(@NotNull String identifier) {
        TAB.getInstance().getPlaceholderManager().registerServerPlaceholder(identifier, -1, () -> identifier);
    }

    @Override
    public void loadPlayers() {
        for (ServerPlayer player : PlayerLookup.all(FabricTAB.getInstance().getServer())) {
            TAB.getInstance().addPlayer(new FabricTabPlayer(player));
        }
    }

    @Override
    public @NotNull NameTag getUnlimitedNameTags() {
        return new NameTag();
    }

    @Override
    public @Nullable PipelineInjector createPipelineInjector() {
        return null;
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
        MinecraftServer.LOGGER.info("[TAB] " + message.toRawText());
    }

    @Override
    public void logWarn(@NotNull IChatBaseComponent message) {
        MinecraftServer.LOGGER.warn("[TAB] " + message.toRawText()); // Fabric console does not support colors
    }

    @Override
    public String getServerVersionInfo() {
        return "[Fabric] " + SharedConstants.getCurrentVersion().getName();
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
