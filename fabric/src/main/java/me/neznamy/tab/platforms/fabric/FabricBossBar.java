package me.neznamy.tab.platforms.fabric;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.bossbar.BossBar;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class FabricBossBar implements BossBar {

    /** Color array for fast access */
    private static final BossBarColor[] colors = new BossBarColor[] {
            BossBarColor.PINK,
            BossBarColor.BLUE,
            BossBarColor.RED,
            BossBarColor.GREEN,
            BossBarColor.YELLOW,
            BossBarColor.PURPLE,
            BossBarColor.WHITE
    };

    /** Overlay array for fast access */
    private static final BossBarOverlay[] overlays = new BossBarOverlay[] {
            BossBarOverlay.PROGRESS,
            BossBarOverlay.NOTCHED_6,
            BossBarOverlay.NOTCHED_10,
            BossBarOverlay.NOTCHED_12,
            BossBarOverlay.NOTCHED_20
    };
    
    @NotNull
    private final FabricTabPlayer player;

    @NotNull
    private final Map<UUID, ServerBossEvent> bars = new HashMap<>();

    @Override
    public void create(@NotNull UUID id, @NotNull String title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
        ServerBossEvent bar = new ServerBossEvent(
                player.getPlatform().toComponent(IChatBaseComponent.optimizedComponent(title), player.getVersion()),
                colors[color.ordinal()],
                overlays[style.ordinal()]
        );
        bar.setProgress(progress);
        bars.put(id, bar);
        bar.addPlayer(player.getPlayer());
    }

    @Override
    public void update(@NotNull UUID id, @NotNull String title) {
        bars.get(id).setName(player.getPlatform().toComponent(IChatBaseComponent.optimizedComponent(title), player.getVersion()));
    }

    @Override
    public void update(@NotNull UUID id, float progress) {
        bars.get(id).setProgress(progress);
    }

    @Override
    public void update(@NotNull UUID id, @NotNull BarStyle style) {
        bars.get(id).setOverlay(overlays[style.ordinal()]);
    }

    @Override
    public void update(@NotNull UUID id, @NotNull BarColor color) {
        bars.get(id).setColor(colors[color.ordinal()]);
    }

    @Override
    public void remove(@NotNull UUID id) {
        bars.remove(id).removePlayer(player.getPlayer());
    }
}
