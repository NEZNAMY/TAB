package me.neznamy.tab.shared.platform.bossbar;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.hook.AdventureHook;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class AdventureBossBar implements BossBar {

    /** Color array for fast access */
    private static final Color[] colors = new Color[] {
            Color.PINK,
            Color.BLUE,
            Color.RED,
            Color.GREEN,
            Color.YELLOW,
            Color.PURPLE,
            Color.WHITE
    };

    /** Overlay array for fast access */
    private static final Overlay[] overlays = new Overlay[] {
            Overlay.PROGRESS,
            Overlay.NOTCHED_6,
            Overlay.NOTCHED_10,
            Overlay.NOTCHED_12,
            Overlay.NOTCHED_20
    };

    private final TabPlayer player;

    private final Map<UUID, net.kyori.adventure.bossbar.BossBar> bossBars = new LinkedHashMap<>();

    @Override
    public void create(@NotNull UUID id, @NotNull String title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
        net.kyori.adventure.bossbar.BossBar bar = net.kyori.adventure.bossbar.BossBar.bossBar(
                AdventureHook.toAdventureComponent(IChatBaseComponent.optimizedComponent(title), player.getVersion()),
                progress,
                colors[color.ordinal()],
                overlays[style.ordinal()]
        );
        bossBars.put(id, bar);
        ((Audience)player.getPlayer()).showBossBar(bar);
    }

    @Override
    public void update(@NotNull UUID id, @NotNull String title) {
        bossBars.get(id).name(AdventureHook.toAdventureComponent(IChatBaseComponent.optimizedComponent(title), player.getVersion()));
    }

    @Override
    public void update(@NotNull UUID id, float progress) {
        bossBars.get(id).progress(progress);
    }

    @Override
    public void update(@NotNull UUID id, @NotNull BarStyle style) {
        bossBars.get(id).overlay(overlays[style.ordinal()]);
    }

    @Override
    public void update(@NotNull UUID id, @NotNull BarColor color) {
        bossBars.get(id).color(colors[color.ordinal()]);
    }

    @Override
    public void remove(@NotNull UUID id) {
        ((Audience)player.getPlayer()).hideBossBar(bossBars.remove(id));
    }
}
