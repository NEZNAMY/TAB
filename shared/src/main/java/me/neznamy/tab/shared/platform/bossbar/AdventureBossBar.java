package me.neznamy.tab.shared.platform.bossbar;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Setter;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class AdventureBossBar implements PlatformBossBar {

    @Setter private Audience audience;

    private final Map<UUID, BossBar> bossBars = new LinkedHashMap<>();

    @Override
    public void create(@NotNull UUID id, @NotNull String title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
        if (bossBars.containsKey(id)) return;
        BossBar bar = BossBar.bossBar(IChatBaseComponent.optimizedComponent(title).toAdventureComponent(),
                progress,
                BossBar.Color.valueOf(color.toString()),
                BossBar.Overlay.valueOf(style.toString()));
        bossBars.put(id, bar);
        audience.showBossBar(bar);
    }

    @Override
    public void update(@NotNull UUID id, @NotNull String title) {
        bossBars.get(id).name(IChatBaseComponent.optimizedComponent(title).toAdventureComponent());
    }

    @Override
    public void update(@NotNull UUID id, float progress) {
        bossBars.get(id).progress(progress);
    }

    @Override
    public void update(@NotNull UUID id, @NotNull BarStyle style) {
        bossBars.get(id).overlay(BossBar.Overlay.valueOf(style.toString()));
    }

    @Override
    public void update(@NotNull UUID id, @NotNull BarColor color) {
        bossBars.get(id).color(BossBar.Color.valueOf(color.toString()));
    }

    @Override
    public void remove(@NotNull UUID id) {
        audience.hideBossBar(bossBars.remove(id));
    }
}
