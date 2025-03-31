package me.neznamy.tab.platforms.neoforge;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.decorators.SafeBossBar;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import org.jetbrains.annotations.NotNull;

/**
 * BossBar implementation for NeoForge using packets.
 */
@RequiredArgsConstructor
public class NeoForgeBossBar extends SafeBossBar<ServerBossEvent> {

    /** Player this BossBar belongs to */
    @NotNull
    private final NeoForgeTabPlayer player;

    @Override
    @NotNull
    public ServerBossEvent constructBossBar(@NotNull TabComponent title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
        ServerBossEvent bar = new ServerBossEvent(
                title.convert(),
                BossBarColor.valueOf(color.name()),
                BossBarOverlay.valueOf(style.name())
        );
        bar.setProgress(progress);
        return bar;
    }

    @Override
    public void create(@NotNull BossBarInfo bar) {
        bar.getBossBar().addPlayer(player.getPlayer());
    }

    @Override
    public void updateTitle(@NotNull BossBarInfo bar) {
        bar.getBossBar().setName(bar.getTitle().convert());
    }

    @Override
    public void updateProgress(@NotNull BossBarInfo bar) {
        bar.getBossBar().setProgress(bar.getProgress());
    }

    @Override
    public void updateStyle(@NotNull BossBarInfo bar) {
        bar.getBossBar().setOverlay(BossBarOverlay.valueOf(bar.getStyle().name()));
    }

    @Override
    public void updateColor(@NotNull BossBarInfo bar) {
        bar.getBossBar().setColor(BossBarColor.valueOf(bar.getColor().name()));
    }

    @Override
    public void remove(@NotNull BossBarInfo bar) {
        bar.getBossBar().removePlayer(player.getPlayer());
    }
}
