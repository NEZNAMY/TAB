package me.neznamy.tab.platforms.bukkit;

import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.EventListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

/**
 * The core for bukkit forwarding events into all enabled features
 */
public class BukkitEventListener extends EventListener<Player> implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent e) {
        quit(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent e) {
        join(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        worldChange(e.getPlayer().getUniqueId(), e.getPlayer().getWorld().getName());
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (command(e.getPlayer().getUniqueId(), e.getMessage())) e.setCancelled(true);
    }

    @Override
    @NotNull
    public TabPlayer createPlayer(@NotNull Player player) {
        return new BukkitTabPlayer((BukkitPlatform) TAB.getInstance().getPlatform(), player);
    }
}
