package me.neznamy.tab.shared.platform.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.SafeBossBar;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import org.jetbrains.annotations.NotNull;

/**
 * Shared BossBar implementation using Adventure API.
 */
@RequiredArgsConstructor
public class AdventureBossBar extends SafeBossBar<BossBar> {

    /** Flag tracking whether this implementation is available on the server or not */
    @Getter
    private static final boolean available = ReflectionUtils.classExists("net.kyori.adventure.bossbar.BossBar");

    /** Player this BossBar belongs to */
    private final TabPlayer player;

    @Override
    @NotNull
    public BossBar constructBossBar(@NotNull TabComponent title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
        return BossBar.bossBar(title.toAdventure(), progress, Color.valueOf(color.name()), Overlay.valueOf(style.name()));
    }

    @Override
    public void create(@NotNull BossBarInfo bar) {
        ((Audience)player.getPlayer()).showBossBar(bar.getBossBar());
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
        ((Audience)player.getPlayer()).hideBossBar(bar.getBossBar());
    }
}
