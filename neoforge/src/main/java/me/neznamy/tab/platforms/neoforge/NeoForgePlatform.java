package me.neznamy.tab.platforms.neoforge;

import com.mojang.logging.LogUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.chat.component.KeybindComponent;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.chat.component.TextComponent;
import me.neznamy.chat.component.TranslatableComponent;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.backend.BackendPlatform;
import me.neznamy.tab.shared.features.PerWorldPlayerListConfiguration;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import me.neznamy.tab.shared.platform.BossBar;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

/**
 * Platform implementation for NeoForge
 */
@RequiredArgsConstructor
@Getter
public class NeoForgePlatform implements BackendPlatform {

    /** Minecraft server reference */
    private final MinecraftServer server;

    @Override
    public void registerUnknownPlaceholder(@NotNull String identifier) {
        registerDummyPlaceholder(identifier);
    }

    @Override
    public void loadPlayers() {
        for (ServerPlayer player : getOnlinePlayers()) {
            TAB.getInstance().addPlayer(new NeoForgeTabPlayer(this, player));
        }
    }

    private Collection<ServerPlayer> getOnlinePlayers() {
        // It's nullable on startup
        return server.getPlayerList() == null ? Collections.emptyList() : server.getPlayerList().getPlayers();
    }

    @Override
    @NotNull
    public PipelineInjector createPipelineInjector() {
        return new NeoForgePipelineInjector();
    }

    @Override
    @NotNull
    public TabExpansion createTabExpansion() {
        return new EmptyTabExpansion();
    }

    @Override
    @Nullable
    public TabFeature getPerWorldPlayerList(@NotNull PerWorldPlayerListConfiguration configuration) {
        return null;
    }

    @Override
    public void logInfo(@NotNull TabComponent message) {
	    LogUtils.getLogger().info("[TAB] {}", message.toRawText());
    }

    @Override
    public void logWarn(@NotNull TabComponent message) {
	    LogUtils.getLogger().warn("[TAB] {}", message.toRawText());
    }

    @Override
    @NotNull
    public String getServerVersionInfo() {
        return "[Fabric] " + SharedConstants.getCurrentVersion().getName();
    }

    @Override
    public void registerListener() {
        new NeoForgeEventListener().register();
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
        return FMLPaths.CONFIGDIR.get().resolve(TabConstants.PLUGIN_ID).toFile();
    }

    @Override
    @NotNull
    public Component convertComponent(@NotNull TabComponent component) {
        // Component type
        MutableComponent nmsComponent;
        if (component instanceof TextComponent) {
            nmsComponent = Component.literal(((TextComponent) component).getText());
        } else if (component instanceof TranslatableComponent) {
            nmsComponent = Component.translatable(((TranslatableComponent) component).getKey());
        } else if (component instanceof KeybindComponent) {
            nmsComponent = Component.keybind(((KeybindComponent) component).getKeybind());
        } else {
            throw new IllegalStateException("Unexpected component type: " + component.getClass().getName());
        }

        // Component style
        nmsComponent.setStyle(new Style(
                component.getModifier().getColor() == null ? null : TextColor.fromRgb(component.getModifier().getColor().getRgb()),
                component.getModifier().getShadowColor(),
                component.getModifier().getBold(),
                component.getModifier().getItalic(),
                component.getModifier().getUnderlined(),
                component.getModifier().getStrikethrough(),
                component.getModifier().getObfuscated(),
                null,
                null,
                null,
                component.getModifier().getFont() == null ? null : ResourceLocation.tryParse(component.getModifier().getFont())
        ));

        // Extra
        for (TabComponent extra : component.getExtra()) {
            nmsComponent.getSiblings().add(convertComponent(extra));
        }

        return nmsComponent;
    }

    @Override
    @NotNull
    public Scoreboard createScoreboard(@NotNull TabPlayer player) {
        return new NeoForgeScoreboard((NeoForgeTabPlayer) player);
    }

    @Override
    @NotNull
    public BossBar createBossBar(@NotNull TabPlayer player) {
        return new NeoForgeBossBar((NeoForgeTabPlayer) player);
    }

    @Override
    @NotNull
    public TabList createTabList(@NotNull TabPlayer player) {
        return new NeoForgeTabList((NeoForgeTabPlayer) player);
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
        return (float) server.getAverageTickTimeNanos() / 1000000;
    }
}
