package me.neznamy.tab.platforms.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import lombok.Getter;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.shared.TAB;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bstats.charts.SimplePie;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main class for Velocity platform.
 * The velocity-plugin.json file creation is disabled by default and
 * requires manual compilation. This avoids unnecessary complications
 * and bug reports from an unsupported platform.
 */
//@com.velocitypowered.api.plugin.Plugin(id = "tab", name = "TAB", version = TabConstants.PLUGIN_VERSION, description = "An all-in-one solution that works", authors = {"NEZNAMY"})
public class Main {

    /** Plugin instance */
    @Getter private static Main instance;

    /** ProxyServer instance */
    @Inject @Getter private ProxyServer server;
    
    /** Metrics factory for bStats */
    @Inject
    private Metrics.Factory metricsFactory;

    /** Console logger with TAB's prefix */
    @Inject
    private Logger logger;

    /** TAB's plugin message channel */
    @Getter private final MinecraftChannelIdentifier minecraftChannelIdentifier = MinecraftChannelIdentifier.create(
            TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME.split(":")[0], TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME.split(":")[1]);

    /** Component cache for 1.16+ players to save CPU when creating components */
    private final Map<IChatBaseComponent, Component> componentCacheModern = new HashMap<>();

    /** Component cache for 1.15- players to save CPU when creating components */
    private final Map<IChatBaseComponent, Component> componentCacheLegacy = new HashMap<>();
    
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
        TAB.setInstance(new TAB(platform, ProtocolVersion.PROXY, server.getVersion().getVersion(), new File("plugins" + File.separatorChar + "TAB"), logger));
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
     * Converts TAB's component class into adventure component.
     * Currently, the only way of conversion is string serialization / deserialization.
     * Manual conversion for better performance might be added in the future.
     * If the entered component is {@code null}, returns {@code null}
     *
     * @param   component
     *          Component to convert
     * @param   clientVersion
     *          Version of player to convert for
     * @return  Converted component or {@code null} if {@code component} is {@code null}
     */
    public Component convertComponent(IChatBaseComponent component, ProtocolVersion clientVersion) {
        if (component == null) return null;
        return clientVersion.getMinorVersion() >= 16 ? fromCache(componentCacheModern, component, clientVersion) : fromCache(componentCacheLegacy, component, clientVersion);
    }

    /**
     * Loads component's adventure version from cache if present. If not, it is created,
     * inserted into cache and returned.
     *
     * @param   map
     *          Cache to load component from / save component into
     * @param   component
     *          Component to convert
     * @param   clientVersion
     *          Player version to convert component for
     * @return  Converted component
     */
    private Component fromCache(Map<IChatBaseComponent, Component> map, IChatBaseComponent component, ProtocolVersion clientVersion) {
        if (map.containsKey(component)) return map.get(component);
        Component obj = GsonComponentSerializer.gson().deserialize(component.toString(clientVersion));
        if (map.size() > 10000) map.clear();
        map.put(component, obj);
        return obj;
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