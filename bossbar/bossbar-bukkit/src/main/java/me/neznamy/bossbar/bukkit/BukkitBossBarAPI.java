package me.neznamy.bossbar.bukkit;

import com.viaversion.viaversion.api.Via;
import lombok.NonNull;
import lombok.SneakyThrows;
import me.neznamy.bossbar.shared.BossBarAPI;
import me.neznamy.bossbar.shared.SafeBossBarManager;
import me.neznamy.bossbar.shared.impl.AdventureBossBarManager;
import me.neznamy.bossbar.shared.impl.DummyBossBarManager;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * BossBarAPI implementation for Bukkit.
 */
public class BukkitBossBarAPI extends BossBarAPI<Player> {

    /** Flag tracking whether this server is 1.9+ or not */
    private static final boolean ABOVE_1_9 = classExists("org.bukkit.boss.BossBar");

    /** Flag tracking whether adventure bossbar is available on the server */
    @SuppressWarnings("ConstantValue")
    private static final boolean ADVENTURE_AVAILABLE = classExists("net.kyori.adventure.bossbar.BossBar") && Audience.class.isAssignableFrom(Player.class);

    /** Flag tracking whether ViaVersion is installed or not */
    private static final boolean VIA_INSTALLED = classExists("com.viaversion.viaversion.api.Via");

    private static boolean classExists(@NotNull String path) {
        try {
            Class.forName(path);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    @NotNull
    @SneakyThrows
    public SafeBossBarManager<?> createBossBarManager(@NonNull Player player) {
        // Adventure supports everything, including fonts, and has better performance
        if (ADVENTURE_AVAILABLE) return new AdventureBossBarManager(player);

        // 1.9+ server, handle using API, potential 1.8 players are handled by ViaVersion
        if (ABOVE_1_9) return new BukkitBossBarManager(player);

        // 1.9+ player on 1.8 server, handle using ViaVersion API
        if (VIA_INSTALLED && Via.getAPI().getPlayerVersion(player.getUniqueId()) >= 107) return new ViaBossBarManager(player);

        // 1.8- server and player, no implementation
        return new DummyBossBarManager(player);
    }
}
