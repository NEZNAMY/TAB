package me.neznamy.tab.platforms.bukkit.bossbar;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.platform.bossbar.BossBar;
import me.neznamy.tab.api.bossbar.BarColor;
import me.neznamy.tab.api.bossbar.BarStyle;
import me.neznamy.tab.platforms.bukkit.BukkitTabPlayer;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.shared.backend.Location;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * BossBar using wither entity for <1.9 players on <1.9 servers.
 * Additional logic, such as teleporting the entity must be done
 * separately, as this class does not handle it.
 */
@RequiredArgsConstructor
public class EntityBossBar implements BossBar {

    /** Player this handler belongs to */
    private final BukkitTabPlayer player;

    @Override
    public void create(@NotNull UUID id, @NotNull String title, float progress, @NotNull BarColor color, @NotNull BarStyle style) {
        DataWatcher w = new DataWatcher();
        float health = 300*progress;
        if (health == 0) health = 1;
        w.getHelper().setHealth(health);
        w.getHelper().setCustomName(title, player.getVersion());
        w.getHelper().setEntityFlags((byte) 32);
        w.getHelper().setWitherInvulnerableTime(880); // Magic number
        player.getEntityView().spawnEntity(id.hashCode(), new UUID(0, 0), EntityType.WITHER, new Location(0, 0, 0), w);
    }

    @Override
    public void update(@NotNull UUID id, @NotNull String title) {
        DataWatcher w = new DataWatcher();
        w.getHelper().setCustomName(title, player.getVersion());
        player.getEntityView().updateEntityMetadata(id.hashCode(), w);
    }

    @Override
    public void update(@NotNull UUID id, float progress) {
        DataWatcher w = new DataWatcher();
        float health = 300*progress;
        if (health == 0) health = 1;
        w.getHelper().setHealth(health);
        player.getEntityView().updateEntityMetadata(id.hashCode(), w);
    }

    @Override
    public void update(@NotNull UUID id, @NotNull BarStyle style) {/*Added in 1.9*/}

    @Override
    public void update(@NotNull UUID id, @NotNull BarColor color) {/*Added in 1.9*/}

    @Override
    public void remove(@NotNull UUID id) {
        player.getEntityView().destroyEntities(id.hashCode());
    }
}
