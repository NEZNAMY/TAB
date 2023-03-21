package me.neznamy.tab.platforms.bukkit.bossbar;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.BossBarHandler;
import me.neznamy.tab.api.chat.rgb.RGBUtils;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.shared.TAB;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Bossbar for 1.9+ servers where Bukkit API is used. If ViaVersion is used
 * to allow 1.8 players, it will handle the entity and teleporting by itself.
 */
@RequiredArgsConstructor
public class BukkitBossBar1_9 implements BossBarHandler {

    /** Player this handler belongs to */
    private final BukkitTabPlayer player;

    /** Bukkit BossBars the player can currently see */
    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    @Override
    public void create(@NonNull UUID id, @NonNull String title, float progress, me.neznamy.tab.api.bossbar.@NonNull BarColor color, me.neznamy.tab.api.bossbar.@NonNull BarStyle style) {
        if (bossBars.containsKey(id)) return;
        BossBar bar = Bukkit.createBossBar(
                RGBUtils.getInstance().convertToBukkitFormat(title,
                        player.getVersion().getMinorVersion() >= 16 && TAB.getInstance().getServerVersion().getMinorVersion() >= 16),
                org.bukkit.boss.BarColor.valueOf(color.name()),
                org.bukkit.boss.BarStyle.valueOf(style.getBukkitName()));
        bar.setProgress(progress);
        bar.addPlayer(player.getPlayer());
        bossBars.put(id, bar);
    }

    @Override
    public void update(@NonNull UUID id, @NonNull String title) {
        bossBars.get(id).setTitle(RGBUtils.getInstance().convertToBukkitFormat(title,
                player.getVersion().getMinorVersion() >= 16 && TAB.getInstance().getServerVersion().getMinorVersion() >= 16));
    }

    @Override
    public void update(@NonNull UUID id, float progress) {
        bossBars.get(id).setProgress(progress);
    }

    @Override
    public void update(@NonNull UUID id, me.neznamy.tab.api.bossbar.@NonNull BarStyle style) {
        bossBars.get(id).setStyle(org.bukkit.boss.BarStyle.valueOf(style.getBukkitName()));
    }

    @Override
    public void update(@NonNull UUID id, me.neznamy.tab.api.bossbar.@NonNull BarColor color) {
        bossBars.get(id).setColor(org.bukkit.boss.BarColor.valueOf(color.name()));
    }

    @Override
    public void remove(@NonNull UUID id) {
        bossBars.remove(id).removePlayer(player.getPlayer());
    }
}
