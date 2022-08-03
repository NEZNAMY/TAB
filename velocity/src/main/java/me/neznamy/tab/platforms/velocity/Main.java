package me.neznamy.tab.platforms.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.platforms.velocity.storage.VelocityPacketStorage;
import me.neznamy.tab.shared.TAB;
import net.kyori.adventure.identity.Identity;
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
 * Main class for Velocity platform
 */
@Plugin(id = "tab", name = "TAB", version = TabConstants.PLUGIN_VERSION, description = "An all-in-one solution that works",
        authors = {"NEZNAMY"}, dependencies = {@Dependency(id = "velocitypacketinjector")})
public class Main {

    /** Plugin instance */
    private static Main instance;

    /** ProxyServer instance */
    @Inject
    private ProxyServer server;

    /** Metrics factory for bStats */
    @Inject
    private Metrics.Factory metricsFactory;

    /** Console logger with TAB's prefix */
    @Inject
    private Logger logger;

    /** TAB's plugin message channel */
    private final MinecraftChannelIdentifier mc = MinecraftChannelIdentifier.create(
            TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME.split(":")[0], TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME.split(":")[1]);

    /** Component cache for 1.16+ players to save CPU when creating components */
    private final Map<IChatBaseComponent, Component> componentCacheModern = new HashMap<>();

    /** Component cache for 1.15- players to save CPU when creating components */
    private final Map<IChatBaseComponent, Component> componentCacheLegacy = new HashMap<>();

    /**
     * Initializes plugin for velocity
     *
     * @param   event
     *          velocity initialize event
     */
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        if (!isVersionSupported()) {
            return;
        }
        VelocityPlatform platform = new VelocityPlatform();
        instance = this;
        if (server.getConfiguration().isOnlineMode()) {
            logger.info(EnumChatFormat.color("&6If you experience tablist prefix/suffix not working and global playerlist duplicating players, toggle "
                    + "\"use-online-uuid-in-tablist\" option in config.yml (set it to opposite value)."));
        }
        server.getChannelRegistrar().register(mc);
        TAB.setInstance(new TAB(platform, ProtocolVersion.PROXY, server.getVersion().getVersion(), new File("plugins" + File.separatorChar + "TAB"), logger));
        server.getEventManager().register(this, new VelocityEventListener(platform));
        server.getCommandManager().register(server.getCommandManager().metaBuilder("btab").aliases("vtab").build(), new VelocityTABCommand());
        TAB.getInstance().load();
        Metrics metrics = metricsFactory.make(this, 10533);
        metrics.addCustomChart(new SimplePie(TabConstants.MetricsChart.GLOBAL_PLAYER_LIST_ENABLED, () -> TabAPI.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.GLOBAL_PLAYER_LIST) ? "Yes" : "No"));
    }

    /**
     * Returns instance of the plugin
     *
     * @return  instance of the plugin
     */
    public static Main getInstance() {
        return instance;
    }

    /**
     * Returns instance of the proxy server
     *
     * @return  ProxyServer instance
     */
    public ProxyServer getServer() {
        return server;
    }

    /**
     * Checks for compatibility and returns true if version is supported, false if not
     *
     * @return  true if version is compatible, false if not
     */
    private boolean isVersionSupported() {
        try {
            long time = System.currentTimeMillis();
            VelocityPacketStorage.setInstance(new VelocityPacketStorage());
            logger.info(EnumChatFormat.color("&7Loaded packet hook in " + (System.currentTimeMillis() - time) + "ms"));
            return true;
        } catch (Exception e) {
            logger.error("Failed to load packet hook! Make sure you are using a supported Velocity version and have VelocityPacketInjector installed.", e);
            return false;
        }
    }

    /**
     * Returns TAB's plugin message channel
     *
     * @return  TAB's plugin message channel
     */
    public MinecraftChannelIdentifier getMinecraftChannelIdentifier() {
        return mc;
    }

    /**
     * Unloads the plugin
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
     *
     * @param   component
     *          Component to convert
     * @param   clientVersion
     *          Version of player to convert for
     * @return  Converted component
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
     *          Cache to load / save component
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
     * TAB's command
     */
    private static class VelocityTABCommand implements SimpleCommand {

        @Override
        public void execute(Invocation invocation) {
            CommandSource sender = invocation.source();
            if (TabAPI.getInstance().isPluginDisabled()) {
                for (String message : TAB.getInstance().getDisabledCommand().execute(invocation.arguments(), sender.hasPermission(TabConstants.Permission.COMMAND_RELOAD), sender.hasPermission(TabConstants.Permission.COMMAND_ALL))) {
                    sender.sendMessage(Identity.nil(), Component.text(EnumChatFormat.color(message)));
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