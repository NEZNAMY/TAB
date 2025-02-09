package me.neznamy.bossbar.shared.impl;

import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.bossbar.shared.SafeBossBarManager;
import org.jetbrains.annotations.NotNull;

/**
 * Dummy implementation when no suitable one is available.
 */
public class DummyBossBarManager extends SafeBossBarManager<Object> {

    /**
     * Constructs new instance for given player.
     *
     * @param   player
     *          Player this Boss bar will belong to
     */
    public DummyBossBarManager(@NotNull Object player) {
        super(player);
    }

    @Override
    @NotNull
    public Object constructBossBar(@NotNull TabComponent title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
        return new Object();
    }

    @Override
    public void create(@NotNull BossBarInfo bar) {
        // Do nothing
    }

    @Override
    public void updateTitle(@NotNull BossBarInfo bar) {
        // Do nothing
    }

    @Override
    public void updateProgress(@NotNull BossBarInfo bar) {
        // Do nothing
    }

    @Override
    public void updateStyle(@NotNull BossBarInfo bar) {
        // Do nothing
    }

    @Override
    public void updateColor(@NotNull BossBarInfo bar) {
        // Do nothing
    }

    @Override
    public void remove(@NotNull BossBarInfo bar) {
        // Do nothing
    }
}
