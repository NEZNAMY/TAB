package me.neznamy.tab.platforms.bukkit;

import lombok.Getter;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.hook.LibsDisguisesHook;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.nms.PacketEntityView;
import me.neznamy.tab.platforms.bukkit.nms.PingRetriever;
import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.platforms.bukkit.scoreboard.ScoreboardLoader;
import me.neznamy.tab.shared.backend.entityview.EntityView;
import me.neznamy.tab.shared.platform.BossBar;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.platforms.bukkit.bossbar.EntityBossBar;
import me.neznamy.tab.platforms.bukkit.bossbar.BukkitBossBar;
import me.neznamy.tab.platforms.bukkit.bossbar.ViaBossBar;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.util.ReflectionUtils;
import net.md_5.bungee.chat.ComponentSerializer;
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

    private static final boolean spigot = ReflectionUtils.classExists("org.bukkit.entity.Player$Spigot");

    @NotNull
    private final Scoreboard<BukkitTabPlayer> scoreboard = ScoreboardLoader.getInstance().apply(this);

    @NotNull
    private final BukkitTabList tabList = new BukkitTabList(this);

    @NotNull
    private final BossBar bossBar = BukkitReflection.getMinorVersion() >= 9 ? new BukkitBossBar(this) :
            getVersion().getMinorVersion() >= 9 ? new ViaBossBar(this) : new EntityBossBar(this);

    @NotNull
    private final EntityView entityView = new PacketEntityView(this);

    /**
     * Constructs new instance with given bukkit player
     *
     * @param   platform
     *          Server platform
     * @param   p
     *          bukkit player
     */
    public BukkitTabPlayer(@NotNull BukkitPlatform platform, @NotNull Player p) {
        super(platform, p, p.getUniqueId(), p.getName(), p.getWorld().getName());
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return getPlayer().hasPermission(permission);
    }

    @Override
    @SneakyThrows
    public int getPing() {
        return PingRetriever.getPing(getPlayer());
    }

    @Override
    public void sendMessage(@NotNull IChatBaseComponent message) {
        if (spigot) {
            getPlayer().spigot().sendMessage(ComponentSerializer.parse(message.toString(getVersion())));
        } else {
            getPlayer().sendMessage(BukkitUtils.toBukkitFormat(message, getVersion().getMinorVersion() >= 16));
        }
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
    public boolean isOnline() {
        return getPlayer().isOnline();
    }

    @Override
    public BukkitPlatform getPlatform() {
        return (BukkitPlatform) platform;
    }

    @Override
    public boolean isVanished() {
        return getPlayer().getMetadata("vanished").stream().anyMatch(MetadataValue::asBoolean);
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
