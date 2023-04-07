package me.neznamy.tab.platforms.fabric;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.bossbar.PlatformBossBar;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;

@RequiredArgsConstructor
public class FabricBossBar implements PlatformBossBar {

    private final FabricTabPlayer player;
    private final Map<UUID, ServerBossEvent> bars = new HashMap<>();

    @Override
    public void create(@NonNull UUID id, @NonNull String title, float progress, @NonNull BarColor color, @NonNull BarStyle style) {
        ServerBossEvent bar = new ServerBossEvent(
                FabricTAB.toComponent(IChatBaseComponent.optimizedComponent(title), player.getVersion()),
                BossEvent.BossBarColor.valueOf(color.name()),
                BossEvent.BossBarOverlay.valueOf(style.name())
        );
        bar.setProgress(progress);
        bars.put(id, bar);
        bar.addPlayer(player.getPlayer());
    }

    @Override
    public void update(@NonNull UUID id, @NonNull String title) {
        bars.get(id).setName(FabricTAB.toComponent(IChatBaseComponent.optimizedComponent(title), player.getVersion()));
    }

    @Override
    public void update(@NonNull UUID id, float progress) {
        bars.get(id).setProgress(progress);
    }

    @Override
    public void update(@NonNull UUID id, @NonNull BarStyle style) {
        bars.get(id).setOverlay(BossEvent.BossBarOverlay.valueOf(style.name()));
    }

    @Override
    public void update(@NonNull UUID id, @NonNull BarColor color) {
        bars.get(id).setColor(BossEvent.BossBarColor.valueOf(color.name()));
    }

    @Override
    public void remove(@NonNull UUID id) {
        bars.remove(id).removePlayer(player.getPlayer());
    }
}
