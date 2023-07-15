package me.neznamy.tab.platforms.sponge7;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.platform.bossbar.BossBar;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.boss.*;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class SpongeBossBar implements BossBar {

    /** Player to send boss bars to */
    private final SpongeTabPlayer player;
    private final Map<UUID, ServerBossBar> bossBars = new HashMap<>();

    @Override
    public void create(@NotNull UUID id, @NotNull String title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
        ServerBossBar bar = ServerBossBar.builder()
                .name(Text.of(title))
                .color(convertBossBarColor(color))
                .overlay(convertOverlay(style))
                .percent(progress)
                .build();
        bossBars.put(id, bar);
        bar.addPlayer(player.getPlayer());
    }

    @Override
    public void update(@NotNull UUID id, @NotNull String title) {
        bossBars.get(id).setName(Text.of(title));
    }

    @Override
    public void update(@NotNull UUID id, float progress) {
        bossBars.get(id).setPercent(progress);
    }

    @Override
    public void update(@NotNull UUID id, @NotNull BarStyle style) {
        bossBars.get(id).setOverlay(convertOverlay(style));
    }

    @Override
    public void update(@NotNull UUID id, @NotNull BarColor color) {
        bossBars.get(id).setColor(convertBossBarColor(color));
    }

    @Override
    public void remove(@NotNull UUID id) {
        bossBars.remove(id).removePlayer(player.getPlayer());
    }

    private @NotNull BossBarColor convertBossBarColor(@NotNull BarColor color) {
        switch (color) {
            case PINK: return BossBarColors.PINK;
            case BLUE: return BossBarColors.BLUE;
            case RED: return BossBarColors.RED;
            case GREEN: return BossBarColors.GREEN;
            case YELLOW: return BossBarColors.YELLOW;
            case WHITE: return BossBarColors.WHITE;
            default: return BossBarColors.PURPLE;
        }
    }

    private @NotNull BossBarOverlay convertOverlay(@NotNull BarStyle style) {
        switch (style) {
            case NOTCHED_6: return BossBarOverlays.NOTCHED_6;
            case NOTCHED_10: return BossBarOverlays.NOTCHED_10;
            case NOTCHED_12: return BossBarOverlays.NOTCHED_12;
            case NOTCHED_20: return BossBarOverlays.NOTCHED_20;
            default: return BossBarOverlays.PROGRESS;
        }
    }
}
