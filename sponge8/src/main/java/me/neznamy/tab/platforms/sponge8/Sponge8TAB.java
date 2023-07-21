package me.neznamy.tab.platforms.sponge8;

import com.google.inject.Inject;
import lombok.Getter;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import org.bstats.charts.SimplePie;
import org.bstats.sponge.Metrics;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.plugin.PluginContainer;

import java.nio.file.Path;

public class Sponge8TAB {

    @Inject @Getter private Game game;
    @Inject @ConfigDir(sharedRoot = false) private Path configDir;
    @Inject private PluginContainer container;
    private final Metrics metrics;

    @Inject
    public Sponge8TAB(Metrics.Factory metricsFactory) {
        this.metrics = metricsFactory.make(17732);
    }

    @Listener
    public void onServerStart(StartingEngineEvent<Server> event) {
        game.eventManager().registerListeners(container, new SpongeEventListener());
        TAB.setInstance(new TAB(new SpongePlatform(), ProtocolVersion.fromFriendlyName(game.platform().minecraftVersion().name()), configDir.toFile()));
        TAB.getInstance().load();
        metrics.addCustomChart(new SimplePie(TabConstants.MetricsChart.UNLIMITED_NAME_TAG_MODE_ENABLED,
                () -> TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.UNLIMITED_NAME_TAGS) ? "Yes" : "No"));
        metrics.addCustomChart(new SimplePie(TabConstants.MetricsChart.SERVER_VERSION, () -> TAB.getInstance().getServerVersion().getFriendlyName()));
    }

    @Listener
    public void onRegisterCommands(RegisterCommandEvent<Command.Raw> event) {
        event.register(container, new SpongeTabCommand(), TabConstants.COMMAND_BACKEND);
    }

    @Listener
    public void onServerStop(StoppingEngineEvent<Server> event) {
        TAB.getInstance().unload();
    }
}
