package me.neznamy.tab.platforms.sponge8;

import com.google.inject.Inject;
import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import org.bstats.sponge.Metrics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Server;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.plugin.PluginContainer;

import java.nio.file.Path;

@Getter
public class Sponge8TAB {

    @Inject @ConfigDir(sharedRoot = false) private Path configDir;
    @Inject private PluginContainer container;
    @Inject private Metrics.Factory metricsFactory;

    @Listener
    public void onServerStart(@Nullable StartingEngineEvent<Server> event) {
        TAB.create(new SpongePlatform(this));
    }

    @Listener
    public void onRegisterCommands(@NotNull RegisterCommandEvent<Command.Raw> event) {
        event.register(container, new SpongeTabCommand(), TabConstants.COMMAND_BACKEND);
    }

    @Listener
    public void onServerStop(@Nullable StoppingEngineEvent<Server> event) {
        TAB.getInstance().unload();
    }
}
