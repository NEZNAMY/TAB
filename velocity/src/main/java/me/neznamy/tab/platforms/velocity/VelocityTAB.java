package me.neznamy.tab.platforms.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bstats.velocity.Metrics;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * Main class for Velocity.
 */
@Plugin(
        id = TabConstants.PLUGIN_ID,
        name = TabConstants.PLUGIN_NAME,
        version = TabConstants.PLUGIN_VERSION,
        description = TabConstants.PLUGIN_DESCRIPTION,
        url = TabConstants.PLUGIN_WEBSITE,
        authors = TabConstants.PLUGIN_AUTHOR
)
@Getter
public class VelocityTAB {

    /** ProxyServer instance */
    @Inject private ProxyServer server;
    
    /** Metrics factory for bStats */
    @Inject private Metrics.Factory metricsFactory;

    /** Console logger with TAB's prefix */
    @Inject private Logger logger;

    /** Folder for configuration files */
    @Inject @DataDirectory private Path dataFolder;

    /**
     * Initializes plugin for velocity
     *
     * @param   event
     *          velocity initialize event
     */
    @Subscribe
    public void onProxyInitialization(@Nullable ProxyInitializeEvent event) {
        if (!ReflectionUtils.classExists("net.kyori.adventure.text.logger.slf4j.ComponentLogger")) {
            logger.warn(EnumChatFormat.RED + "The plugin requires Velocity build #274 (released on October 12th, 2023) and up to work.");
            return;
        }
        TAB.create(new VelocityPlatform(this));
    }
    
    /**
     * Shutdown event listener that properly disables all features
     * and makes them send unload packets to players.
     *
     * @param   event
     *          proxy disable event
     */
    @Subscribe
    public void onProxyShutdown(@Nullable ProxyShutdownEvent event) {
        if (TAB.getInstance() == null) return;
        // Fix race condition as Velocity calls DisconnectEvent for each player before this event
        TAB.getInstance().getCPUManager().runTask(() -> TAB.getInstance().unload());
    }
}