package me.neznamy.tab.shared.backend.features.unlimitedtags;

import lombok.Getter;
import me.neznamy.tab.api.ArmorStand;
import me.neznamy.tab.api.Property;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;

import java.util.UUID;

public abstract class BackendArmorStand implements ArmorStand {

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

    /** Sneaking flag of armor stands */
    protected boolean sneaking;

    /** Armor stand visibility */
    protected boolean visible;

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
    public BackendArmorStand(BackendNameTagX feature, BackendArmorStandManager asm, TabPlayer owner, String propertyName, double yOffset, boolean staticOffset) {
        this.manager = feature;
        this.asm = asm;
        this.owner = owner;
        this.staticOffset = staticOffset;
        this.offset = yOffset;
        this.property = owner.getProperty(propertyName);
        visible = calculateVisibility();
    }

    @Override
    public void setOffset(double offset) {
        if (this.offset == offset) return;
        this.offset = offset;
        for (TabPlayer all : asm.getNearbyPlayers()) {
            sendTeleportPacket(all);
        }
    }

    @Override
    public void refresh() {
        visible = calculateVisibility();
        updateMetadata();
    }

    @Override
    public void updateVisibility(boolean force) {
        boolean visibility = calculateVisibility();
        if (visible != visibility || force) {
            refresh();
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
                    destroy(viewer);
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
    public void destroy() {
        for (TabPlayer all : asm.getNearbyPlayers())
            destroy(all);
    }

    @Override
    public void teleport() {
        for (TabPlayer all : asm.getNearbyPlayers()) {
            sendTeleportPacket(all);
        }
    }

    @Override
    public void teleport(TabPlayer viewer) {
        if (!asm.isNearby(viewer) && viewer != owner) {
            asm.spawn(viewer);
        } else {
            sendTeleportPacket(viewer);
        }
    }

    /**
     * Returns general visibility rule for everyone with limited info
     *
     * @return  {@code true} if armor stand should be visible, {@code false} if not
     */
    public boolean calculateVisibility() {
        if (manager.isArmorStandsAlwaysVisible()) return true;
        if (owner.isDisguised() || manager.isOnBoat(owner)) return false;
        return owner.getGamemode() != 3 && !manager.hasHiddenNametag(owner) && property.get().length() > 0;
    }

    /**
     * Returns {@code true} if display name is in fact empty,
     * for example only containing color codes, {@code false} if not.
     *
     * @param   displayName
     *          string to check
     * @return  {@code true} if it's empty, {@code false} if not
     */
    protected boolean isNameVisiblyEmpty(String displayName) {
        if (displayName.length() == 0) return true;
        if (!displayName.startsWith(EnumChatFormat.COLOR_STRING) && !displayName.startsWith("&") && !displayName.startsWith("#")) return false;
        String text = IChatBaseComponent.fromColoredText(displayName).toRawText();
        if (text.contains(" ")) text = text.replace(" ", "");
        return text.length() == 0;
    }

    /**
     * Gets Y to add based on player's pose, armor stand offset and viewer
     *
     * @param   sleeping
     *          Whether player is sleeping or not
     * @param   sneaking
     *          Whether player is sneakinig or not
     * @param   viewer
     *          Viewer of the armor stand
     * @return  Y to add to player's location
     */
    public double getYAdd(boolean sleeping, boolean sneaking, TabPlayer viewer) {
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
        if (viewer.getVersion().getMinorVersion() == 8 && !manager.isMarkerFor18x()) y -= 2;
        return y;
    }

    public void respawn(TabPlayer viewer) {
        destroy(viewer);
        // 1.8.0 will not see entity that respawned in the same tick
        // creating new delayed task every time someone sneaks can be abused and cause OOM
        spawn(viewer);
    }

    /**
     * Updates armor stand's metadata for everyone
     */
    public void updateMetadata() {
        for (TabPlayer viewer : asm.getNearbyPlayers()) {
            updateMetadata(viewer);
        }
    }

    /**
     * Updates armor stand's metadata for specified viewer
     */
    public abstract void updateMetadata(TabPlayer viewer);

    public abstract void sendTeleportPacket(TabPlayer viewer);
}
