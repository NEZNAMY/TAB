package me.neznamy.tab.platforms.bukkit;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.shared.TAB;
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
@RequiredArgsConstructor
public class BukkitEventListener implements Listener {

    /** Platform instance */
    private final BukkitPlatform platform;
	
    private final TAB instance = TAB.getInstance();
    
    /**
     * Listener to PlayerQuitEvent to remove player data and forward the event to features
     *
     * @param   e
     *          quit event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent e) {
        if (instance.isPluginDisabled()) return;
        instance.getCPUManager().runTask(() -> instance.getFeatureManager().onQuit(instance.getPlayer(e.getPlayer().getUniqueId())));
    }
    
    /**
     * Listener to PlayerJoinEvent to create player data and forward the event to features
     *
     * @param   e
     *          join event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        if (instance.isPluginDisabled()) return;
        instance.getCPUManager().runTask(() ->
                instance.getFeatureManager().onJoin(new BukkitTabPlayer(e.getPlayer(), platform.getProtocolVersion(e.getPlayer()))));
    }

    /**
     * Listener to PlayerChangedWorldEvent to forward the event to features
     *
     * @param   e
     *          world changed event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        if (instance.isPluginDisabled()) return;
        instance.getCPUManager().runTask(() ->
                instance.getFeatureManager().onWorldChange(e.getPlayer().getUniqueId(), e.getPlayer().getWorld().getName()));
    }

    /**
     * Listener to PlayerChangedWorldEvent to forward the event to features
     *
     * @param   e
     *          command preprocess event
     */
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (TabAPI.getInstance().isPluginDisabled()) return;
        if (TabAPI.getInstance().getFeatureManager().onCommand(
                TabAPI.getInstance().getPlayer(e.getPlayer().getUniqueId()), e.getMessage())) e.setCancelled(true);
    }
}