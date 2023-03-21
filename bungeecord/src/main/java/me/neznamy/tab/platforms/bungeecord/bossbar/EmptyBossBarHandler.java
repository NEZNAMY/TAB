package me.neznamy.tab.platforms.bungeecord.bossbar;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.BossBarHandler;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;

import java.util.UUID;

/**
 * Dummy boss bar handler for 1.8 players, as the packet was added in
 * 1.9 and BungeeCord does not support entities.
 */
public class EmptyBossBarHandler implements BossBarHandler {

    @Getter private static final EmptyBossBarHandler instance = new EmptyBossBarHandler();

    @Override
    public void create(@NonNull UUID id, @NonNull String title, float progress, @NonNull BarColor color, @NonNull BarStyle style) {}

    @Override
    public void update(@NonNull UUID id, @NonNull String title) {}

    @Override
    public void update(@NonNull UUID id, float progress) {}

    @Override
    public void update(@NonNull UUID id, @NonNull BarStyle style) {}

    @Override
    public void update(@NonNull UUID id, @NonNull BarColor color) {}

    @Override
    public void remove(@NonNull UUID id) {}
}
