package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

/**
 * Class listening to Bukkit events which the feature requires and are
 * sufficient, without packets being requires.
 */
@RequiredArgsConstructor
public class EventListener implements Listener {

    /** Reference to the main feature */
    private final BukkitNameTagX feature;

    /**
     * Sneak event listener to de-spawn and spawn armor stands to skip animation
     *
     * @param   e
     *          sneak event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSneak(PlayerToggleSneakEvent e) {
        TabPlayer p = TabAPI.getInstance().getPlayer(e.getPlayer().getUniqueId());
        if (p == null || feature.isPlayerDisabled(p)) return;
        TAB.getInstance().getCPUManager().runMeasuredTask(feature, TabConstants.CpuUsageCategory.PLAYER_SNEAK, () -> feature.getArmorStandManager(p).sneak(e.isSneaking()));
    }

    /**
     * Respawning armor stands as respawn screen destroys all entities in client
     *
     * @param   e
     *          respawn event
     */
    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        TAB.getInstance().getCPUManager().runMeasuredTask(feature, TabConstants.CpuUsageCategory.PLAYER_RESPAWN, () -> {
            TabPlayer respawned = TabAPI.getInstance().getPlayer(e.getPlayer().getUniqueId());
            if (feature.isPlayerDisabled(respawned)) return;
            feature.getArmorStandManager(respawned).teleport();
        });
    }
}