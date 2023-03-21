package me.neznamy.tab.platforms.sponge8;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.BossBarHandler;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import net.kyori.adventure.bossbar.BossBar;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class SpongeBossBarHandler implements BossBarHandler {

    /** Player to send boss bars to */
    private final SpongeTabPlayer player;
    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    @Override
    public void create(@NonNull UUID id, @NonNull String title, float progress, @NonNull BarColor color, @NonNull BarStyle style) {
        if (bossBars.containsKey(id)) return;
        BossBar bar = BossBar.bossBar(Sponge8TAB.getAdventureCache().get(IChatBaseComponent.optimizedComponent(title), player.getVersion()),
                progress, BossBar.Color.valueOf(color.toString()), BossBar.Overlay.valueOf(style.toString()));
        bossBars.put(id, bar);
        player.getPlayer().showBossBar(bar);
    }

    @Override
    public void update(@NonNull UUID id, @NonNull String title) {
        bossBars.get(id).name(Sponge8TAB.getAdventureCache().get(IChatBaseComponent.optimizedComponent(title), player.getVersion()));
    }

    @Override
    public void update(@NonNull UUID id, float progress) {
        bossBars.get(id).progress(progress);
    }

    @Override
    public void update(@NonNull UUID id, @NonNull BarStyle style) {
        bossBars.get(id).overlay(BossBar.Overlay.valueOf(style.toString()));
    }

    @Override
    public void update(@NonNull UUID id, @NonNull BarColor color) {
        bossBars.get(id).color(BossBar.Color.valueOf(color.toString()));
    }

    @Override
    public void remove(@NonNull UUID id) {
        player.getPlayer().hideBossBar(bossBars.remove(id));
    }
}
