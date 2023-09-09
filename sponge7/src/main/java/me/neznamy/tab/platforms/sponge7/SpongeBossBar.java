package me.neznamy.tab.platforms.sponge7;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.bossbar.BossBar;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.boss.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class SpongeBossBar implements BossBar {

    /** Color array for fast access */
    private static final BossBarColor[] colors = new BossBarColor[] {
            BossBarColors.PINK, BossBarColors.BLUE, BossBarColors.RED, BossBarColors.GREEN, BossBarColors.YELLOW, BossBarColors.PURPLE, BossBarColors.WHITE
    };

    /** Style array for fast access */
    private static final BossBarOverlay[] styles = new BossBarOverlay[] {
            BossBarOverlays.PROGRESS, BossBarOverlays.NOTCHED_6, BossBarOverlays.NOTCHED_10, BossBarOverlays.NOTCHED_12, BossBarOverlays.NOTCHED_20
    };

    /** Player to send boss bars to */
    @NotNull
    private final SpongeTabPlayer player;

    @NotNull
    private final Map<UUID, ServerBossBar> bossBars = new HashMap<>();

    @Override
    public void create(@NotNull UUID id, @NotNull String title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
        ServerBossBar bar = ServerBossBar.builder()
                .name(player.getPlatform().toComponent(IChatBaseComponent.optimizedComponent(title), player.getVersion()))
                .color(colors[color.ordinal()])
                .overlay(styles[style.ordinal()])
                .percent(progress)
                .build();
        bossBars.put(id, bar);
        bar.addPlayer(player.getPlayer());
    }

    @Override
    public void update(@NotNull UUID id, @NotNull String title) {
        bossBars.get(id).setName(player.getPlatform().toComponent(IChatBaseComponent.optimizedComponent(title), player.getVersion()));
    }

    @Override
    public void update(@NotNull UUID id, float progress) {
        bossBars.get(id).setPercent(progress);
    }

    @Override
    public void update(@NotNull UUID id, @NotNull BarStyle style) {
        bossBars.get(id).setOverlay(styles[style.ordinal()]);
    }

    @Override
    public void update(@NotNull UUID id, @NotNull BarColor color) {
        bossBars.get(id).setColor(colors[color.ordinal()]);
    }

    @Override
    public void remove(@NotNull UUID id) {
        bossBars.remove(id).removePlayer(player.getPlayer());
    }
}
