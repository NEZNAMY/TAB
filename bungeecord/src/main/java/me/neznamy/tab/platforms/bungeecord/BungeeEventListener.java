package me.neznamy.tab.platforms.bungeecord;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.platform.EventListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;

/**
 * The core for BungeeCord forwarding events into all enabled features
 */
public class BungeeEventListener extends EventListener<ProxiedPlayer> implements Listener {

    @EventHandler
    public void onQuit(PlayerDisconnectEvent e) {
        quit(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onSwitch(ServerSwitchEvent e) {
        Runnable task = () -> serverChange(e.getPlayer(), e.getPlayer().getUniqueId(), e.getPlayer().getServer().getInfo().getName());
        // Temporary workaround until https://github.com/SpigotMC/BungeeCord/issues/3542 is resolved
        if (e.getPlayer().getPendingConnection().getVersion() >= ProtocolVersion.V1_20_2.getNetworkId()) {
            TAB.getInstance().getCPUManager().runTaskLater(500, "", "", task);
        } else {
            task.run();
        }
    }

    @EventHandler
    public void onCommand(ChatEvent e) {
        if (e.isCommand() && command(((ProxiedPlayer)e.getSender()).getUniqueId(), e.getMessage())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void on(PluginMessageEvent e) {
        if (!e.getTag().equals(TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME)) return;
        if (e.getReceiver() instanceof ProxiedPlayer) {
            e.setCancelled(true);
            pluginMessage(((ProxiedPlayer) e.getReceiver()).getUniqueId(), e.getData());
        }
    }

    @Override
    @NotNull
    public TabPlayer createPlayer(@NotNull ProxiedPlayer player) {
        return new BungeeTabPlayer((BungeePlatform) TAB.getInstance().getPlatform(), player);
    }
}