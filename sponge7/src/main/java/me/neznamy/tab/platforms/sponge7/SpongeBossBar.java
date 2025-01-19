package me.neznamy.tab.platforms.sponge7;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.decorators.SafeBossBar;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.boss.*;

/**
 * BossBar implementation for Sponge 7 using its API.
 */
@RequiredArgsConstructor
public class SpongeBossBar extends SafeBossBar<ServerBossBar> {

    /** Color array for fast access */
    private static final BossBarColor[] colors = {
            BossBarColors.PINK,
            BossBarColors.BLUE,
            BossBarColors.RED,
            BossBarColors.GREEN,
            BossBarColors.YELLOW,
            BossBarColors.PURPLE,
            BossBarColors.WHITE
    };

    /** Style array for fast access */
    private static final BossBarOverlay[] styles = {
            BossBarOverlays.PROGRESS,
            BossBarOverlays.NOTCHED_6,
            BossBarOverlays.NOTCHED_10,
            BossBarOverlays.NOTCHED_12,
            BossBarOverlays.NOTCHED_20
    };

    /** Player to send boss bars to */
    @NotNull
    private final SpongeTabPlayer player;

    @Override
    @NotNull
    public ServerBossBar constructBossBar(@NotNull TabComponent title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
        return ServerBossBar.builder()
                .name(title.convert())
                .color(colors[color.ordinal()])
                .overlay(styles[style.ordinal()])
                .percent(progress)
                .build();
    }

    @Override
    public void create(@NotNull BossBarInfo bar) {
        bar.getBossBar().addPlayer(player.getPlayer());
    }

    @Override
    public void updateTitle(@NotNull BossBarInfo bar) {
        bar.getBossBar().setName(bar.getTitle().convert());
    }

    @Override
    public void updateProgress(@NotNull BossBarInfo bar) {
        bar.getBossBar().setPercent(bar.getProgress());
    }

    @Override
    public void updateStyle(@NotNull BossBarInfo bar) {
        bar.getBossBar().setOverlay(styles[bar.getStyle().ordinal()]);
    }

    @Override
    public void updateColor(@NotNull BossBarInfo bar) {
        bar.getBossBar().setColor(colors[bar.getColor().ordinal()]);
    }

    @Override
    public void remove(@NotNull BossBarInfo bar) {
        bar.getBossBar().removePlayer(player.getPlayer());
    }
}
