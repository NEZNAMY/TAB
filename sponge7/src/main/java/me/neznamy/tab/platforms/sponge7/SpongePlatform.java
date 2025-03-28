package me.neznamy.tab.platforms.sponge7;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendPlatform;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.shared.features.PerWorldPlayerListConfiguration;
import me.neznamy.tab.shared.features.injection.PipelineInjector;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.platform.BossBar;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
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

    /** Server version */
    @Getter
    private final ProtocolVersion serverVersion = ProtocolVersion.fromFriendlyName(Sponge.getGame().getPlatform().getMinecraftVersion().getName());

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
    @Nullable
    public TabFeature getPerWorldPlayerList(@NotNull PerWorldPlayerListConfiguration configuration) {
        return null;
    }

    @Override
    public void logInfo(@NotNull TabComponent message) {
        plugin.getLogger().info(message.toLegacyText());
    }

    @Override
    public void logWarn(@NotNull TabComponent message) {
        plugin.getLogger().warn("§c" + message.toLegacyText());
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
                .build(), getCommand());
    }

    @Override
    public void startMetrics() {
        // Not available
    }

    @Override
    @NotNull
    public File getDataFolder() {
        return plugin.getConfigDir();
    }

    @Override
    @NotNull
    public Text convertComponent(@NotNull TabComponent component) {
        return Text.of(component.toLegacyText());
    }

    @Override
    @NotNull
    public Scoreboard createScoreboard(@NotNull TabPlayer player) {
        return new SpongeScoreboard((SpongeTabPlayer) player);
    }

    @Override
    @NotNull
    public BossBar createBossBar(@NotNull TabPlayer player) {
        return new SpongeBossBar((SpongeTabPlayer) player);
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
        return Sponge.getServer().getTicksPerSecond();
    }

    @Override
    public double getMSPT() {
        return -1; // Not available
    }
}
