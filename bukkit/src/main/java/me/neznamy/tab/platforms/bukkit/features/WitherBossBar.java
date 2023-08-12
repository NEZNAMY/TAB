package me.neznamy.tab.platforms.bukkit.features;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.api.bossbar.BossBar;
import me.neznamy.tab.shared.features.types.WorldSwitchListener;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.features.bossbar.BossBarManagerImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * An additional class with additional code for &lt;1.9 servers due to an entity being required
 */
@RequiredArgsConstructor
public class WitherBossBar extends BossBarManagerImpl implements Listener, WorldSwitchListener {

    /**
     * Constructs new instance and registers events
     *
     * @param   plugin
     *          Plugin instance to register events
     */
    public WitherBossBar(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void load() {
        //when MC is on fullscreen, BossBar disappears after 1 second of not being seen
        //when in a small window, it's about 100ms
        TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(100,
                featureName, TabConstants.CpuUsageCategory.TELEPORTING_WITHER, this::teleport);
        super.load();
        teleport();
    }

    /**
     * Updates Wither location for all online players
     */
    private void teleport() {
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            if (p.getVersion().getMinorVersion() > 8) continue; //sending VV packets to those
            for (BossBar line : getRegisteredBossBars().values()) {
                if (!line.getPlayers().contains(p)) continue;
                Location eyeLocation = ((BukkitTabPlayer)p).getPlayer().getEyeLocation();
                Location loc = eyeLocation.add(eyeLocation.getDirection().normalize().multiply(60)); // Wither distance
                if (loc.getY() < 1) loc.setY(1);
                ((BackendTabPlayer)p).getEntityView().teleportEntity(line.getUniqueId().hashCode(), new me.neznamy.tab.shared.backend.Location(loc.getX(), loc.getY(), loc.getZ()));
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
        TabPlayer player = TAB.getInstance().getPlayer(e.getPlayer().getUniqueId());
        if (player == null) return;
        TAB.getInstance().getCPUManager().runMeasuredTask(featureName, TabConstants.CpuUsageCategory.PLAYER_RESPAWN,
                () -> detectBossBarsAndSend(player));
    }

    @Override
    public void onWorldChange(@NotNull TabPlayer p, @NotNull String from, @NotNull String to) {
        for (BossBar line : lineValues) {
            line.removePlayer(p);
        }
        detectBossBarsAndSend(p);
    }
}