package me.neznamy.tab.platforms.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import lombok.Getter;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.shared.TAB;
import net.kyori.adventure.text.Component;
import org.bstats.charts.SimplePie;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Main class for Velocity platform.
 * The velocity-plugin.json file creation is disabled by default and
 * requires manual compilation. This avoids unnecessary complications
 * and bug reports from an unsupported platform.
 */
@Plugin(
        id = TabConstants.PLUGIN_ID,
        name = TabConstants.PLUGIN_NAME,
        version = TabConstants.PLUGIN_VERSION,
        description = TabConstants.PLUGIN_DESCRIPTION,
        url = TabConstants.PLUGIN_WEBSITE,
        authors = {"NEZNAMY"}
)
public class Main {

    /** Plugin instance */
    @Getter private static Main instance;

    /** ProxyServer instance */
    @Inject @Getter private ProxyServer server;
    
    /** Metrics factory for bStats */
    @Inject private Metrics.Factory metricsFactory;

    /** Console logger with TAB's prefix */
    @Inject private Logger logger;

    /** Folder for configuration files */
    @Inject @DataDirectory Path dataFolder;

    /** Plugin message channel */
    @Getter private final MinecraftChannelIdentifier minecraftChannelIdentifier = MinecraftChannelIdentifier.create(
            TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME.split(":")[0], TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME.split(":")[1]);

    /** Platform implementation for velocity */
    @Getter private final VelocityPlatform platform = new VelocityPlatform();

    /**
     * Initializes plugin for velocity
     *
     * @param   event
     *          velocity initialize event
     */
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;
        if (server.getConfiguration().isOnlineMode()) {
            logger.info(EnumChatFormat.color("&6If you experience tablist prefix/suffix not working and global playerlist duplicating players, toggle "
                    + "\"use-online-uuid-in-tablist\" option in config.yml (set it to opposite value)."));
        }
        server.getChannelRegistrar().register(minecraftChannelIdentifier);
        TAB.setInstance(new TAB(platform, ProtocolVersion.PROXY, server.getVersion().getVersion(), dataFolder.toFile(), logger));
        server.getEventManager().register(this, new VelocityEventListener());
        VelocityTABCommand cmd = new VelocityTABCommand();
        server.getCommandManager().register(server.getCommandManager().metaBuilder("btab").build(), cmd);
        server.getCommandManager().register(server.getCommandManager().metaBuilder("vtab").build(), cmd);
        TAB.getInstance().load();
        Metrics metrics = metricsFactory.make(this, 10533);
        metrics.addCustomChart(new SimplePie(TabConstants.MetricsChart.GLOBAL_PLAYER_LIST_ENABLED, () -> TabAPI.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.GLOBAL_PLAYER_LIST) ? "Yes" : "No"));
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

    /**
     * TAB's main command for operating with the plugin
     */
    private static class VelocityTABCommand implements SimpleCommand {

        @Override
        public void execute(Invocation invocation) {
            CommandSource sender = invocation.source();
            if (TabAPI.getInstance().isPluginDisabled()) {
                for (String message : TAB.getInstance().getDisabledCommand().execute(invocation.arguments(), sender.hasPermission(TabConstants.Permission.COMMAND_RELOAD), sender.hasPermission(TabConstants.Permission.COMMAND_ALL))) {
                    sender.sendMessage(Component.text(EnumChatFormat.color(message)));
                }
            } else {
                TabPlayer p = null;
                if (sender instanceof Player) {
                    p = TabAPI.getInstance().getPlayer(((Player)sender).getUniqueId());
                    if (p == null) return; //player not loaded correctly
                }
                TAB.getInstance().getCommand().execute(p, invocation.arguments());
            }
        }

        @Override
        public List<String> suggest(Invocation invocation) {
            TabPlayer p = null;
            if (invocation.source() instanceof Player) {
                p = TabAPI.getInstance().getPlayer(((Player)invocation.source()).getUniqueId());
                if (p == null) return new ArrayList<>(); //player not loaded correctly
            }
            return TAB.getInstance().getCommand().complete(p, invocation.arguments());
        }
    }
}