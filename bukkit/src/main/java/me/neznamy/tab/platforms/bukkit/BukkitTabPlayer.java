package me.neznamy.tab.platforms.bukkit;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.hook.LibsDisguisesHook;
import me.neznamy.tab.platforms.bukkit.nms.BukkitReflection;
import me.neznamy.tab.platforms.bukkit.nms.PacketEntityView;
import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.platforms.bukkit.scoreboard.PacketScoreboard;
import me.neznamy.tab.shared.backend.entityview.EntityView;
import me.neznamy.tab.shared.platform.bossbar.BossBar;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.platforms.bukkit.bossbar.EntityBossBar;
import me.neznamy.tab.platforms.bukkit.bossbar.BukkitBossBar;
import me.neznamy.tab.platforms.bukkit.bossbar.ViaBossBar;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * TabPlayer implementation for Bukkit platform
 */
@SuppressWarnings("deprecation")
@Getter
public class BukkitTabPlayer extends BackendTabPlayer {

    /** Player's NMS handle (EntityPlayer), preloading for speed */
    @NotNull
    private final Object handle;

    /** Player's connection for sending packets, preloading for speed */
    @NotNull
    private final Object playerConnection;

    @NotNull
    private final Scoreboard<BukkitTabPlayer> scoreboard = new PacketScoreboard(this);

    @NotNull
    private final TabList tabList = new BukkitTabList(this);

    @NotNull
    private final BossBar bossBar = TAB.getInstance().getServerVersion().getMinorVersion() >= 9 ?
            new BukkitBossBar(this) : getVersion().getMinorVersion() >= 9 ? new ViaBossBar(this) : new EntityBossBar(this);

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
    @SneakyThrows
    public BukkitTabPlayer(@NotNull BukkitPlatform platform, @NotNull Player p) {
        super(platform, p, p.getUniqueId(), p.getName(), p.getWorld().getName());
        handle = NMSStorage.getInstance().getHandle.invoke(player);
        playerConnection = NMSStorage.getInstance().PLAYER_CONNECTION.get(handle);
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return getPlayer().hasPermission(permission);
    }

    @Override
    @SneakyThrows
    public int getPing() {
        if (BukkitReflection.getMinorVersion() >= 17) {
            return getPlayer().getPing();
        }
        return NMSStorage.getInstance().PING.getInt(handle);
    }

    @SneakyThrows
    public void sendPacket(@NotNull Object nmsPacket) {
        if (!getPlayer().isOnline()) return;
        NMSStorage.getInstance().sendPacket.invoke(playerConnection, nmsPacket);
    }

    @Override
    public void sendMessage(@NotNull IChatBaseComponent message) {
        getPlayer().sendMessage(BukkitUtils.toBukkitFormat(message, getVersion().getMinorVersion() >= 16));
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
    @SneakyThrows
    @Nullable
    public TabList.Skin getSkin() {
        Collection<Property> col = ((GameProfile)NMSStorage.getInstance().getProfile.invoke(handle)).getProperties().get(TabList.TEXTURES_PROPERTY);
        if (col.isEmpty()) return null; //offline mode
        Property property = col.iterator().next();
        if (BukkitReflection.is1_20_2Plus()) {
            return new TabList.Skin(
                    (String) property.getClass().getMethod("value").invoke(property),
                    (String) property.getClass().getMethod("signature").invoke(property)
            );
        } else {
            return new TabList.Skin(property.getValue(), property.getSignature());
        }
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
