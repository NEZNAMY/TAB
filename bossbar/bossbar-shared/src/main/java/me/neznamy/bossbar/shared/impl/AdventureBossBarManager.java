package me.neznamy.bossbar.shared.impl;

import me.neznamy.bossbar.shared.SafeBossBarManager;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import org.jetbrains.annotations.NotNull;

/**
 * Shared BossBar implementation using Adventure API.
 */
public class AdventureBossBarManager extends SafeBossBarManager<BossBar> {

    /**
     * Constructs new instance for given player.
     *
     * @param   player
     *          Player this Boss bar will belong to
     */
    public AdventureBossBarManager(@NotNull Object player) { // Object because otherwise error is thrown if adventure is not available and this class is imported
        super(player);
    }

    @Override
    @NotNull
    public BossBar constructBossBar(@NotNull TabComponent title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
        return BossBar.bossBar(title.toAdventure(), progress, Color.valueOf(color.name()), Overlay.valueOf(style.name()));
    }

    @Override
    public void create(@NotNull BossBarInfo bar) {
        ((Audience)player).showBossBar(bar.getBossBar());
    }

    @Override
    public void updateTitle(@NotNull BossBarInfo bar) {
        bar.getBossBar().name(bar.getTitle().toAdventure());
    }

    @Override
    public void updateProgress(@NotNull BossBarInfo bar) {
        bar.getBossBar().progress(bar.getProgress());
    }

    @Override
    public void updateStyle(@NotNull BossBarInfo bar) {
        bar.getBossBar().overlay(Overlay.valueOf(bar.getStyle().name()));
    }

    @Override
    public void updateColor(@NotNull BossBarInfo bar) {
        bar.getBossBar().color(Color.valueOf(bar.getColor().name()));
    }

    @Override
    public void remove(@NotNull BossBarInfo bar) {
        ((Audience)player).hideBossBar(bar.getBossBar());
    }
}
