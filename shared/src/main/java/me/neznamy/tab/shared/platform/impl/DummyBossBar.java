package me.neznamy.tab.shared.platform.impl;

import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.decorators.SafeBossBar;
import org.jetbrains.annotations.NotNull;

/**
 * Dummy implementation when no suitable one is available.
 */
public class DummyBossBar extends SafeBossBar<Object> {

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
