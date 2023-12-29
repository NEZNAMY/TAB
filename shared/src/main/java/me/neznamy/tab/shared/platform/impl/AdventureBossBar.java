package me.neznamy.tab.shared.platform.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.hook.AdventureHook;
import me.neznamy.tab.shared.platform.BossBar;
import me.neznamy.tab.shared.platform.TabPlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import org.jetbrains.annotations.NotNull;

/**
 * Shared BossBar implementation using Adventure API.
 */
@AllArgsConstructor
public class AdventureBossBar implements BossBar {

    /** Player this BossBar belongs to */
    private final TabPlayer player;

    /** BossBars currently visible to the player */
    private final Map<UUID, net.kyori.adventure.bossbar.BossBar> bossBars = new LinkedHashMap<>();

    @Override
    public void create(@NotNull UUID id, @NotNull String title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
        if (bossBars.containsKey(id)) {
            // Can happen on 1.20.2+ on Velocity on server switch
            ((Audience)player.getPlayer()).hideBossBar(bossBars.get(id));
            ((Audience)player.getPlayer()).showBossBar(bossBars.get(id));
            return;
        }
        net.kyori.adventure.bossbar.BossBar bar = net.kyori.adventure.bossbar.BossBar.bossBar(
                AdventureHook.toAdventureComponent(IChatBaseComponent.optimizedComponent(title), player.getVersion()),
                progress,
                Color.valueOf(color.name()),
                Overlay.valueOf(style.name())
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
        bossBars.get(id).overlay(Overlay.valueOf(style.name()));
    }

    @Override
    public void update(@NotNull UUID id, @NotNull BarColor color) {
        bossBars.get(id).color(Color.valueOf(color.name()));
    }

    @Override
    public void remove(@NotNull UUID id) {
        ((Audience)player.getPlayer()).hideBossBar(bossBars.remove(id));
    }
}
