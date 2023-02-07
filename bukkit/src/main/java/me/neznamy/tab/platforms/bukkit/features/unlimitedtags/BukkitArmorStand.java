package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import lombok.Getter;
import me.neznamy.tab.api.*;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.protocol.TabPacket;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityDestroy;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityMetadata;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityTeleport;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutSpawnEntityLiving;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;

import java.util.UUID;

/**
 * A class representing an armor stand attached to a player
 */
public class BukkitArmorStand implements ArmorStand {

    /** Entity id counter to pick unique entity ID for each armor stand */
    private static int idCounter = 2000000000;

    /** NameTag feature */
    private final BukkitNameTagX manager = (BukkitNameTagX) TabAPI.getInstance().getFeatureManager().getFeature(TabConstants.Feature.UNLIMITED_NAME_TAGS);

    /** Armor stand manager which this armor stand belongs to */
    private final BukkitArmorStandManager asm;
    
    /** Owner of the armor stand */
    private final TabPlayer owner;

    /** Owner as a Bukkit player */
    private final Player player;

    /** Offset in blocks, 0 for original height */
    @Getter private double offset;

    /** If offset is static, or dynamic based on other armor stands */
    private final boolean staticOffset;

    /** Entity ID of this armor stand */
    @Getter private final int entityId = idCounter++;

    /** Unique ID of this armor stand */
    private final UUID uuid = UUID.randomUUID();

    /** Sneaking flag of armor stands */
    private boolean sneaking;

    /** Armor stand visibility */
    private boolean visible;

    /** Refresh property dedicated to this armor stand */
    @Getter private final Property property;

    /** Entity destroy packet */
    private final PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(entityId);

    /**
     * Constructs new instance with given parameters.
     *
     * @param   asm
     *          Armor stand manager which this armor stand belongs to
     * @param   owner
     *          Owner of the armor stand
     * @param   propertyName
     *          Name of refresh property to use
     * @param   yOffset
     *          Offset in blocks
     * @param   staticOffset
     *          {@code true} if offset is static, {@code false} if not
     */
    public BukkitArmorStand(BukkitArmorStandManager asm, TabPlayer owner, String propertyName, double yOffset, boolean staticOffset) {
        this.asm = asm;
        this.owner = owner;
        this.staticOffset = staticOffset;
        player = (Player) owner.getPlayer();
        this.offset = yOffset;
        this.property = owner.getProperty(propertyName);
        visible = getVisibility();
        sneaking = player.isSneaking();
    }

    @Override
    public void refresh() {
        visible = getVisibility();
        updateMetadata();
    }

    @Override
    public boolean hasStaticOffset() {
        return staticOffset;
    }

    @Override
    public void setOffset(double offset) {
        if (this.offset == offset) return;
        this.offset = offset;
        for (TabPlayer all : asm.getNearbyPlayers()) {
            all.sendCustomPacket(getTeleportPacket(all), TabConstants.PacketCategory.UNLIMITED_NAMETAGS_OFFSET_CHANGE);
        }
    }

    @Override
    public void spawn(TabPlayer viewer) {
        for (TabPacket packet : getSpawnPackets(viewer)) {
            viewer.sendCustomPacket(packet, TabConstants.PacketCategory.UNLIMITED_NAMETAGS_SPAWN);
        }
    }

    @Override
    public void destroy() {
        for (TabPlayer all : asm.getNearbyPlayers()) all.sendCustomPacket(destroyPacket, TabConstants.PacketCategory.UNLIMITED_NAMETAGS_DESPAWN);
    }
    
    @Override
    public void destroy(TabPlayer viewer) {
        viewer.sendCustomPacket(destroyPacket, TabConstants.PacketCategory.UNLIMITED_NAMETAGS_DESPAWN);
    }

    @Override
    public void teleport() {
        for (TabPlayer all : asm.getNearbyPlayers()) {
            all.sendCustomPacket(getTeleportPacket(all), TabConstants.PacketCategory.UNLIMITED_NAMETAGS_TELEPORT);
        }
    }

    @Override
    public void teleport(TabPlayer viewer) {
        if (!asm.isNearby(viewer) && viewer != owner) {
            asm.spawn(viewer);
        } else {
            viewer.sendCustomPacket(getTeleportPacket(viewer), TabConstants.PacketCategory.UNLIMITED_NAMETAGS_TELEPORT);
        }
    }

    @Override
    public void sneak(boolean sneaking) {
        if (this.sneaking == sneaking) return; //idk
        this.sneaking = sneaking;
        for (TabPlayer viewer : asm.getNearbyPlayers()) {
            if (viewer.getVersion().getMinorVersion() == 14 && !manager.isArmorStandsAlwaysVisible()) {
                //1.14.x client sided bug, de-spawning completely
                if (sneaking) {
                    viewer.sendCustomPacket(destroyPacket, TabConstants.PacketCategory.UNLIMITED_NAMETAGS_SNEAK);
                } else {
                    spawn(viewer);
                }
            } else {
                //respawning so there's no animation and it's instant
                respawn(viewer);
            }
        }
    }

    @Override
    public void updateVisibility(boolean force) {
        boolean visibility = getVisibility();
        if (visible != visibility || force) {
            refresh();
        }
    }

    /**
     * Returns teleport packet for specified viewer
     *
     * @param   viewer
     *          player to get location for
     * @return  teleport packet
     */
    public PacketPlayOutEntityTeleport getTeleportPacket(TabPlayer viewer) {
        return new PacketPlayOutEntityTeleport(entityId, getArmorStandLocationFor(viewer));
    }

    /**
     * Updates armor stand's metadata
     */
    public void updateMetadata() {
        for (TabPlayer viewer : asm.getNearbyPlayers()) {
            viewer.sendCustomPacket(new PacketPlayOutEntityMetadata(entityId, createDataWatcher(property.getFormat(viewer), viewer)), TabConstants.PacketCategory.UNLIMITED_NAMETAGS_METADATA);
        }
    }

    /**
     * Returns general visibility rule for everyone with limited info
     *
     * @return  {@code true} if armor stand should be visible, {@code false} if not
     */
    public boolean getVisibility() {
        if (manager.isArmorStandsAlwaysVisible()) return true;
        if (owner.isDisguised() || manager.getVehicleManager().isOnBoat(owner)) return false;
        return !owner.hasInvisibilityPotion() && owner.getGamemode() != 3 && !manager.hasHiddenNametag(owner) && property.get().length() > 0;
    }

    /**
     * Returns location where armor stand should be at time of calling.
     * This takes into account everything that affects height, including
     * viewer's game version.
     *
     * @param   viewer
     *          Player looking at the armor stand
     * @return  Location where armor stand should be for specified viewer
     */
    public Location getLocation(TabPlayer viewer) {
        double x = player.getLocation().getX();
        double y = getY() + offset;
        double z = player.getLocation().getZ();
        if (!player.isSleeping()) {
            if (sneaking) {
                if (viewer.getVersion().getMinorVersion() >= 15) {
                    y += 1.37;
                } else if (viewer.getVersion().getMinorVersion() >= 9) {
                    y += 1.52;
                } else {
                    y += 1.7;
                }
            } else {
                y += viewer.getVersion().getMinorVersion() >= 9 ? 1.8 : 1.84; // Normal
            }
        } else {
            y += viewer.getVersion().getMinorVersion() >= 9 ? 0.2 : 0.26; // Sleeping
        }
        return new Location(null,x,y,z);
    }

    /**
     * Returns Y where player is based on player's vehicle due to bukkit API bug
     *
     * @return  correct player's Y
     */
    protected double getY() {
        //1.14+ server sided bug
        Entity vehicle = player.getVehicle();
        if (vehicle != null) {
            if (vehicle.getType().toString().contains("HORSE")) { //covering all 3 horse types
                return vehicle.getLocation().getY() + 0.85;
            }
            if (vehicle.getType().toString().equals("DONKEY")) { //1.11+
                return vehicle.getLocation().getY() + 0.525;
            }
            if (vehicle.getType() == EntityType.PIG) {
                return vehicle.getLocation().getY() + 0.325;
            }
            if (vehicle.getType().toString().equals("STRIDER")) { //1.16+
                return vehicle.getLocation().getY() + 1.15;
            }
        }
        //1.13+ swimming or 1.9+ flying with elytra
        if (isSwimming() || (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 9 && player.isGliding())) {
            return player.getLocation().getY()-1.22;
        }
        return player.getLocation().getY();
    }

    /**
     * Returns {@code true} if owner is swimming, {@code false} if not
     * @return  {@code true} if owner is swimming, {@code false} if not
     */
    private boolean isSwimming() {
        if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 14 && player.getPose() == Pose.SWIMMING) return true;
        return TabAPI.getInstance().getServerVersion().getMinorVersion() == 13 && player.isSwimming();
    }

    /**
     * Creates data watcher with specified display name for viewer
     *
     * @param   displayName
     *          armor stand name
     * @param   viewer
     *          player to apply checks against
     * @return  DataWatcher for viewer
     */
    public DataWatcher createDataWatcher(String displayName, TabPlayer viewer) {
        DataWatcher datawatcher = new DataWatcher();

        byte flag = 32; //invisible
        if (sneaking) flag += (byte)2;
        datawatcher.getHelper().setEntityFlags(flag);
        datawatcher.getHelper().setCustomName(displayName, viewer.getVersion());

        boolean visibility;
        if (isNameVisiblyEmpty(displayName) || !((Player) viewer.getPlayer()).canSee(player) ||
                manager.hasHiddenNametag(owner, viewer) || manager.hasHiddenNameTagVisibilityView(viewer)) {
            visibility = false;
        } else {
            visibility = visible;
        }
        datawatcher.getHelper().setCustomNameVisible(visibility);

        if (viewer.getVersion().getMinorVersion() > 8 || manager.isMarkerFor18x()) datawatcher.getHelper().setArmorStandFlags((byte)16);
        return datawatcher;
    }

    /**
     * Returns {@code true} if display name is in fact empty,
     * for example only containing color codes, {@code false} if not.
     *
     * @param   displayName
     *          string to check
     * @return  {@code true} if it's empty, {@code false} if not
     */
    private boolean isNameVisiblyEmpty(String displayName) {
        if (displayName.length() == 0) return true;
        if (!displayName.startsWith(EnumChatFormat.COLOR_STRING) && !displayName.startsWith("&") && !displayName.startsWith("#")) return false;
        String text = IChatBaseComponent.fromColoredText(displayName).toRawText();
        if (text.contains(" ")) text = text.replace(" ", "");
        return text.length() == 0;
    }

    /**
     * Returns list of packets to send to make armor stand spawn with metadata
     *
     * @param   viewer
     *          viewer to apply relational placeholders for
     * @return  List of packets that spawn the armor stand
     */
    public TabPacket[] getSpawnPackets(TabPlayer viewer) {
        visible = getVisibility();
        DataWatcher dataWatcher = createDataWatcher(property.getFormat(viewer), viewer);
        if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 15) {
            return new TabPacket[] {
                    new PacketPlayOutSpawnEntityLiving(entityId, uuid, EntityType.ARMOR_STAND, getArmorStandLocationFor(viewer), null),
                    new PacketPlayOutEntityMetadata(entityId, dataWatcher)
            };
        } else {
            return new TabPacket[] {
                    new PacketPlayOutSpawnEntityLiving(entityId, uuid, EntityType.ARMOR_STAND, getArmorStandLocationFor(viewer), dataWatcher),
            };
        }
    }

    /**
     * Returns location where armor stand should be for specified viewer
     *
     * @param   viewer
     *          player to get location for
     * @return  location of armor stand
     */
    public Location getArmorStandLocationFor(TabPlayer viewer) {
        return viewer.getVersion().getMinorVersion() == 8 && !manager.isMarkerFor18x() ? getLocation(viewer).add(0,-2,0) : getLocation(viewer);
    }

    @Override
    public void respawn(TabPlayer viewer) {
        viewer.sendCustomPacket(destroyPacket, TabConstants.PacketCategory.UNLIMITED_NAMETAGS_DESPAWN);
        // 1.8.0 will not see entity that respawned in the same tick
        // creating new delayed task every time someone sneaks can be abused and cause OOM
        spawn(viewer);
    }
}