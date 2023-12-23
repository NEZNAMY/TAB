package me.neznamy.tab.shared.backend.features.unlimitedtags;

import lombok.Getter;
import me.neznamy.tab.shared.features.nametags.unlimited.ArmorStandManager;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.features.nametags.unlimited.NameTagX;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BackendArmorStandManager implements ArmorStandManager {

    /** Space in blocks between armor stand lines */
    private final double SPACE_BETWEEN_LINES = 0.26;

    private final BackendNameTagX nameTagX;

    @Getter private boolean sneaking;

    /** Armor stands in an array for speed while iterating */
    private final ArmorStand[] armorStandArray;

    /** Players in entity tracking range of owner */
    private final List<BackendTabPlayer> nearbyPlayerList = new ArrayList<>();

    /** Nearby players in an array for speed while iterating */
    @Getter private BackendTabPlayer[] nearbyPlayers = new BackendTabPlayer[0];

    /**
     * Constructs new instance with given parameters and loads armor stands.
     *
     * @param   nameTagX
     *          Main feature
     * @param   owner
     *          Owner of this armor stand manager
     */
    public BackendArmorStandManager(@NotNull NameTagX nameTagX, @NotNull TabPlayer owner) {
        this.nameTagX = (BackendNameTagX) nameTagX;
        sneaking = this.nameTagX.isSneaking(owner);
        owner.setProperty(nameTagX, TabConstants.Property.NAMETAG, owner.getProperty(TabConstants.Property.TAGPREFIX).getCurrentRawValue()
                + owner.getProperty(TabConstants.Property.CUSTOMTAGNAME).getCurrentRawValue()
                + owner.getProperty(TabConstants.Property.TAGSUFFIX).getCurrentRawValue());
        double height = 0;
        List<ArmorStand> armorStands = new ArrayList<>();
        for (String line : nameTagX.getDynamicLines()) {
            armorStands.add(new ArmorStand((BackendNameTagX) nameTagX, this, owner, line, height, false));
            height += SPACE_BETWEEN_LINES;
        }
        for (Map.Entry<String, Object> line : nameTagX.getStaticLines().entrySet()) {
            armorStands.add(new ArmorStand((BackendNameTagX) nameTagX, this, owner, line.getKey(), Double.parseDouble(line.getValue().toString()), true));
        }
        armorStandArray = armorStands.toArray(new ArmorStand[0]);
        fixArmorStandHeights();
    }

    /**
     * Teleports armor stands to player's current location for specified viewer
     *
     * @param   viewer
     *          player to teleport armor stands for
     */
    public void teleport(@NotNull BackendTabPlayer viewer) {
        for (ArmorStand a : armorStandArray) a.teleport(viewer);
    }

    /**
     * Teleports armor stands to player's current location for all nearby players
     */
    public void teleport() {
        for (ArmorStand a : armorStandArray) a.teleport();
    }

    /**
     * Returns {@code true} if requested player is nearby, {@code false} if not
     *
     * @param   viewer
     *          Player to check for
     * @return  {@code true} if player nearby, {@code false} if not
     */
    public boolean isNearby(@NotNull BackendTabPlayer viewer) {
        return nearbyPlayerList.contains(viewer);
    }

    /**
     * Sets sneak value of armor stands to specified value
     *
     * @param   sneaking
     *          new sneaking status
     */
    public void sneak(boolean sneaking) {
        this.sneaking = sneaking;
        for (BackendTabPlayer viewer : nearbyPlayers) {
            if (viewer.getVersion().getMinorVersion() == 14 && !nameTagX.isArmorStandsAlwaysVisible()) {
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

    /**
     * Performs respawn operation on all armor stands to skip teleport animation
     */
    public void respawn() {
        for (BackendTabPlayer viewer : nearbyPlayers) {
            respawn(viewer);
        }
    }

    public void respawn(@NotNull BackendTabPlayer viewer) {
        // 1.8.0 will not see entity that respawned in the same tick
        // creating new delayed task every time someone sneaks can be abused and cause OOM
        // RIP 1.8.0
        for (ArmorStand as : armorStandArray) {
            viewer.getEntityView().destroyEntities(as.getEntityId());
        }
        for (ArmorStand a : armorStandArray) {
            a.spawn(viewer);
        }
    }

    /**
     * Spawns all armor stands for specified viewer and adds them into nearby players
     *
     * @param   viewer
     *          player to spawn armor stands for
     */
    public void spawn(@NotNull BackendTabPlayer viewer) {
        nearbyPlayerList.add(viewer);
        nearbyPlayers = nearbyPlayerList.toArray(new BackendTabPlayer[0]);
        if (viewer.getVersion().getMinorVersion() < 8) return;
        for (ArmorStand a : armorStandArray) a.spawn(viewer);
    }

    /**
     * Fixes heights of all armor stands due to dynamic lines
     */
    public void fixArmorStandHeights() {
        double currentY = -SPACE_BETWEEN_LINES;
        for (ArmorStand as : armorStandArray) {
            if (as.isStaticOffset()) continue;
            if (!as.getProperty().get().isEmpty()) {
                currentY += SPACE_BETWEEN_LINES;
                as.setOffset(currentY);
            }
        }
    }

    /**
     * Removes specified player from list of nearby players
     *
     * @param   viewer
     *          player to remove
     */
    public void unregisterPlayer(@NotNull BackendTabPlayer viewer) {
        if (nearbyPlayerList.remove(viewer)) nearbyPlayers = nearbyPlayerList.toArray(new BackendTabPlayer[0]);
    }

    public void updateVisibility(boolean force) {
        for (ArmorStand a : armorStandArray) a.updateVisibility(force);
    }

    /**
     * Sends destroy packet of all armor stands to specified player and removes them from nearby players list
     *
     * @param   viewer
     *          player to destroy armor stands for
     */
    public void destroy(@NotNull BackendTabPlayer viewer) {
        for (ArmorStand as : armorStandArray) {
            viewer.getEntityView().destroyEntities(as.getEntityId());
        }
        unregisterPlayer(viewer);
    }

    @Override
    public void destroy() {
        for (BackendTabPlayer viewer : nearbyPlayers) {
            for (ArmorStand as : armorStandArray) {
                viewer.getEntityView().destroyEntities(as.getEntityId());
            }
        }
        nearbyPlayerList.clear();
        nearbyPlayers = new BackendTabPlayer[0];
    }

    @Override
    public void refresh(boolean force) {
        boolean fix = false;
        for (ArmorStand as : armorStandArray) {
            if (as.getProperty().update() || force) {
                as.refresh();
                fix = true;
            }
        }
        if (fix) fixArmorStandHeights();
    }

    public void updateMetadata(@NotNull BackendTabPlayer viewer) {
        for (ArmorStand a : armorStandArray) {
            viewer.getEntityView().updateEntityMetadata(a.entityId, a.createDataWatcher(a.getProperty().getFormat(viewer), viewer));
        }
    }
}
