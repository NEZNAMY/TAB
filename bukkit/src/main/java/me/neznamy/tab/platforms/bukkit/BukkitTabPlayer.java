package me.neznamy.tab.platforms.bukkit;

import lombok.Getter;
import me.neznamy.tab.platforms.bukkit.bossbar.BossBarLoader;
import me.neznamy.tab.platforms.bukkit.hook.LibsDisguisesHook;
import me.neznamy.tab.platforms.bukkit.entity.PacketEntityView;
import me.neznamy.tab.platforms.bukkit.nms.PingRetriever;
import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.platforms.bukkit.scoreboard.ScoreboardLoader;
import me.neznamy.tab.platforms.bukkit.tablist.TabListBase;
import me.neznamy.tab.shared.backend.entityview.DummyEntityView;
import me.neznamy.tab.shared.backend.entityview.EntityView;
import me.neznamy.tab.shared.platform.BossBar;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
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

    private static final boolean spigot = ReflectionUtils.classExists("net.md_5.bungee.chat.ComponentSerializer");

    @NotNull
    private final Scoreboard<BukkitTabPlayer> scoreboard = ScoreboardLoader.getInstance().apply(this);

    @NotNull
    private final TabListBase tabList = TabListBase.getInstance().apply(this);

    @NotNull
    private final BossBar bossBar = BossBarLoader.findInstance(this);

    @NotNull
    private final EntityView entityView = PacketEntityView.isAvailable() ? new PacketEntityView(this) : new DummyEntityView();

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
        if (spigot) {
            getPlayer().spigot().sendMessage(ComponentSerializer.parse(message.toString(getVersion())));
        } else {
            getPlayer().sendMessage(getPlatform().toBukkitFormat(message, getVersion().supportsRGB()));
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
