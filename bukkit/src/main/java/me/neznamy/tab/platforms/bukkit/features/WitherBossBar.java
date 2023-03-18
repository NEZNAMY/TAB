package me.neznamy.tab.platforms.bukkit.features;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * An additional class with additional code for &lt;1.9 servers due to an entity being required
 */
@RequiredArgsConstructor
public class WitherBossBar extends BossBarManagerImpl implements Listener {

    /** Distance of the Wither in blocks */
    private static final int WITHER_DISTANCE = 60;

    /** Reference to plugin for registering listener */
    private final JavaPlugin plugin;

    @Override
    public void load() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        //when MC is on fullscreen, BossBar disappears after 1 second of not being seen
        //when in a small window, it's about 100ms
        TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(100,
                this, TabConstants.CpuUsageCategory.TELEPORTING_WITHER, this::teleport);
        super.load();
        teleport();
    }

    /**
     * Updates Wither's location for all online players
     */
    private void teleport() {
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            if (p.getVersion().getMinorVersion() > 8) continue; //sending VV packets to those
            for (BossBar line : getRegisteredBossBars().values()) {
                if (!line.containsPlayer(p)) continue;
                Location loc = ((Player) p.getPlayer()).getEyeLocation().add(((Player) p.getPlayer()).getEyeLocation().getDirection().normalize().multiply(WITHER_DISTANCE));
                if (loc.getY() < 1) loc.setY(1);
                ((BackendTabPlayer)p).teleportEntity(line.getUniqueId().hashCode(), new me.neznamy.tab.shared.backend.Location(loc.getX(), loc.getY(), loc.getZ(), 0, 0));
            }
        }
    }
    
    @Override
    public void unload() {
        super.unload();
        HandlerList.unregisterAll(this);
    }
    
    /**
     * Respawning wither as respawn screen destroys all entities in client
     */
    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        TAB.getInstance().getCPUManager().runMeasuredTask(this, TabConstants.CpuUsageCategory.PLAYER_RESPAWN,
                () -> detectBossBarsAndSend(TAB.getInstance().getPlayer(e.getPlayer().getUniqueId())));
    }
}