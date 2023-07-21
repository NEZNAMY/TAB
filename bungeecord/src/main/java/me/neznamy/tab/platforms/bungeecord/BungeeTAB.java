package me.neznamy.tab.platforms.bungeecord;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;
import org.bstats.charts.SimplePie;

/**
 * Main class for BungeeCord.
 */
public class BungeeTAB extends Plugin {

    @Override
    public void onEnable() {
        if (!ReflectionUtils.classExists("net.md_5.bungee.protocol.packet.PlayerListItemUpdate")) {
            getLogger().info(EnumChatFormat.color("&cThe plugin requires BungeeCord build #1671 and up (or an equivalent fork) to work."));
            return;
        }
        getProxy().registerChannel(TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME);
        getProxy().getPluginManager().registerListener(this, new BungeeEventListener());
        getProxy().getPluginManager().registerCommand(this, new BungeeTabCommand());
        TAB.setInstance(new TAB(new BungeePlatform(this), ProtocolVersion.PROXY, getDataFolder()));
        TAB.getInstance().load();
        new Metrics(this, 10535).addCustomChart(new SimplePie(TabConstants.MetricsChart.GLOBAL_PLAYER_LIST_ENABLED, () -> TAB.getInstance().getFeatureManager().isFeatureEnabled(TabConstants.Feature.GLOBAL_PLAYER_LIST) ? "Yes" : "No"));
    }

    @Override
    public void onDisable() {
        if (TAB.getInstance() != null) TAB.getInstance().unload();
    }
}