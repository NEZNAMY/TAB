package me.neznamy.tab.platforms.bukkit;

import me.neznamy.tab.shared.platform.PlatformEventListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * The core for bukkit forwarding events into all enabled features
 */
public class BukkitEventListener extends PlatformEventListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent e) {
        quit(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        join(new BukkitTabPlayer(e.getPlayer()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        worldChange(e.getPlayer().getUniqueId(), e.getPlayer().getWorld().getName());
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (command(e.getPlayer().getUniqueId(), e.getMessage())) e.setCancelled(true);
    }
}