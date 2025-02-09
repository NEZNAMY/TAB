package me.neznamy.tab.platforms.fabric;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.chat.component.KeybindComponent;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.chat.component.TextComponent;
import me.neznamy.chat.component.TranslatableComponent;
import me.neznamy.tab.platforms.fabric.hook.FabricTabExpansion;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.backend.BackendPlatform;
import me.neznamy.tab.shared.features.PerWorldPlayerListConfiguration;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

/**
 * Platform implementation for Fabric
 */
@RequiredArgsConstructor
@Getter
public class FabricPlatform implements BackendPlatform {

    /** Minecraft server reference */
    private final MinecraftServer server;

    /** Server version */
    private final ProtocolVersion serverVersion = ProtocolVersion.fromFriendlyName(FabricTAB.minecraftVersion);

    @Override
    public void registerUnknownPlaceholder(@NotNull String identifier) {
        if (!FabricLoader.getInstance().isModLoaded("placeholder-api")) {
            registerDummyPlaceholder(identifier);
            return;
        }

        PlaceholderManagerImpl manager = TAB.getInstance().getPlaceholderManager();
        int refresh = manager.getRefreshInterval(identifier);
        manager.registerPlayerPlaceholder(identifier, refresh,
                p -> Placeholders.parseText(
                            FabricMultiVersion.newTextComponent(identifier),
                            PlaceholderContext.of((ServerPlayer) p.getPlayer())
                        ).getString()
        );
    }

    @Override
    public void loadPlayers() {
        for (ServerPlayer player : getOnlinePlayers()) {
            TAB.getInstance().addPlayer(new FabricTabPlayer(this, player));
        }
    }

    private Collection<ServerPlayer> getOnlinePlayers() {
        // It's nullable on startup
        return server.getPlayerList() == null ? Collections.emptyList() : server.getPlayerList().getPlayers();
    }

    @Override
    @NotNull
    public PipelineInjector createPipelineInjector() {
        return new FabricPipelineInjector();
    }

    @Override
    @NotNull
    public TabExpansion createTabExpansion() {
        if (FabricLoader.getInstance().isModLoaded("placeholder-api"))
            return new FabricTabExpansion();
        return new EmptyTabExpansion();
    }

    @Override
    @Nullable
    public TabFeature getPerWorldPlayerList(@NotNull PerWorldPlayerListConfiguration configuration) {
        return null;
    }

    @Override
    public void logInfo(@NotNull TabComponent message) {
        FabricMultiVersion.logInfo(message);
    }

    @Override
    public void logWarn(@NotNull TabComponent message) {
        FabricMultiVersion.logWarn(message);
    }

    @Override
    @NotNull
    public String getServerVersionInfo() {
        return "[Fabric] " + FabricTAB.minecraftVersion;
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
    public File getDataFolder() {
        return FabricLoader.getInstance().getConfigDir().resolve(TabConstants.PLUGIN_ID).toFile();
    }

    @Override
    @NotNull
    public Component convertComponent(@NotNull TabComponent component) {
        // Component type
        Component nmsComponent;
        if (component instanceof TextComponent) {
            nmsComponent = FabricMultiVersion.newTextComponent(((TextComponent) component).getText());
        } else if (component instanceof TranslatableComponent) {
            nmsComponent = FabricMultiVersion.newTranslatableComponent(((TranslatableComponent) component).getKey());
        } else if (component instanceof KeybindComponent) {
            nmsComponent = FabricMultiVersion.newKeybindComponent(((KeybindComponent) component).getKeybind());
        } else {
            throw new IllegalStateException("Unexpected component type: " + component.getClass().getName());
        }

        // Component style
        FabricMultiVersion.setStyle(nmsComponent, FabricMultiVersion.convertModifier(component.getModifier()));

        // Extra
        for (TabComponent extra : component.getExtra()) {
            FabricMultiVersion.addSibling(nmsComponent, convertComponent(extra));
        }

        return nmsComponent;
    }

    @Override
    @NotNull
    public Scoreboard createScoreboard(@NotNull TabPlayer player) {
        return new FabricScoreboard((FabricTabPlayer) player);
    }

    @Override
    @NotNull
    public TabList createTabList(@NotNull TabPlayer player) {
        return new FabricTabList((FabricTabPlayer) player);
    }

    @Override
    public boolean supportsNumberFormat() {
        return serverVersion.getNetworkId() >= ProtocolVersion.V1_20_3.getNetworkId();
    }

    @Override
    public boolean supportsListOrder() {
        return serverVersion.getNetworkId() >= ProtocolVersion.V1_21_2.getNetworkId();
    }

    @Override
    public boolean supportsScoreboards() {
        return true;
    }

    @Override
    public double getTPS() {
        double mspt = getMSPT();
        if (mspt < 50) return 20;
        return Math.round(1000 / mspt);
    }

    @Override
    public double getMSPT() {
        return FabricMultiVersion.getMSPT(server);
    }
}
