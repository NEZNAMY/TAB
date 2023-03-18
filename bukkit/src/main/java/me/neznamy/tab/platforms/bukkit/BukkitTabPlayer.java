package me.neznamy.tab.platforms.bukkit;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.legacy.bossbar.BossColor;
import com.viaversion.viaversion.api.legacy.bossbar.BossStyle;
import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.Scoreboard;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.chat.rgb.RGBUtils;
import me.neznamy.tab.api.tablist.Skin;
import me.neznamy.tab.api.tablist.TabList;
import me.neznamy.tab.api.util.ComponentCache;
import me.neznamy.tab.api.util.ReflectionUtils;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;
import me.neznamy.tab.platforms.bukkit.nms.storage.packet.*;
import me.neznamy.tab.platforms.bukkit.tablist.BulkUpdateBukkitTabList;
import me.neznamy.tab.platforms.bukkit.tablist.SingleUpdateBukkitTabList;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * TabPlayer implementation for Bukkit platform
 */
@SuppressWarnings("deprecation")
public class BukkitTabPlayer extends BackendTabPlayer {

    /** Spigot check */
    private static final boolean spigot = ReflectionUtils.classExists("org.bukkit.entity.Player$Spigot");

    /** Component cache to save CPU when creating components */
    private static final ComponentCache<IChatBaseComponent, BaseComponent[]> componentCache = new ComponentCache<>(10000,
            (component, clientVersion) -> ComponentSerializer.parse(component.toString(clientVersion)));

    /** Player's NMS handle (EntityPlayer), preloading for speed */
    private Object handle;

    /** Player's connection for sending packets, preloading for speed */
    @Getter private Object playerConnection;
    
    /** Bukkit BossBars the player can currently see */
    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    /** ViaVersion BossBars this 1.9+ player can see on 1.8 server */
    private final Map<UUID, com.viaversion.viaversion.api.legacy.bossbar.BossBar> viaBossBars = new HashMap<>();

    /** Player's scoreboard */
    @Getter private final Scoreboard scoreboard = new BukkitScoreboard(this);

    /** Player's tablist */
    @Getter private final TabList tabList = getVersion().getMinorVersion() >= 8 ?
            new BulkUpdateBukkitTabList(this) : new SingleUpdateBukkitTabList(this);

    /**
     * Constructs new instance with given bukkit player and protocol version
     *
     * @param   p
     *          bukkit player
     * @param   protocolVersion
     *          Player's protocol network id
     */
    public BukkitTabPlayer(Player p, int protocolVersion) {
        super(p, p.getUniqueId(), p.getName(), TAB.getInstance().getConfiguration().getServerName(), p.getWorld().getName(), protocolVersion, true);
        try {
            handle = NMSStorage.getInstance().getHandle.invoke(player);
            playerConnection = NMSStorage.getInstance().PLAYER_CONNECTION.get(handle);
        } catch (ReflectiveOperationException e) {
            TAB.getInstance().getErrorManager().printError("Failed to get playerConnection of " + p.getName(), e);
        }
    }

    @Override
    public boolean hasPermission(@NonNull String permission) {
        long time = System.nanoTime();
        boolean value = getPlayer().hasPermission(permission);
        TAB.getInstance().getCPUManager().addMethodTime("hasPermission", System.nanoTime()-time);
        return value;
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

    @Override
    public void sendPacket(Object nmsPacket) {
        if (nmsPacket == null || !getPlayer().isOnline()) return;
        long time = System.nanoTime();
        try {
            NMSStorage.getInstance().sendPacket.invoke(playerConnection, nmsPacket);
        } catch (ReflectiveOperationException e) {
            TAB.getInstance().getErrorManager().printError("An error occurred when sending " + nmsPacket.getClass().getSimpleName(), e);
        }
        TAB.getInstance().getCPUManager().addMethodTime("sendPacket", System.nanoTime()-time);
    }

    @Override
    public void sendMessage(IChatBaseComponent message) {
        if (spigot) {
            getPlayer().spigot().sendMessage(componentCache.get(message, version));
        } else {
            getPlayer().sendMessage(message.toLegacyText());
        }
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
    public Skin getSkin() {
        try {
            Collection<Property> col = ((GameProfile)NMSStorage.getInstance().getProfile.invoke(handle)).getProperties().get("textures");
            if (col.isEmpty()) return null; //offline mode
            Property property = col.iterator().next();
            return new Skin(property.getValue(), property.getSignature());
        } catch (ReflectiveOperationException e) {
            TAB.getInstance().getErrorManager().printError("Failed to get skin of " + getName(), e);
            return null;
        }
    }

    @Override
    public Player getPlayer() {
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

    @SuppressWarnings("deprecation")
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
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendBossBar(@NonNull UUID id, @NonNull String title, float progress, me.neznamy.tab.api.bossbar.@NonNull BarColor color, me.neznamy.tab.api.bossbar.@NonNull BarStyle style) {
        String convertedTitle = RGBUtils.getInstance().convertToBukkitFormat(title,
                getVersion().getMinorVersion() >= 16 && TAB.getInstance().getServerVersion().getMinorVersion() >= 16);
        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 9) {
            if (bossBars.containsKey(id)) return;
            BossBar bar = Bukkit.createBossBar(
                    convertedTitle,
                    BarColor.valueOf(color.name()),
                    BarStyle.valueOf(style.getBukkitName()));
            bar.setProgress(progress);
            bar.addPlayer(getPlayer());
            bossBars.put(id, bar);
        } else if (getVersion().getMinorVersion() >= 9) {
            if (viaBossBars.containsKey(id)) return;
            com.viaversion.viaversion.api.legacy.bossbar.BossBar bar = Via.getAPI().legacyAPI().createLegacyBossBar(
                    convertedTitle,
                    progress,
                    BossColor.valueOf(color.name()),
                    BossStyle.valueOf(style.getBukkitName()));
            viaBossBars.put(id, bar);
            bar.addPlayer(getPlayer().getUniqueId());
        } else {
            DataWatcher w = new DataWatcher();
            float health = 300*progress;
            if (health == 0) health = 1;
            w.getHelper().setHealth(health);
            w.getHelper().setCustomName(title, getVersion());
            w.getHelper().setEntityFlags((byte) 32);
            w.getHelper().setWitherInvulnerableTime(880); // Magic number
            spawnEntity(id.hashCode(), new UUID(0, 0), EntityType.WITHER, new Location(0, 0, 0, 0, 0), w);
        }
    }

    @Override
    public void updateBossBar(@NonNull UUID id, @NonNull String title) {
        String convertedTitle = RGBUtils.getInstance().convertToBukkitFormat(title,
                getVersion().getMinorVersion() >= 16 && TAB.getInstance().getServerVersion().getMinorVersion() >= 16);
        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 9) {
            bossBars.get(id).setTitle(convertedTitle);
        } else if (getVersion().getMinorVersion() >= 9){
            viaBossBars.get(id).setTitle(convertedTitle);
        } else {
            DataWatcher w = new DataWatcher();
            w.getHelper().setCustomName(title, getVersion());
            updateEntityMetadata(id.hashCode(), w);
        }
    }

    @Override
    public void updateBossBar(@NonNull UUID id, float progress) {
        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 9) {
            bossBars.get(id).setProgress(progress);
        } else if (getVersion().getMinorVersion() >= 9) {
            viaBossBars.get(id).setHealth(progress);
        } else {
            DataWatcher w = new DataWatcher();
            float health = 300*progress;
            if (health == 0) health = 1;
            w.getHelper().setHealth(health);
            updateEntityMetadata(id.hashCode(), w);
        }
    }

    @Override
    public void updateBossBar(@NonNull UUID id, me.neznamy.tab.api.bossbar.@NonNull BarStyle style) {
        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 9) {
            bossBars.get(id).setStyle(BarStyle.valueOf(style.getBukkitName()));
        } else if (getVersion().getMinorVersion() >= 9) {
            viaBossBars.get(id).setStyle(BossStyle.valueOf(style.getBukkitName()));
        }
    }

    @Override
    public void updateBossBar(@NonNull UUID id, me.neznamy.tab.api.bossbar.@NonNull BarColor color) {
        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 9) {
            bossBars.get(id).setColor(BarColor.valueOf(color.name()));
        } else if (getVersion().getMinorVersion() >= 9) {
            viaBossBars.get(id).setColor(BossColor.valueOf(color.name()));
        }
    }

    @Override
    public void removeBossBar(@NonNull UUID id) {
        if (TAB.getInstance().getServerVersion().getMinorVersion() >= 9) {
            bossBars.remove(id).removePlayer(getPlayer());
        } else if (getVersion().getMinorVersion() >= 9) {
            viaBossBars.remove(id).removePlayer(getPlayer().getUniqueId());
        } else {
            destroyEntities(id.hashCode());
        }
    }

    @Override
    public void spawnEntity(int entityId, UUID id, Object entityType, Location location, EntityData data) {
        try {
            sendPacket(PacketPlayOutSpawnEntityLivingStorage.build(entityId, id, entityType, location, data));
            if (TAB.getInstance().getServerVersion().getMinorVersion() < 15) {
                updateEntityMetadata(entityId, data);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateEntityMetadata(int entityId, EntityData data) {
        try {
            if (PacketPlayOutEntityMetadataStorage.CONSTRUCTOR.getParameterCount() == 2) {
                //1.19.3+
                sendPacket(PacketPlayOutEntityMetadataStorage.CONSTRUCTOR.newInstance(entityId, DataWatcher.packDirty.invoke(data.build())));
            } else {
                sendPacket(PacketPlayOutEntityMetadataStorage.CONSTRUCTOR.newInstance(entityId, data.build(), true));
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void teleportEntity(int entityId, Location location) {
        try {
            sendPacket(PacketPlayOutEntityTeleportStorage.build(entityId, location));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }
    }
}
