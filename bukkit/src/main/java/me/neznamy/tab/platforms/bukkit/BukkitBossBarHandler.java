package me.neznamy.tab.platforms.bukkit;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.legacy.bossbar.BossColor;
import com.viaversion.viaversion.api.legacy.bossbar.BossStyle;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.BossBarHandler;
import me.neznamy.tab.api.chat.rgb.RGBUtils;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.Location;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class BukkitBossBarHandler implements BossBarHandler {

    private final BukkitTabPlayer player;

    /** Bukkit BossBars the player can currently see */
    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    /** ViaVersion BossBars this 1.9+ player can see on 1.8 server */
    private final Map<UUID, com.viaversion.viaversion.api.legacy.bossbar.BossBar> viaBossBars = new HashMap<>();

    @Override
    public void create(@NonNull UUID id, @NonNull String title, float progress, me.neznamy.tab.api.bossbar.@NonNull BarColor color, me.neznamy.tab.api.bossbar.@NonNull BarStyle style) {
        String convertedTitle = RGBUtils.getInstance().convertToBukkitFormat(title,
                player.getVersion().getMinorVersion() >= 16 && TAB.getInstance().getServerVersion().getMinorVersion() >= 16);
        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 9) {
            if (bossBars.containsKey(id)) return;
            BossBar bar = Bukkit.createBossBar(
                    convertedTitle,
                    BarColor.valueOf(color.name()),
                    BarStyle.valueOf(style.getBukkitName()));
            bar.setProgress(progress);
            bar.addPlayer(player.getPlayer());
            bossBars.put(id, bar);
        } else if (player.getVersion().getMinorVersion() >= 9) {
            if (viaBossBars.containsKey(id)) return;
            com.viaversion.viaversion.api.legacy.bossbar.BossBar bar = Via.getAPI().legacyAPI().createLegacyBossBar(
                    convertedTitle,
                    progress,
                    BossColor.valueOf(color.name()),
                    BossStyle.valueOf(style.getBukkitName()));
            viaBossBars.put(id, bar);
            bar.addPlayer(player.getPlayer().getUniqueId());
        } else {
            DataWatcher w = new DataWatcher();
            float health = 300*progress;
            if (health == 0) health = 1;
            w.getHelper().setHealth(health);
            w.getHelper().setCustomName(title, player.getVersion());
            w.getHelper().setEntityFlags((byte) 32);
            w.getHelper().setWitherInvulnerableTime(880); // Magic number
            player.spawnEntity(id.hashCode(), new UUID(0, 0), EntityType.WITHER, new Location(0, 0, 0, 0, 0), w);
        }
    }

    @Override
    public void update(@NonNull UUID id, @NonNull String title) {
        String convertedTitle = RGBUtils.getInstance().convertToBukkitFormat(title,
                player.getVersion().getMinorVersion() >= 16 && TAB.getInstance().getServerVersion().getMinorVersion() >= 16);
        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 9) {
            bossBars.get(id).setTitle(convertedTitle);
        } else if (player.getVersion().getMinorVersion() >= 9){
            viaBossBars.get(id).setTitle(convertedTitle);
        } else {
            DataWatcher w = new DataWatcher();
            w.getHelper().setCustomName(title, player.getVersion());
            player.updateEntityMetadata(id.hashCode(), w);
        }
    }

    @Override
    public void update(@NonNull UUID id, float progress) {
        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 9) {
            bossBars.get(id).setProgress(progress);
        } else if (player.getVersion().getMinorVersion() >= 9) {
            viaBossBars.get(id).setHealth(progress);
        } else {
            DataWatcher w = new DataWatcher();
            float health = 300*progress;
            if (health == 0) health = 1;
            w.getHelper().setHealth(health);
            player.updateEntityMetadata(id.hashCode(), w);
        }
    }

    @Override
    public void update(@NonNull UUID id, me.neznamy.tab.api.bossbar.@NonNull BarStyle style) {
        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 9) {
            bossBars.get(id).setStyle(BarStyle.valueOf(style.getBukkitName()));
        } else if (player.getVersion().getMinorVersion() >= 9) {
            viaBossBars.get(id).setStyle(BossStyle.valueOf(style.getBukkitName()));
        }
    }

    @Override
    public void update(@NonNull UUID id, me.neznamy.tab.api.bossbar.@NonNull BarColor color) {
        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 9) {
            bossBars.get(id).setColor(BarColor.valueOf(color.name()));
        } else if (player.getVersion().getMinorVersion() >= 9) {
            viaBossBars.get(id).setColor(BossColor.valueOf(color.name()));
        }
    }

    @Override
    public void remove(@NonNull UUID id) {
        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 9) {
            bossBars.remove(id).removePlayer(player.getPlayer());
        } else if (player.getVersion().getMinorVersion() >= 9) {
            viaBossBars.remove(id).removePlayer(player.getPlayer().getUniqueId());
        } else {
            player.destroyEntities(id.hashCode());
        }
    }
}
