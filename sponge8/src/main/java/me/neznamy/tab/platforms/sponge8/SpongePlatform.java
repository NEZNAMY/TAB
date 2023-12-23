package me.neznamy.tab.platforms.sponge8;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.backend.BackendPlatform;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.nametags.NameTag;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.hook.AdventureHook;
import me.neznamy.tab.shared.placeholders.expansion.EmptyTabExpansion;
import me.neznamy.tab.shared.placeholders.expansion.TabExpansion;
import net.kyori.adventure.text.Component;
import org.bstats.charts.SimplePie;
import org.bstats.sponge.Metrics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.io.File;

/**
 * Platform implementation for Sponge 8 and up
 */
@RequiredArgsConstructor
public class SpongePlatform implements BackendPlatform {

    /** Main class reference */
    @NotNull
    private final Sponge8TAB plugin;

    @Override
    public void registerUnknownPlaceholder(@NotNull String identifier) {
        registerDummyPlaceholder(identifier);
    }

    @Override
    public void loadPlayers() {
        for (ServerPlayer player : Sponge.server().onlinePlayers()) {
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
        Sponge.systemSubject().sendMessage(Component.text("[TAB] ").append(
                AdventureHook.toAdventureComponent(message, TAB.getInstance().getServerVersion())));
    }

    @Override
    public void logWarn(@NotNull IChatBaseComponent message) {
        Sponge.systemSubject().sendMessage(Component.text("[TAB] [WARN] ").append(
                AdventureHook.toAdventureComponent(message, TAB.getInstance().getServerVersion()))); // Sponge console does not support colors
    }

    @Override
    @NotNull
    public String getServerVersionInfo() {
        return "[Sponge] " + Sponge.platform().minecraftVersion().name();
    }

    @Override
    public void registerListener() {
        Sponge.game().eventManager().registerListeners(plugin.getContainer(), new SpongeEventListener());
    }

    @Override
    public void registerCommand() {
        // Must be registered in main class event listener
    }

    @Override
    public void startMetrics() {
        Metrics metrics = plugin.getMetricsFactory().make(TabConstants.BSTATS_PLUGIN_ID_SPONGE);
        metrics.startup(null);
        metrics.addCustomChart(new SimplePie(TabConstants.MetricsChart.SERVER_VERSION,
                () -> TAB.getInstance().getServerVersion().getFriendlyName()));
    }

    @Override
    @NotNull
    public ProtocolVersion getServerVersion() {
        return ProtocolVersion.fromFriendlyName(Sponge.game().platform().minecraftVersion().name());
    }

    @Override
    @NotNull
    public File getDataFolder() {
        return plugin.getConfigDir().toFile();
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
