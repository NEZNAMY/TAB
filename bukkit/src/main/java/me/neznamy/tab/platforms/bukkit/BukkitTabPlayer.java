package me.neznamy.tab.platforms.bukkit;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.chat.rgb.RGBUtils;
import me.neznamy.tab.shared.hook.ViaVersionHook;
import me.neznamy.tab.shared.platform.bossbar.PlatformBossBar;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.platform.tablist.TabList;
import me.neznamy.tab.platforms.bukkit.bossbar.BukkitBossBar1_8;
import me.neznamy.tab.platforms.bukkit.bossbar.BukkitBossBar1_9;
import me.neznamy.tab.platforms.bukkit.bossbar.BukkitBossBarVia;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.*;
import me.neznamy.tab.shared.platform.PlatformScoreboard;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

/**
 * TabPlayer implementation for Bukkit platform
 */
@SuppressWarnings("deprecation")
public class BukkitTabPlayer extends BackendTabPlayer {

    /** Player's NMS handle (EntityPlayer), preloading for speed */
    private Object handle;

    /** Player's connection for sending packets, preloading for speed */
    @Getter private Object playerConnection;

    @Getter private final PlatformScoreboard<BukkitTabPlayer> scoreboard = new BukkitScoreboard(this);
    @Getter private final TabList tabList = new BukkitTabList(this);
    @Getter private final PlatformBossBar bossBar = TAB.getInstance().getServerVersion().getMinorVersion() >= 9 ?
            new BukkitBossBar1_9(this) : getVersion().getMinorVersion() >= 9 ? new BukkitBossBarVia(this) : new BukkitBossBar1_8(this);

    /**
     * Constructs new instance with given bukkit player and protocol version
     *
     * @param   p
     *          bukkit player
     */
    public BukkitTabPlayer(Player p) {
        super(p, p.getUniqueId(), p.getName(), TAB.getInstance().getConfiguration().getServerName(),
                p.getWorld().getName(), ViaVersionHook.getInstance().getPlayerVersion(p.getUniqueId(), p.getName()));
        try {
            handle = NMSStorage.getInstance().getHandle.invoke(player);
            playerConnection = NMSStorage.getInstance().PLAYER_CONNECTION.get(handle);
        } catch (ReflectiveOperationException e) {
            TAB.getInstance().getErrorManager().printError("Failed to get playerConnection of " + p.getName(), e);
        }
    }

    @Override
    public boolean hasPermission(@NonNull String permission) {
        return getPlayer().hasPermission(permission);
    }

    @Override
    public int getPing() {
        try {
            int ping = NMSStorage.getInstance().PING.getInt(handle);
            if (ping > 10000 || ping < 0) ping = -1;
            return ping;
        } catch (IllegalAccessException e) {
            return -1;
        }
    }

    public void sendPacket(@Nullable Object nmsPacket) {
        if (nmsPacket == null || !getPlayer().isOnline()) return;
        try {
            NMSStorage.getInstance().sendPacket.invoke(playerConnection, nmsPacket);
        } catch (ReflectiveOperationException e) {
            TAB.getInstance().getErrorManager().printError("An error occurred when sending " + nmsPacket.getClass().getSimpleName(), e);
        }
    }

    @Override
    public void sendMessage(@NonNull IChatBaseComponent message) {
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
    public TabList.Skin getSkin() {
        try {
            Collection<Property> col = ((GameProfile)NMSStorage.getInstance().getProfile.invoke(handle)).getProperties().get(TabList.TEXTURES_PROPERTY);
            if (col.isEmpty()) return null; //offline mode
            Property property = col.iterator().next();
            return new TabList.Skin(property.getValue(), property.getSignature());
        } catch (ReflectiveOperationException e) {
            TAB.getInstance().getErrorManager().printError("Failed to get skin of " + getName(), e);
            return null;
        }
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
    public void setPlayerListHeaderFooter(@NonNull IChatBaseComponent header, @NonNull IChatBaseComponent footer) {
        // Method was added to Bukkit API in 1.13.1, however despite that it's just a String one
        // Using it would cause high CPU usage and massive memory allocations on RGB & animations
        // Send packet instead for performance & older server version support

        /*if (TAB.getInstance().getServerVersion().getNetworkId() >= ProtocolVersion.V1_13_1.getNetworkId()) {
            String bukkitHeader = RGBUtils.getInstance().convertToBukkitFormat(header.toFlatText(),
                    getVersion().getMinorVersion() >= 16 && TAB.getInstance().getServerVersion().getMinorVersion() >= 16);
            String bukkitFooter = RGBUtils.getInstance().convertToBukkitFormat(footer.toFlatText(),
                    getVersion().getMinorVersion() >= 16 && TAB.getInstance().getServerVersion().getMinorVersion() >= 16);
            getPlayer().setPlayerListHeaderFooter(bukkitHeader, bukkitFooter);
            return;
        }*/

        try {
            sendPacket(PacketPlayOutPlayerListHeaderFooterStorage.build(header, footer, version));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void spawnEntity(int entityId, @NonNull UUID id, @NonNull Object entityType, @NonNull Location location, @NonNull EntityData data) {
        try {
            sendPacket(PacketPlayOutSpawnEntityLivingStorage.build(entityId, id, entityType, location, data));
            if (TAB.getInstance().getServerVersion().getMinorVersion() >= 15) {
                updateEntityMetadata(entityId, data);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void updateEntityMetadata(int entityId, @NonNull EntityData data) {
        try {
            if (PacketPlayOutEntityMetadataStorage.CONSTRUCTOR.getParameterCount() == 2) {
                //1.19.3+
                sendPacket(PacketPlayOutEntityMetadataStorage.CONSTRUCTOR.newInstance(entityId, DataWatcher.packDirty.invoke(data.build())));
            } else {
                sendPacket(PacketPlayOutEntityMetadataStorage.CONSTRUCTOR.newInstance(entityId, data.build(), true));
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void teleportEntity(int entityId, @NonNull Location location) {
        try {
            sendPacket(PacketPlayOutEntityTeleportStorage.build(entityId, location));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void destroyEntities(int... entities) {
        try {
            if (PacketPlayOutEntityDestroyStorage.CONSTRUCTOR.getParameterTypes()[0] != int.class) {
                sendPacket(PacketPlayOutEntityDestroyStorage.CONSTRUCTOR.newInstance(new Object[]{entities}));
            } else {
                //1.17.0 Mojank
                for (int entity : entities) {
                    sendPacket(PacketPlayOutEntityDestroyStorage.CONSTRUCTOR.newInstance(entity));
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
