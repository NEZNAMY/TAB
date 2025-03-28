package me.neznamy.tab.platforms.sponge8;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.backend.BackendPlatform;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.shared.features.PerWorldPlayerListConfiguration;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.platform.BossBar;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.impl.AdventureBossBar;
import net.kyori.adventure.text.Component;
import org.bstats.charts.SimplePie;
import org.bstats.sponge.Metrics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.io.File;

/**
 * Platform implementation for Sponge 8 and up.
 */
@RequiredArgsConstructor
public class SpongePlatform implements BackendPlatform {

    /** Main class reference */
    @NotNull
    private final Sponge8TAB plugin;

    /** Server version */
    @Getter
    private final ProtocolVersion serverVersion = ProtocolVersion.fromFriendlyName(Sponge.game().platform().minecraftVersion().name());

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
    @Nullable
    public TabFeature getPerWorldPlayerList(@NotNull PerWorldPlayerListConfiguration configuration) {
        return null;
    }

    @Override
    public void logInfo(@NotNull TabComponent message) {
        Sponge.systemSubject().sendMessage(Component.text("[TAB] ").append(message.toAdventure()));
    }

    @Override
    public void logWarn(@NotNull TabComponent message) {
        Sponge.systemSubject().sendMessage(Component.text("[TAB] [WARN] ").append(message.toAdventure()));
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
        metrics.addCustomChart(new SimplePie(TabConstants.MetricsChart.SERVER_VERSION, serverVersion::getFriendlyName));
    }

    @Override
    @NotNull
    public File getDataFolder() {
        return plugin.getConfigDir().toFile();
    }

    @Override
    @NotNull
    public Component convertComponent(@NotNull TabComponent component) {
        return component.toAdventure();
    }

    @Override
    @NotNull
    public Scoreboard createScoreboard(@NotNull TabPlayer player) {
        return new SpongeScoreboard((SpongeTabPlayer) player);
    }

    @Override
    @NotNull
    public BossBar createBossBar(@NotNull TabPlayer player) {
        return new AdventureBossBar(player);
    }

    @Override
    @NotNull
    public TabList createTabList(@NotNull TabPlayer player) {
        return new SpongeTabList((SpongeTabPlayer) player);
    }

    @Override
    public boolean supportsScoreboards() {
        return true;
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
