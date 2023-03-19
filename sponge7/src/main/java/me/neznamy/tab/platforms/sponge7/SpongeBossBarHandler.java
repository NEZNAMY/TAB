package me.neznamy.tab.platforms.sponge7;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.BossBarHandler;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import org.spongepowered.api.boss.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class SpongeBossBarHandler implements BossBarHandler {

    /** Player to send boss bars to */
    private final SpongeTabPlayer player;
    private final Map<UUID, ServerBossBar> bossBars = new HashMap<>();

    @Override
    public void create(@NonNull UUID id, @NonNull String title, float progress, @NonNull BarColor color, @NonNull BarStyle style) {
        ServerBossBar bar = ServerBossBar.builder()
                .name(Sponge7TAB.getTextCache().get(IChatBaseComponent.optimizedComponent(title), player.getVersion()))
                .color(convertBossBarColor(color))
                .overlay(convertOverlay(style))
                .percent(progress)
                .build();
        bossBars.put(id, bar);
        bar.addPlayer(player.getPlayer());
    }

    @Override
    public void update(@NonNull UUID id, @NonNull String title) {
        bossBars.get(id).setName(Sponge7TAB.getTextCache().get(IChatBaseComponent.optimizedComponent(title), player.getVersion()));
    }

    @Override
    public void update(@NonNull UUID id, float progress) {
        bossBars.get(id).setPercent(progress);
    }

    @Override
    public void update(@NonNull UUID id, @NonNull BarStyle style) {
        bossBars.get(id).setOverlay(convertOverlay(style));
    }

    @Override
    public void update(@NonNull UUID id, @NonNull BarColor color) {
        bossBars.get(id).setColor(convertBossBarColor(color));
    }

    @Override
    public void remove(@NonNull UUID id) {
        bossBars.remove(id).removePlayer(player.getPlayer());
    }

    private @NonNull BossBarColor convertBossBarColor(@NonNull BarColor color) {
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

    private @NonNull BossBarOverlay convertOverlay(@NonNull BarStyle style) {
        switch (style) {
            case NOTCHED_6: return BossBarOverlays.NOTCHED_6;
            case NOTCHED_10: return BossBarOverlays.NOTCHED_10;
            case NOTCHED_12: return BossBarOverlays.NOTCHED_12;
            case NOTCHED_20: return BossBarOverlays.NOTCHED_20;
            default: return BossBarOverlays.PROGRESS;
        }
    }
}
