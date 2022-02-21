package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;

/**
 * The event listener part for securing proper functionality of armor stands
 */
public class EventListener implements Listener {

    //the NameTag feature handler
    private final BukkitNameTagX feature;

    /**
     * Constructs new instance with given parameters
     * @param feature - NameTag feature handler
     */
    public EventListener(BukkitNameTagX feature) {
        this.feature = feature;
    }

    /**
     * Sneak event listener to de-spawn and spawn armor stands to skip animation
     * @param e - sneak event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSneak(PlayerToggleSneakEvent e) {
        TabPlayer p = TAB.getInstance().getPlayer(e.getPlayer().getUniqueId());
        if (p == null || feature.isPlayerDisabled(p)) return;
        TAB.getInstance().getCPUManager().runMeasuredTask(feature, TabConstants.CpuUsageCategory.PLAYER_SNEAK, () -> feature.getArmorStandManager(p).sneak(e.isSneaking()));
    }

    /**
     * Respawning armor stands as respawn screen destroys all entities in client
     * @param e - respawn event
     */
    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        TAB.getInstance().getCPUManager().runMeasuredTask(feature, TabConstants.CpuUsageCategory.PLAYER_RESPAWN, () -> {
            TabPlayer respawned = TAB.getInstance().getPlayer(e.getPlayer().getUniqueId());
            if (feature.isPlayerDisabled(respawned)) return;
            feature.getArmorStandManager(respawned).teleport();
        });
    }
}