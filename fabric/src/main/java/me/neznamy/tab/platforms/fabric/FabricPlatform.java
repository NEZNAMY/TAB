package me.neznamy.tab.platforms.fabric;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.platforms.fabric.hook.FabricTabExpansion;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.backend.BackendPlatform;
import me.neznamy.tab.shared.chat.*;
import me.neznamy.tab.shared.features.PerWorldPlayerListConfiguration;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.platform.BossBar;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.fabricmc.loader.api.FabricLoader;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

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
    public Component convertComponent(@NotNull TabComponent component, boolean modern) {
        if (component instanceof SimpleComponent component1) {
            return FabricMultiVersion.newTextComponent(component1.getText());
        }
        if (component instanceof StructuredComponent component1) {
            Component nmsComponent = FabricMultiVersion.newTextComponent(component1.getText());
            FabricMultiVersion.setStyle(nmsComponent, FabricMultiVersion.convertModifier(component1.getModifier()));
            for (StructuredComponent extra : component1.getExtra()) {
                FabricMultiVersion.addSibling(nmsComponent, convertComponent(extra, modern));
            }
            return nmsComponent;
        }
        if (component instanceof AdventureComponent) {
            return fromAdventure(((AdventureComponent) component).getComponent());
        }
        throw new UnsupportedOperationException("Unknown component implementation: " + component.getClass().getName());
    }

    @NotNull
    private Component fromAdventure(@NotNull net.kyori.adventure.text.Component component) {
        Component nmsComponent;
        if (component instanceof TextComponent) {
            nmsComponent = FabricMultiVersion.newTextComponent(((TextComponent) component).content());
        } else if (component instanceof TranslatableComponent) {
            nmsComponent = FabricMultiVersion.newTranslatableComponent(((TranslatableComponent)component).key());
        } else if (component instanceof KeybindComponent) {
            nmsComponent = FabricMultiVersion.newKeybindComponent(((KeybindComponent)component).keybind());
        } else {
            throw new IllegalStateException("Cannot convert " + component.getClass().getName());
        }

        net.kyori.adventure.text.format.TextColor color = component.color();
        Key font = component.style().font();
        Map<TextDecoration, TextDecoration.State> decorations = component.style().decorations();
        FabricMultiVersion.setStyle(nmsComponent, FabricMultiVersion.convertModifier(new ChatModifier(
                color == null ? null : new TextColor(color.red(), color.green(), color.blue()),
                getDecoration(decorations.get(TextDecoration.BOLD)),
                getDecoration(decorations.get(TextDecoration.ITALIC)),
                getDecoration(decorations.get(TextDecoration.UNDERLINED)),
                getDecoration(decorations.get(TextDecoration.STRIKETHROUGH)),
                getDecoration(decorations.get(TextDecoration.OBFUSCATED)),
                font == null ? null : font.asString()
        )));
        for (net.kyori.adventure.text.Component extra : component.children()) {
            FabricMultiVersion.addSibling(nmsComponent, fromAdventure(extra));
        }
        return nmsComponent;
    }

    @Nullable
    private Boolean getDecoration(@Nullable TextDecoration.State state) {
        if (state == null || state == TextDecoration.State.NOT_SET) return null;
        return state == TextDecoration.State.TRUE;
    }

    @Override
    @NotNull
    public Scoreboard createScoreboard(@NotNull TabPlayer player) {
        return new FabricScoreboard((FabricTabPlayer) player);
    }

    @Override
    @NotNull
    public BossBar createBossBar(@NotNull TabPlayer player) {
        return new FabricBossBar((FabricTabPlayer) player);
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
