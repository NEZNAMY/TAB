package me.neznamy.tab.platforms.bukkit.bossbar;

import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.shared.platform.BossBar;
import org.jetbrains.annotations.NotNull;

/**
 * Class for finding BossBar implementation based on server and client version.
 */
public class BossBarLoader {

    /**
     * Finds best available instance for given player.
     *
     * @param   player
     *          Player to find instance for
     * @return  BossBar instance for player
     */
    @NotNull
    public static BossBar findInstance(@NotNull BukkitTabPlayer player) {
        // 1.9+ server, handle using API, potential 1.8 players are handled by ViaVersion
        if (BukkitReflection.getMinorVersion() >= 9) return new BukkitBossBar(player);

        // 1.9+ player on 1.8 server, handle using ViaVersion API
        if (player.getVersion().getMinorVersion() >= 9) return new ViaBossBar(player);

        // 1.8- server and player, use wither
        return new EntityBossBar(player);
    }
}
