package me.neznamy.tab.platforms.bungeecord;

import java.util.ArrayList;

import org.bstats.bungeecord.Metrics;
import org.bstats.charts.SimplePie;

import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.api.TabConstants;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;

/**
 * Main class for BungeeCord platform
 */
public class Main extends Plugin {

    @Override
    public void onEnable(){
        if (!isVersionSupported()) {
            getLogger().info(EnumChatFormat.color("&cThe plugin requires BungeeCord build #1637 and up to work. Get it at https://ci.md-5.net/job/BungeeCord/"));
            return;
        }
        BungeePlatform platform = new BungeePlatform();
        ProxyServer.getInstance().registerChannel(TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME);
        TAB.setInstance(new TAB(platform, ProtocolVersion.PROXY, getProxy().getVersion(), getDataFolder(), getLogger()));
        getProxy().getPluginManager().registerListener(this, new BungeeEventListener(platform));
        getProxy().getPluginManager().registerCommand(this, new BTABCommand());
        TAB.getInstance().load();
        Metrics metrics = new Metrics(this, 10535);
        metrics.addCustomChart(new SimplePie(TabConstants.MetricsChart.PERMISSION_SYSTEM, () -> TAB.getInstance().getGroupManager().getPlugin().getName()));
        metrics.addCustomChart(new SimplePie(TabConstants.MetricsChart.GLOBAL_PLAYER_LIST_ENABLED, () -> TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.GLOBAL_PLAYER_LIST) ? "Yes" : "No"));
    }

    /**
     * Checks for compatibility and returns {@code true} if version is supported, {@code false} if not
     *
     * @return  {@code true} if version is compatible, {@code false} if not
     */
    private boolean isVersionSupported() {
        try {
            Class.forName("net.md_5.bungee.protocol.Property");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public void onDisable() {
        if (TAB.getInstance() != null) TAB.getInstance().unload();
    }

    /**
     * TAB command for BungeeCord
     */
    private static class BTABCommand extends Command implements TabExecutor {

        /**
         * Constructs new instance
         */
        public BTABCommand() {
            super("btab", null);
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            if (TAB.getInstance().isPluginDisabled()) {
                for (String message : TAB.getInstance().getDisabledCommand().execute(args, sender.hasPermission(TabConstants.Permission.COMMAND_RELOAD), sender.hasPermission(TabConstants.Permission.COMMAND_ALL))) {
                    sender.sendMessage(new TextComponent(EnumChatFormat.color(message)));
                }
            } else {
                TabPlayer p = null;
                if (sender instanceof ProxiedPlayer) {
                    p = TAB.getInstance().getPlayer(((ProxiedPlayer)sender).getUniqueId());
                    if (p == null) return; //player not loaded correctly
                }
                TAB.getInstance().getCommand().execute(p, args);
            }
        }

        @Override
        public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
            TabPlayer p = null;
            if (sender instanceof ProxiedPlayer) {
                p = TAB.getInstance().getPlayer(((ProxiedPlayer)sender).getUniqueId());
                if (p == null) return new ArrayList<>(); //player not loaded correctly
            }
            return TAB.getInstance().getCommand().complete(p, args);
        }
    }
}