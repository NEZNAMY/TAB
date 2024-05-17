package me.neznamy.tab.platforms.bukkit;

import lombok.Getter;
import me.neznamy.tab.platforms.bukkit.bossbar.BossBarLoader;
import me.neznamy.tab.platforms.bukkit.hook.LibsDisguisesHook;
import me.neznamy.tab.platforms.bukkit.nms.PingRetriever;
import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.platforms.bukkit.scoreboard.ScoreboardLoader;
import me.neznamy.tab.platforms.bukkit.tablist.TabListBase;
import me.neznamy.tab.shared.platform.BossBar;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * TabPlayer implementation for Bukkit platform
 */
@SuppressWarnings("deprecation")
@Getter
public class BukkitTabPlayer extends BackendTabPlayer {

    @NotNull
    private final Scoreboard<BukkitTabPlayer, ?> scoreboard = ScoreboardLoader.getInstance().apply(this);

    @NotNull
    private final TabListBase<?> tabList = TabListBase.getInstance().apply(this);

    @NotNull
    private final BossBar bossBar = BossBarLoader.findInstance(this);

    /**
     * Constructs new instance with given bukkit player
     *
     * @param   platform
     *          Server platform
     * @param   p
     *          bukkit player
     */
    public BukkitTabPlayer(@NotNull BukkitPlatform platform, @NotNull Player p) {
        super(platform, p, p.getUniqueId(), p.getName(), p.getWorld().getName(), platform.getServerVersion().getNetworkId());
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return getPlayer().hasPermission(permission);
    }

    @Override
    public int getPing() {
        return PingRetriever.getPing(getPlayer());
    }

    @Override
    public void sendMessage(@NotNull TabComponent message) {
        getPlayer().sendMessage(getPlatform().toBukkitFormat(message, getVersion().supportsRGB()));
    }

    @Override
    public boolean hasInvisibilityPotion() {
        return getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY);
    }

    @Override
    public boolean isDisguised() {
        return LibsDisguisesHook.isDisguised(this);
    }

    @Override
    @Nullable
    public TabList.Skin getSkin() {
        return tabList.getSkin();
    }

    @Override
    @NotNull
    public Player getPlayer() {
        return (Player) player;
    }

    @Override
    public BukkitPlatform getPlatform() {
        return (BukkitPlatform) platform;
    }

    @Override
    public boolean isVanished0() {
        for (MetadataValue v : getPlayer().getMetadata("vanished")) {
            if (v.asBoolean()) return true;
        }
        return false;
    }

    @Override
    public int getGamemode() {
        return getPlayer().getGameMode().getValue();
    }

    @Override
    public double getHealth() {
        return getPlayer().getHealth();
    }

    @Override
    @NotNull
    public String getDisplayName() {
        return getPlayer().getDisplayName();
    }
}
