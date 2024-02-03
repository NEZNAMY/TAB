package me.neznamy.tab.platforms.fabric;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.BossBar;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * BossBar implementation for Fabric using packets.
 */
@RequiredArgsConstructor
public class FabricBossBar implements BossBar {

    /** Player this BossBar belongs to */
    @NotNull
    private final FabricTabPlayer player;

    /** Map of BossBars visible to the player */
    @NotNull
    private final Map<UUID, ServerBossEvent> bars = new HashMap<>();

    @Override
    public void create(@NotNull UUID id, @NotNull String title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
        ServerBossEvent bar = new ServerBossEvent(
                player.getPlatform().toComponent(TabComponent.optimized(title), player.getVersion()),
                BossBarColor.valueOf(color.name()),
                BossBarOverlay.valueOf(style.name())
        );
        bar.setProgress(progress); // Somehow the compiled method name is same despite method being renamed in 1.17
        bars.put(id, bar);
        bar.addPlayer(player.getPlayer());
    }

    @Override
    public void update(@NotNull UUID id, @NotNull String title) {
        bars.get(id).setName(player.getPlatform().toComponent(TabComponent.optimized(title), player.getVersion()));
    }

    @Override
    public void update(@NotNull UUID id, float progress) {
        bars.get(id).setProgress(progress); // Somehow the compiled method name is same despite method being renamed in 1.17
    }

    @Override
    public void update(@NotNull UUID id, @NotNull BarStyle style) {
        bars.get(id).setOverlay(BossBarOverlay.valueOf(style.name()));
    }

    @Override
    public void update(@NotNull UUID id, @NotNull BarColor color) {
        bars.get(id).setColor(BossBarColor.valueOf(color.name()));
    }

    @Override
    public void remove(@NotNull UUID id) {
        bars.remove(id).removePlayer(player.getPlayer());
    }
}
