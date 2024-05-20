
package me.neznamy.tab.shared.platform.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.BossBar;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import org.jetbrains.annotations.NotNull;

/**
 * Shared BossBar implementation using Adventure API.
 */
@RequiredArgsConstructor
public class AdventureBossBar implements BossBar {

    /** Flag tracking whether this implementation is available on the server or not */
    @Getter
    private static final boolean available = ReflectionUtils.classExists("net.kyori.adventure.bossbar.BossBar");

    /** Player this BossBar belongs to */
    private final TabPlayer player;

    /** BossBars currently visible to the player */
    private final Map<UUID, net.kyori.adventure.bossbar.BossBar> bossBars = new HashMap<>();

    /** Flag tracking whether boss bars should be frozen or not */
    private boolean frozen;

    @Override
    public void create(@NotNull UUID id, @NotNull String title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
        if (frozen) return; // Server switch
        net.kyori.adventure.bossbar.BossBar bar = net.kyori.adventure.bossbar.BossBar.bossBar(
                TabComponent.optimized(title).convert(player.getVersion()),
                progress,
                Color.valueOf(color.name()),
                Overlay.valueOf(style.name())
        );
        bossBars.put(id, bar);
        ((Audience)player.getPlayer()).showBossBar(bar);
    }

    @Override
    public void update(@NotNull UUID id, @NotNull String title) {
        if (frozen) return; // Server switch
        bossBars.get(id).name(TabComponent.optimized(title).convert(player.getVersion()));
    }

    @Override
    public void update(@NotNull UUID id, float progress) {
        if (frozen) return; // Server switch
        bossBars.get(id).progress(progress);
    }

    @Override
    public void update(@NotNull UUID id, @NotNull BarStyle style) {
        if (frozen) return; // Server switch
        bossBars.get(id).overlay(Overlay.valueOf(style.name()));
    }

    @Override
    public void update(@NotNull UUID id, @NotNull BarColor color) {
        if (frozen) return; // Server switch
        bossBars.get(id).color(Color.valueOf(color.name()));
    }

    @Override
    public void remove(@NotNull UUID id) {
        if (frozen) return; // Server switch
        ((Audience)player.getPlayer()).hideBossBar(bossBars.remove(id));
    }

    @Override
    public void freeze() {
        bossBars.clear();
        frozen = true;
    }

    @Override
    public void unfreeze() {
        frozen = false;
    }
}
