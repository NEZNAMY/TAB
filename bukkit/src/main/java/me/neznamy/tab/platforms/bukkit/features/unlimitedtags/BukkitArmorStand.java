package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.TabPacket;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityDestroy;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityMetadata;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutEntityTeleport;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOutSpawnEntityLiving;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.shared.backend.features.unlimitedtags.BackendArmorStand;
import me.neznamy.tab.shared.backend.features.unlimitedtags.BackendArmorStandManager;
import me.neznamy.tab.shared.backend.features.unlimitedtags.BackendNameTagX;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;

/**
 * A class representing an armor stand attached to a player
 */
public class BukkitArmorStand extends BackendArmorStand {

    /** Owner as a Bukkit player */
    private final Player player;

    /** Entity destroy packet */
    protected final TabPacket destroyPacket = new PacketPlayOutEntityDestroy(entityId);

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
    public BukkitArmorStand(BackendNameTagX feature, BackendArmorStandManager asm, TabPlayer owner, String propertyName, double yOffset, boolean staticOffset) {
        super(feature, asm, owner, propertyName, yOffset, staticOffset);
        player = (Player) owner.getPlayer();
        sneaking = player.isSneaking();
    }

    @Override
    public void spawn(TabPlayer viewer) {
        for (TabPacket packet : getSpawnPackets(viewer)) {
            viewer.sendCustomPacket(packet, TabConstants.PacketCategory.UNLIMITED_NAMETAGS_SPAWN);
        }
    }

    @Override
    public void destroy(TabPlayer viewer) {
        viewer.sendCustomPacket(destroyPacket, TabConstants.PacketCategory.UNLIMITED_NAMETAGS_DESPAWN);
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
        Location loc = player.getLocation();
        double y = loc.getY();

        //1.14+ server sided bug
        Entity vehicle = player.getVehicle();
        if (vehicle != null) {
            if (vehicle.getType().toString().contains("HORSE")) { //covering all 3 horse types
                y = vehicle.getLocation().getY() + 0.85;
            }
            if (vehicle.getType().toString().equals("DONKEY")) { //1.11+
                y = vehicle.getLocation().getY() + 0.525;
            }
            if (vehicle.getType() == EntityType.PIG) {
                y = vehicle.getLocation().getY() + 0.325;
            }
            if (vehicle.getType().toString().equals("STRIDER")) { //1.16+
                y = vehicle.getLocation().getY() + 1.15;
            }
        } else {
            //1.13+ swimming or 1.9+ flying with elytra
            if (isSwimming() || (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 9 && player.isGliding())) {
                y = loc.getY()-1.22;
            }
        }
        y += getYAdd(player.isSleeping(), sneaking, viewer);
        return new Location(null, loc.getX(), y, loc.getZ());
    }

    @Override
    public void updateMetadata(TabPlayer viewer) {
        viewer.sendCustomPacket(new PacketPlayOutEntityMetadata(entityId, createDataWatcher(property.getFormat(viewer), viewer)), TabConstants.PacketCategory.UNLIMITED_NAMETAGS_METADATA);
    }

    @Override
    public void sendTeleportPacket(TabPlayer viewer) {
        viewer.sendCustomPacket(new PacketPlayOutEntityTeleport(entityId, getLocation(viewer)),
                TabConstants.PacketCategory.UNLIMITED_NAMETAGS_TELEPORT);
    }

    /**
     * Returns {@code true} if owner is swimming, {@code false} if not
     * @return  {@code true} if owner is swimming, {@code false} if not
     */
    private boolean isSwimming() {
        if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 14 && player.getPose() == Pose.SWIMMING) return true;
        return TabAPI.getInstance().getServerVersion().getMinorVersion() == 13 && player.isSwimming();
    }

    private TabPacket[] getSpawnPackets(TabPlayer viewer) {
        visible = calculateVisibility();
        DataWatcher dataWatcher = createDataWatcher(property.getFormat(viewer), viewer);
        if (TabAPI.getInstance().getServerVersion().getMinorVersion() >= 15) {
            return new TabPacket[] {
                    new PacketPlayOutSpawnEntityLiving(entityId, uuid, EntityType.ARMOR_STAND, getLocation(viewer), null),
                    new PacketPlayOutEntityMetadata(entityId, dataWatcher)
            };
        } else {
            return new TabPacket[] {
                    new PacketPlayOutSpawnEntityLiving(entityId, uuid, EntityType.ARMOR_STAND, getLocation(viewer), dataWatcher),
            };
        }
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
        datawatcher.getHelper().setEntityFlags((byte) (sneaking ? 34 : 32));
        datawatcher.getHelper().setCustomName(displayName, viewer.getVersion());
        datawatcher.getHelper().setCustomNameVisible(!shouldBeInvisibleFor(viewer, displayName) && visible);
        if (viewer.getVersion().getMinorVersion() > 8 || manager.isMarkerFor18x())
            datawatcher.getHelper().setArmorStandFlags((byte)16);
        return datawatcher;
    }
}