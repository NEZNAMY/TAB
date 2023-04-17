package me.neznamy.tab.platforms.bukkit;

import me.neznamy.tab.shared.hook.ViaVersionHook;
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

    /**
     * Listener to PlayerQuitEvent to remove player data and forward the event to features
     *
     * @param   e
     *          quit event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent e) {
        quit(e.getPlayer().getUniqueId());
    }
    
    /**
     * Listener to PlayerJoinEvent to create player data and forward the event to features
     *
     * @param   e
     *          join event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        join(new BukkitTabPlayer(e.getPlayer(), ViaVersionHook.getInstance().getPlayerVersion(e.getPlayer().getUniqueId(), e.getPlayer().getName())));
    }

    /**
     * Listener to PlayerChangedWorldEvent to forward the event to features
     *
     * @param   e
     *          world changed event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        worldChange(e.getPlayer().getUniqueId(), e.getPlayer().getWorld().getName());
    }

    /**
     * Listener to PlayerChangedWorldEvent to forward the event to features
     *
     * @param   e
     *          command preprocess event
     */
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (command(e.getPlayer().getUniqueId(), e.getMessage())) e.setCancelled(true);
    }
}