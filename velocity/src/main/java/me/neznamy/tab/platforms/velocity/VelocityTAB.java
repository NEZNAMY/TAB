package me.neznamy.tab.platforms.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import lombok.Getter;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import org.bstats.charts.SimplePie;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * Main class for Velocity platform.
 * The velocity-plugin.json is manually deleted from releases and therefore
 * requires manual compilation. This avoids unnecessary complications
 * and bug reports from an unsupported platform.
 * Most of the reasons why Velocity is not and will not be supported are listed at
 * https://gist.github.com/NEZNAMY/21c1aabe57a0a462ee175386c510fdf8
 */
@Plugin(
        id = TabConstants.PLUGIN_ID,
        name = TabConstants.PLUGIN_NAME,
        version = TabConstants.PLUGIN_VERSION,
        description = TabConstants.PLUGIN_DESCRIPTION,
        url = TabConstants.PLUGIN_WEBSITE,
        authors = {TabConstants.PLUGIN_AUTHOR}
)
public class VelocityTAB {

    /** ProxyServer instance */
    @Inject @Getter private ProxyServer server;
    
    /** Metrics factory for bStats */
    @Inject private Metrics.Factory metricsFactory;

    /** Console logger with TAB's prefix */
    @Inject @Getter private Logger logger;

    /** Folder for configuration files */
    @Inject @DataDirectory Path dataFolder;

    /** Plugin message channel */
    @Getter private static final MinecraftChannelIdentifier minecraftChannelIdentifier =
            MinecraftChannelIdentifier.from(TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME);

    /**
     * Initializes plugin for velocity
     *
     * @param   event
     *          velocity initialize event
     */
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getCommandManager().register(server.getCommandManager().metaBuilder(TabConstants.COMMAND_PROXY).build(), new VelocityTabCommand());
        server.getChannelRegistrar().register(minecraftChannelIdentifier);
        server.getEventManager().register(this, new VelocityEventListener());
        TAB.setInstance(new TAB(new VelocityPlatform(this, server), ProtocolVersion.PROXY, server.getVersion().getVersion(), dataFolder.toFile()));
        TAB.getInstance().load();
        metricsFactory.make(this, 10533).addCustomChart(new SimplePie(TabConstants.MetricsChart.GLOBAL_PLAYER_LIST_ENABLED,
                () -> TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.GLOBAL_PLAYER_LIST) ? "Yes" : "No"));
    }
    
    /**
     * Shutdown event listener that properly disables all features
     * and makes them send unload packets to players.
     *
     * @param   event
     *          proxy disable event
     */
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        TAB.getInstance().unload();
    }
}