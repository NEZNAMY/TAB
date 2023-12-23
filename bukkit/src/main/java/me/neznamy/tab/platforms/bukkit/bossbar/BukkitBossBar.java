package me.neznamy.tab.platforms.bukkit.bossbar;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.platforms.bukkit.BukkitUtils;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.BossBar;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * BossBar for 1.9+ servers where Bukkit API is used. If ViaVersion is used
 * to allow 1.8 players, it will handle the entity and teleporting by itself.
 */
@RequiredArgsConstructor
public class BukkitBossBar implements BossBar {

    /** Style array for fast access */
    private static final org.bukkit.boss.BarStyle[] styles = org.bukkit.boss.BarStyle.values();
    
    /** Player this handler belongs to */
    @NotNull
    private final BukkitTabPlayer player;

    /** Bukkit BossBars the player can currently see */
    @NotNull
    private final Map<UUID, org.bukkit.boss.BossBar> bossBars = new HashMap<>();

    @Override
    public void create(@NotNull UUID id, @NotNull String title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
        if (bossBars.containsKey(id)) return;
        org.bukkit.boss.BossBar bar = Bukkit.createBossBar(
                BukkitUtils.toBukkitFormat(IChatBaseComponent.optimizedComponent(title), player.getVersion().supportsRGB()),
                org.bukkit.boss.BarColor.valueOf(color.name()),
                styles[style.ordinal()]
        );
        bar.setProgress(progress);
        bar.addPlayer(player.getPlayer());
        bossBars.put(id, bar);
    }

    @Override
    public void update(@NotNull UUID id, @NotNull String title) {
        bossBars.get(id).setTitle(BukkitUtils.toBukkitFormat(IChatBaseComponent.optimizedComponent(title), player.getVersion().supportsRGB()));
    }

    @Override
    public void update(@NotNull UUID id, float progress) {
        bossBars.get(id).setProgress(progress);
    }

    @Override
    public void update(@NotNull UUID id, @NotNull BarStyle style) {
        bossBars.get(id).setStyle(styles[style.ordinal()]);
    }

    @Override
    public void update(@NotNull UUID id, @NotNull BarColor color) {
        bossBars.get(id).setColor(org.bukkit.boss.BarColor.valueOf(color.name()));
    }

    @Override
    public void remove(@NotNull UUID id) {
        bossBars.remove(id).removePlayer(player.getPlayer());
    }
}
