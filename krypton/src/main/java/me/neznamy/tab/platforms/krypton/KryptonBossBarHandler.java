package me.neznamy.tab.platforms.krypton;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.BossBarHandler;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import net.kyori.adventure.bossbar.BossBar;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class KryptonBossBarHandler implements BossBarHandler {

    private final KryptonTabPlayer player;

    private final Map<UUID, BossBar> bossBars = new LinkedHashMap<>();

    public void create(@NotNull UUID id, @NotNull String title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
        if (bossBars.containsKey(id)) return;
        BossBar bar = BossBar.bossBar(Main.toComponent(title, player.getVersion()),
                progress,
                BossBar.Color.valueOf(color.toString()),
                BossBar.Overlay.valueOf(style.toString()));
        bossBars.put(id, bar);
        player.getPlayer().showBossBar(bar);
    }

    public void update(@NotNull UUID id, @NotNull String title) {
        bossBars.get(id).name(Main.toComponent(title, player.getVersion()));
    }

    public void update(@NotNull UUID id, float progress) {
        bossBars.get(id).progress(progress);
    }

    public void update(@NotNull UUID id, @NotNull BarStyle style) {
        bossBars.get(id).overlay(BossBar.Overlay.valueOf(style.toString()));
    }

    public void update(@NotNull UUID id, @NotNull BarColor color) {
        bossBars.get(id).color(BossBar.Color.valueOf(color.toString()));
    }

    public void remove(@NotNull UUID id) {
        player.getPlayer().hideBossBar(bossBars.remove(id));
    }
}