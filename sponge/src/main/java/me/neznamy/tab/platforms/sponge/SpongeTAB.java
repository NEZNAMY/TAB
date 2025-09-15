package me.neznamy.tab.platforms.sponge;

import com.google.inject.Inject;
import lombok.Getter;
import me.neznamy.tab.shared.ProjectVariables;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import org.apache.logging.log4j.Logger;
import org.bstats.sponge.Metrics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.nio.file.Path;

/**
 * Main class for Sponge.
 */
@Getter
@Plugin(ProjectVariables.PLUGIN_ID)
public class SpongeTAB {

    @Inject @ConfigDir(sharedRoot = false) private Path configDir;
    @Inject private PluginContainer container;
    @Inject private Metrics.Factory metricsFactory;
    @Inject private Logger logger;

    /**
     * Enables the plugin.
     *
     * @param   event
     *          Server start event
     */
    @Listener
    public void onServerStart(@Nullable StartingEngineEvent<Server> event) {
        ProtocolVersion serverVersion = ProtocolVersion.fromNetworkId(Sponge.platform().minecraftVersion().protocolVersion());
        if (serverVersion.getNetworkId() < ProtocolVersion.V1_20_6.getNetworkId()) {
            logger.warn("====================================================================================================");
            logger.warn("This plugin version was made for Minecraft version 1.20.6 and above.");
            logger.warn("====================================================================================================");
            return;
        }
        TAB.create(new SpongePlatform(this));
    }

    /**
     * Registers plugin's command.
     *
     * @param   event
     *          Event to register command to
     */
    @Listener
    public void onRegisterCommands(@NotNull RegisterCommandEvent<Command.Raw> event) {
        event.register(container, new SpongeTabCommand(), "tab"); // TODO extract it from Platform somehow
    }

    /**
     * Disables the plugin.
     *
     * @param   event
     *          Server stop event
     */
    @Listener
    public void onServerStop(@Nullable StoppingEngineEvent<Server> event) {
        if (TAB.getInstance() != null) TAB.getInstance().unload();
    }
}
