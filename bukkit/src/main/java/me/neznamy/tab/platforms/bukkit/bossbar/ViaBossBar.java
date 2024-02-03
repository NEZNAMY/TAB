package me.neznamy.tab.platforms.bukkit.bossbar;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.legacy.bossbar.BossColor;
import com.viaversion.viaversion.api.legacy.bossbar.BossStyle;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.BossBar;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handler for 1.9+ players on 1.8 server using ViaVersion API.
 */
@RequiredArgsConstructor
public class ViaBossBar implements BossBar {

    /** Style array for fast access */
    private static final BossStyle[] styles = BossStyle.values();
    
    /** Player this handler belongs to */
    @NotNull
    private final BukkitTabPlayer player;

    /** ViaVersion BossBars this 1.9+ player can see on 1.8 server */
    @NotNull
    private final Map<UUID, com.viaversion.viaversion.api.legacy.bossbar.BossBar> viaBossBars = new HashMap<>();

    @Override
    public void create(@NotNull UUID id, @NotNull String title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
        if (viaBossBars.containsKey(id)) return;
        com.viaversion.viaversion.api.legacy.bossbar.BossBar bar = Via.getAPI().legacyAPI().createLegacyBossBar(
                TabComponent.optimized(title).toString(player.getVersion()),
                progress,
                BossColor.valueOf(color.name()),
                styles[style.ordinal()]
        );
        viaBossBars.put(id, bar);
        bar.addPlayer(player.getPlayer().getUniqueId());
    }

    @Override
    public void update(@NotNull UUID id, @NotNull String title) {
        viaBossBars.get(id).setTitle(TabComponent.optimized(title).toString(player.getVersion()));
    }

    @Override
    public void update(@NotNull UUID id, float progress) {
        viaBossBars.get(id).setHealth(progress);
    }

    @Override
    public void update(@NotNull UUID id, @NotNull BarStyle style) {
        viaBossBars.get(id).setStyle(styles[style.ordinal()]);
    }

    @Override
    public void update(@NotNull UUID id, @NotNull BarColor color) {
        viaBossBars.get(id).setColor(BossColor.valueOf(color.name()));
    }

    @Override
    public void remove(@NotNull UUID id) {
        viaBossBars.remove(id).removePlayer(player.getPlayer().getUniqueId());
    }
}
