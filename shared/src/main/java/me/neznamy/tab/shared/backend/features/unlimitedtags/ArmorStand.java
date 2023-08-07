package me.neznamy.tab.shared.backend.features.unlimitedtags;

import lombok.Getter;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.backend.EntityData;
import me.neznamy.tab.shared.backend.Location;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ArmorStand {

    /** Entity id counter to pick unique entity ID for each armor stand */
    private static int idCounter = 2000000000;

    /** NameTag feature */
    protected final BackendNameTagX manager;

    /** Armor stand manager which this armor stand belongs to */
    protected final BackendArmorStandManager asm;

    /** Owner of the armor stand */
    protected final TabPlayer owner;

    /** Offset in blocks, 0 for original height */
    @Getter protected double offset;

    /** If offset is static, or dynamic based on other armor stands */
    @Getter private final boolean staticOffset;

    /** Entity ID of this armor stand */
    @Getter protected final int entityId = idCounter++;

    /** Unique ID of this armor stand */
    protected final UUID uuid = UUID.randomUUID();

    /** Armor stand visibility to use in metadata */
    protected boolean visible;

    /** Visibility flag to compare against for previous value, including invisibility potion compensation */
    private boolean visibleWPotion;

    /** Refresh property dedicated to this armor stand */
    @Getter protected final Property property;

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
    public ArmorStand(@NotNull BackendNameTagX feature, @NotNull BackendArmorStandManager asm, @NotNull TabPlayer owner,
                      @NotNull String propertyName, double yOffset, boolean staticOffset) {
        this.manager = feature;
        this.asm = asm;
        this.owner = owner;
        this.staticOffset = staticOffset;
        this.offset = yOffset;
        this.property = owner.getProperty(propertyName);
        visible = calculateVisibility();
        visibleWPotion = visible && !owner.hasInvisibilityPotion();
    }

    public void setOffset(double offset) {
        if (this.offset == offset) return;
        this.offset = offset;
        for (BackendTabPlayer all : asm.getNearbyPlayers()) {
            sendTeleportPacket(all);
        }
    }

    public void refresh() {
        visible = calculateVisibility();
        visibleWPotion = visible && !owner.hasInvisibilityPotion();
        updateMetadata();
    }

    public void updateVisibility(boolean force) {
        boolean visibility = calculateVisibility() && !owner.hasInvisibilityPotion(); //trigger packet send but don't save, so it can be reverted if viewer is spectator
        if (visibleWPotion != visibility || force) {
            refresh();
        }
    }

    public void teleport() {
        for (BackendTabPlayer all : asm.getNearbyPlayers()) {
            sendTeleportPacket(all);
        }
    }

    public void teleport(BackendTabPlayer viewer) {
        if (!asm.isNearby(viewer) && viewer != owner) {
            asm.spawn(viewer);
        } else {
            sendTeleportPacket(viewer);
        }
    }

    public void spawn(BackendTabPlayer viewer) {
        visible = calculateVisibility();
        viewer.getEntityView().spawnEntity(entityId, uuid, manager.getArmorStandType(),
                new Location(manager.getX(owner), getYLocation(viewer), manager.getZ(owner)),
                createDataWatcher(property.getFormat(viewer), viewer));
    }

    /**
     * Returns general visibility rule for everyone with limited info
     *
     * @return  {@code true} if armor stand should be visible, {@code false} if not
     */
    public boolean calculateVisibility() {
        if (manager.isArmorStandsAlwaysVisible()) return true;
        if (owner.isDisguised() || manager.isOnBoat(owner)) return false;
        return owner.getGamemode() != 3 && !manager.hasHiddenNameTag(owner) && property.get().length() > 0 &&
                !manager.getUnlimitedDisableChecker().isDisabledPlayer(owner);
    }

    /**
     * Returns {@code true} if display name is in fact empty,
     * for example only containing color codes, {@code false} if not.
     *
     * @param   displayName
     *          string to check
     * @return  {@code true} if it's empty, {@code false} if not
     */
    protected boolean isNameVisiblyEmpty(@NotNull String displayName) {
        if (displayName.length() == 0) return true;
        String rawText = displayName.contains(" ") ? displayName.replace(" ", "") : displayName;
        if (!rawText.startsWith(EnumChatFormat.COLOR_STRING) && !rawText.startsWith("&") && !rawText.startsWith("#")) return false;
        return IChatBaseComponent.fromColoredText(rawText).toRawText().length() == 0;
    }

    /**
     * Gets Y to add based on player's pose, armor stand offset and viewer
     *
     * @param   sleeping
     *          Whether player is sleeping or not
     * @param   sneaking
     *          Whether player is sneaking or not
     * @param   viewer
     *          Viewer of the armor stand
     * @return  Y to add to player's location
     */
    public double getYAdd(boolean sleeping, boolean sneaking, @NotNull TabPlayer viewer) {
        double y = getOffset();
        if (!sleeping) {
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
        return y;
    }

    /**
     * Updates armor stand's metadata for everyone
     */
    public void updateMetadata() {
        for (BackendTabPlayer viewer : asm.getNearbyPlayers()) {
            viewer.getEntityView().updateEntityMetadata(entityId, createDataWatcher(property.getFormat(viewer), viewer));
        }
    }

    public boolean shouldBeInvisibleFor(@NotNull TabPlayer viewer, @NotNull String displayName) {
        return isNameVisiblyEmpty(displayName) || !manager.canSee(viewer, owner) ||
                manager.hasHiddenNameTag(owner, viewer) || manager.hasHiddenNameTagVisibilityView(viewer) ||
                (owner.hasInvisibilityPotion() && viewer.getGamemode() != 3);
    }

    public void sendTeleportPacket(@NotNull BackendTabPlayer viewer) {
        viewer.getEntityView().teleportEntity(entityId, new Location(manager.getX(owner), getYLocation(viewer), manager.getZ(owner)));
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
    public EntityData createDataWatcher(@NotNull String displayName, @NotNull TabPlayer viewer) {
        byte flags = (byte) (manager.isSneaking(owner) ? 34 : 32);
        boolean nameVisible = !shouldBeInvisibleFor(viewer, displayName) && visible;
        return manager.createDataWatcher(viewer, flags, displayName, nameVisible);
    }

    /**
     * Returns Y location where armor stand should be at time of calling.
     * This takes into account everything that affects height, including
     * viewer's game version.
     *
     * @param   viewer
     *          Player looking at the armor stand
     * @return  Location where armor stand should be for specified viewer
     */
    public double getYLocation(@NotNull TabPlayer viewer) {
        double y = manager.getY(owner.getPlayer());
        //1.14+ server sided bug
        Object vehicle = manager.getVehicle(owner);
        if (vehicle != null) {
            String type = manager.getEntityType(vehicle);
            double vehicleY = manager.getY(vehicle);
            if (type.contains("horse")) { //covering all 3 horse types
                y = vehicleY + 0.85;
            }
            if (type.equals("donkey")) { //1.11+
                y = vehicleY + 0.525;
            }
            if (type.equals("pig")) {
                y = vehicleY + 0.325;
            }
            if (type.equals("strider")) { //1.16+
                y = vehicleY + 1.15;
            }
        } else {
            //1.13+ swimming or 1.9+ flying with elytra
            if (manager.isSwimming(owner) || manager.isGliding(owner)) {
                y = manager.getY(owner.getPlayer())-1.22;
            }
        }
        y += getYAdd(manager.isSleeping(owner), manager.isSneaking(owner), viewer);
        return y;
    }
}
