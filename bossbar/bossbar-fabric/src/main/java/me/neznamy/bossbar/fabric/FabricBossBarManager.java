package me.neznamy.bossbar.fabric;

import me.neznamy.bossbar.shared.SafeBossBarManager;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import org.jetbrains.annotations.NotNull;

/**
 * BossBar implementation for Fabric using packets.
 */
public class FabricBossBarManager extends SafeBossBarManager<ServerBossEvent> {

    /**
     * Constructs new instance for given player.
     *
     * @param   player
     *          Player this Boss bar will belong to
     */
    public FabricBossBarManager(@NotNull ServerPlayer player) {
        super(player);
    }

    @Override
    @NotNull
    public ServerBossEvent constructBossBar(@NotNull TabComponent title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
        ServerBossEvent bar = new ServerBossEvent(
                title.convert(),
                BossBarColor.valueOf(color.name()),
                BossBarOverlay.valueOf(style.name())
        );
        bar.setProgress(progress); // Somehow, the compiled method name is same despite method being renamed in 1.17
        return bar;
    }

    @Override
    public void create(@NotNull BossBarInfo bar) {
        bar.getBossBar().addPlayer((ServerPlayer) player);
    }

    @Override
    public void updateTitle(@NotNull BossBarInfo bar) {
        bar.getBossBar().setName(bar.getTitle().convert());
    }

    @Override
    public void updateProgress(@NotNull BossBarInfo bar) {
        bar.getBossBar().setProgress(bar.getProgress()); // Somehow, the compiled method name is same despite method being renamed in 1.17
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
        bar.getBossBar().removePlayer((ServerPlayer) player);
    }
}
