package me.neznamy.bossbar.bukkit;

import me.neznamy.bossbar.shared.SafeBossBarManager;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * BossBar for 1.9+ servers where Bukkit API is used. If ViaVersion is used
 * to allow 1.8 players, it will handle the entity and teleporting by itself.
 */
public class BukkitBossBarManager extends SafeBossBarManager<BossBar> {

    private static final boolean RGB_SUPPORT = Integer.parseInt(Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1]) >= 16;

    /** Style array because names do not match */
    private static final org.bukkit.boss.BarStyle[] styles = org.bukkit.boss.BarStyle.values();

    /**
     * Constructs new instance for given player.
     *
     * @param   player
     *          Player this Boss bar will belong to
     */
    public BukkitBossBarManager(@NotNull Player player) {
        super(player);
    }

    @Override
    @NotNull
    public BossBar constructBossBar(@NotNull TabComponent title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
        BossBar bar = Bukkit.createBossBar(
                title.toBukkitFormat(RGB_SUPPORT),
                org.bukkit.boss.BarColor.valueOf(color.name()),
                styles[style.ordinal()]
        );
        bar.setProgress(progress);
        return bar;
    }

    @Override
    public void create(@NotNull SafeBossBarManager<BossBar>.BossBarInfo bar) {
        bar.getBossBar().addPlayer((Player) player);
    }

    @Override
    public void updateTitle(@NotNull SafeBossBarManager<BossBar>.BossBarInfo bar) {
        bar.getBossBar().setTitle(bar.getTitle().toBukkitFormat(RGB_SUPPORT));
    }

    @Override
    public void updateProgress(@NotNull SafeBossBarManager<BossBar>.BossBarInfo bar) {
        bar.getBossBar().setProgress(bar.getProgress());
    }

    @Override
    public void updateStyle(@NotNull SafeBossBarManager<BossBar>.BossBarInfo bar) {
        bar.getBossBar().setStyle(styles[bar.getStyle().ordinal()]);
    }

    @Override
    public void updateColor(@NotNull SafeBossBarManager<BossBar>.BossBarInfo bar) {
        bar.getBossBar().setColor(org.bukkit.boss.BarColor.valueOf(bar.getColor().name()));
    }

    @Override
    public void remove(@NotNull SafeBossBarManager<BossBar>.BossBarInfo bar) {
        bar.getBossBar().removePlayer((Player) player);
    }
}
