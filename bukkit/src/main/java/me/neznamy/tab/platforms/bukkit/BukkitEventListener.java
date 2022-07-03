package me.neznamy.tab.platforms.bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.neznamy.tab.shared.TAB;

/**
 * The core for bukkit forwarding events into all enabled features
 */
public class BukkitEventListener implements Listener {

    /** Platform instance */
    private final BukkitPlatform platform;

    /**
     * Constructs new instance with given parameter
     *
     * @param   platform
     *          Platform instance
     */
    public BukkitEventListener(BukkitPlatform platform) {
        this.platform = platform;
    }
    
    /**
     * Listener to PlayerQuitEvent to remove player data and forward the event to features
     *
     * @param   e
     *          quit event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent e){
        if (TAB.getInstance().isDisabled()) return;
        TAB.getInstance().getCPUManager().runTask(() -> TAB.getInstance().getFeatureManager().onQuit(TAB.getInstance().getPlayer(e.getPlayer().getUniqueId())));
    }
    
    /**
     * Listener to PlayerJoinEvent to create player data and forward the event to features
     *
     * @param   e
     *          join event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        if (TAB.getInstance().isDisabled()) return;
        TAB.getInstance().getCPUManager().runTask(() -> TAB.getInstance().getFeatureManager().onJoin(new BukkitTabPlayer(e.getPlayer(), platform.getProtocolVersion(e.getPlayer()))));
    }

    /**
     * Listener to PlayerChangedWorldEvent to forward the event to features
     *
     * @param   e
     *          world changed event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldChange(PlayerChangedWorldEvent e){
        if (TAB.getInstance().isDisabled()) return;
        TAB.getInstance().getCPUManager().runTask(() -> TAB.getInstance().getFeatureManager().onWorldChange(e.getPlayer().getUniqueId(), e.getPlayer().getWorld().getName()));
    }

    /**
     * Listener to PlayerChangedWorldEvent to forward the event to features
     *
     * @param   e
     *          command preprocess event
     */
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (TAB.getInstance().isDisabled()) return;
        if (TAB.getInstance().getFeatureManager().onCommand(TAB.getInstance().getPlayer(e.getPlayer().getUniqueId()), e.getMessage())) e.setCancelled(true);
    }
}