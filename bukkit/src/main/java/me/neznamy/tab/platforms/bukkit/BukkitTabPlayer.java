package me.neznamy.tab.platforms.bukkit;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.SneakyThrows;
import me.neznamy.tab.platforms.bukkit.nms.PacketEntityView;
import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.platforms.bukkit.scoreboard.PacketScoreboard;
import me.neznamy.tab.shared.backend.entityview.EntityView;
import me.neznamy.tab.shared.chat.rgb.RGBUtils;
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
import org.bukkit.entity.Entity;
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
    private final Object handle;

    /** Player's connection for sending packets, preloading for speed */
    private final Object playerConnection;

    private final Scoreboard<BukkitTabPlayer> scoreboard = new PacketScoreboard(this);
    private final TabList tabList = new BukkitTabList(this);
    private final BossBar bossBar = TAB.getInstance().getServerVersion().getMinorVersion() >= 9 ?
            new BukkitBossBar(this) : getVersion().getMinorVersion() >= 9 ? new ViaBossBar(this) : new EntityBossBar(this);
    private final EntityView entityView = new PacketEntityView(this);

    /**
     * Constructs new instance with given bukkit player and protocol version
     *
     * @param   p
     *          bukkit player
     */
    @SneakyThrows
    public BukkitTabPlayer(Player p) {
        super(p, p.getUniqueId(), p.getName(), p.getWorld().getName());
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
        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 17) {
            return getPlayer().getPing();
        }
        return NMSStorage.getInstance().PING.getInt(handle);
    }

    @SneakyThrows
    public void sendPacket(@Nullable Object nmsPacket) {
        if (nmsPacket == null || !getPlayer().isOnline()) return;
        NMSStorage.getInstance().sendPacket.invoke(playerConnection, nmsPacket);
    }

    @Override
    public void sendMessage(@NotNull IChatBaseComponent message) {
        getPlayer().sendMessage(RGBUtils.getInstance().convertToBukkitFormat(message.toFlatText(),
                getVersion().getMinorVersion() >= 16 && TAB.getInstance().getServerVersion().getMinorVersion() >= 16));
    }

    @Override
    public boolean hasInvisibilityPotion() {
        return getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY);
    }

    @Override
    public boolean isDisguised() {
        try {
            if (!((BukkitPlatform)TAB.getInstance().getPlatform()).isLibsDisguisesEnabled()) return false;
            return (boolean) Class.forName("me.libraryaddict.disguise.DisguiseAPI").getMethod("isDisguised", Entity.class).invoke(null, getPlayer());
        } catch (LinkageError | ReflectiveOperationException e) {
            //java.lang.NoClassDefFoundError: Could not initialize class me.libraryaddict.disguise.DisguiseAPI
            TAB.getInstance().getErrorManager().printError("Failed to check disguise status using LibsDisguises", e);
            ((BukkitPlatform)TAB.getInstance().getPlatform()).setLibsDisguisesEnabled(false);
            return false;
        }
    }

    @Override
    @SneakyThrows
    public TabList.Skin getSkin() {
        Collection<Property> col = ((GameProfile)NMSStorage.getInstance().getProfile.invoke(handle)).getProperties().get(TabList.TEXTURES_PROPERTY);
        if (col.isEmpty()) return null; //offline mode
        Property property = col.iterator().next();
        return new TabList.Skin(property.getValue(), property.getSignature());
    }

    @Override
    public @NotNull Player getPlayer() {
        return (Player) player;
    }

    @Override
    public boolean isOnline() {
        return getPlayer().isOnline();
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
    public String getDisplayName() {
        return getPlayer().getDisplayName();
    }
}
