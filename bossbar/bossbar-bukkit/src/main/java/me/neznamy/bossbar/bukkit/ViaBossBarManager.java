package me.neznamy.bossbar.bukkit;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.legacy.bossbar.BossBar;
import com.viaversion.viaversion.api.legacy.bossbar.BossColor;
import com.viaversion.viaversion.api.legacy.bossbar.BossStyle;
import me.neznamy.bossbar.shared.SafeBossBarManager;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Handler for 1.9+ players on 1.8 server using ViaVersion API.
 */
public class ViaBossBarManager extends SafeBossBarManager<BossBar> {

    /** Style array for fast access */
    private static final BossStyle[] styles = BossStyle.values();

    /**
     * Constructs new instance for given player.
     *
     * @param   player
     *          Player this Boss bar will belong to
     */
    public ViaBossBarManager(@NotNull Player player) {
        super(player);
    }

    @Override
    @NotNull
    public BossBar constructBossBar(@NotNull TabComponent title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
        return Via.getAPI().legacyAPI().createLegacyBossBar(
                title.toLegacyText(),
                progress,
                BossColor.valueOf(color.name()),
                styles[style.ordinal()]
        );
    }

    @Override
    public void create(@NotNull BossBarInfo bar) {
        bar.getBossBar().addPlayer(((Player)player).getUniqueId());
    }

    @Override
    public void updateTitle(@NotNull BossBarInfo bar) {
        bar.getBossBar().setTitle(bar.getTitle().toLegacyText());
    }

    @Override
    public void updateProgress(@NotNull BossBarInfo bar) {
        bar.getBossBar().setHealth(bar.getProgress());
    }

    @Override
    public void updateStyle(@NotNull BossBarInfo bar) {
        bar.getBossBar().setStyle(styles[bar.getStyle().ordinal()]);
    }

    @Override
    public void updateColor(@NotNull BossBarInfo bar) {
        bar.getBossBar().setColor(BossColor.valueOf(bar.getColor().name()));
    }

    @Override
    public void remove(@NotNull BossBarInfo bar) {
        bar.getBossBar().removePlayer(((Player)player).getUniqueId());
    }
}
