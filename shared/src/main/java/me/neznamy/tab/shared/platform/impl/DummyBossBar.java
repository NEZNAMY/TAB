package me.neznamy.tab.shared.platform.impl;

import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.platform.BossBar;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Dummy implementation when no suitable one is available.
 */
public class DummyBossBar implements BossBar {

    @Override
    public void create(@NotNull UUID id, @NotNull String title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
        // Do nothing
    }

    @Override
    public void update(@NotNull UUID id, @NotNull String title) {
        // Do nothing
    }

    @Override
    public void update(@NotNull UUID id, float progress) {
        // Do nothing
    }

    @Override
    public void update(@NotNull UUID id, @NotNull BarStyle style) {
        // Do nothing
    }

    @Override
    public void update(@NotNull UUID id, @NotNull BarColor color) {
        // Do nothing
    }

    @Override
    public void remove(@NotNull UUID id) {
        // Do nothing
    }
}
