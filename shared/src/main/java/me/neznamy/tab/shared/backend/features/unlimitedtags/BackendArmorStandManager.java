package me.neznamy.tab.shared.backend.features.unlimitedtags;

import lombok.Getter;
import me.neznamy.tab.api.*;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.tab.shared.features.nametags.unlimited.NameTagX;

import java.util.*;

public class BackendArmorStandManager implements ArmorStandManager {

    private final BackendNameTagX nameTagX;

    @Getter private boolean sneaking;

    /** Map of registered armor stands by name */
    private final Map<String, BackendArmorStand> armorStands = new LinkedHashMap<>();

    /** Armor stands in an array for speed while iterating */
    private BackendArmorStand[] armorStandArray;

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
    public BackendArmorStandManager(NameTagX nameTagX, TabPlayer owner) {
        this.nameTagX = (BackendNameTagX) nameTagX;
        sneaking = this.nameTagX.isSneaking(owner);
        owner.setProperty(nameTagX, TabConstants.Property.NAMETAG, owner.getProperty(TabConstants.Property.TAGPREFIX).getCurrentRawValue()
                + owner.getProperty(TabConstants.Property.CUSTOMTAGNAME).getCurrentRawValue()
                + owner.getProperty(TabConstants.Property.TAGSUFFIX).getCurrentRawValue());
        double height = 0;
        for (String line : nameTagX.getDynamicLines()) {
            armorStands.put(line, new BackendArmorStand((BackendNameTagX) nameTagX, this, owner, line, height, false));
            height += 0.26;
        }
        for (Map.Entry<String, Object> line : nameTagX.getStaticLines().entrySet()) {
            armorStands.put(line.getKey(), new BackendArmorStand((BackendNameTagX) nameTagX, this, owner, line.getKey(), Double.parseDouble(line.getValue().toString()), true));
        }
        armorStandArray = armorStands.values().toArray(new BackendArmorStand[0]);
        fixArmorStandHeights();
    }

    /**
     * Teleports armor stands to player's current location for specified viewer
     *
     * @param   viewer
     *          player to teleport armor stands for
     */
    public void teleport(BackendTabPlayer viewer) {
        for (BackendArmorStand a : armorStandArray) a.teleport(viewer);
    }

    /**
     * Teleports armor stands to player's current location for all nearby players
     */
    public void teleport() {
        for (BackendArmorStand a : armorStandArray) a.teleport();
    }

    /**
     * Returns {@code true} if requested player is nearby, {@code false} if not
     *
     * @param   viewer
     *          Player to check for
     * @return  {@code true} if player nearby, {@code false} if not
     */
    public boolean isNearby(BackendTabPlayer viewer) {
        return nearbyPlayerList.contains(viewer);
    }

    /**
     * Returns {@code true} if manager contains armor stand with specified entity id, {@code false} if not
     *
     * @param   entityId
     *          entity id
     * @return  {@code true} if armor stand with specified entity id exists, {@code false} if not
     */
    public boolean hasArmorStandWithID(int entityId) {
        for (ArmorStand a : armorStandArray) {
            if (a.getEntityId() == entityId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets sneak value of armor stands to specified value
     *
     * @param   sneaking
     *          new sneaking status
     */
    public void sneak(boolean sneaking) {
        if (this.sneaking == sneaking) return;
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

    public void respawn(BackendTabPlayer viewer) {
        // 1.8.0 will not see entity that respawned in the same tick
        // creating new delayed task every time someone sneaks can be abused and cause OOM
        // RIP 1.8.0
        for (ArmorStand as : armorStandArray) {
            viewer.destroyEntities(as.getEntityId());
        }
        for (BackendArmorStand a : armorStandArray) {
            a.spawn(viewer);
        }
    }

    /**
     * Spawns all armor stands for specified viewer and adds them into nearby players
     *
     * @param   viewer
     *          player to spawn armor stands for
     */
    public void spawn(BackendTabPlayer viewer) {
        nearbyPlayerList.add(viewer);
        nearbyPlayers = nearbyPlayerList.toArray(new BackendTabPlayer[0]);
        if (viewer.getVersion().getMinorVersion() < 8) return;
        for (BackendArmorStand a : armorStandArray) a.spawn(viewer);
    }

    /**
     * Fixes heights of all armor stands due to dynamic lines
     */
    public void fixArmorStandHeights() {
        double currentY = -0.26;
        for (ArmorStand as : armorStandArray) {
            if (as.isStaticOffset()) continue;
            if (as.getProperty().get().length() != 0) {
                currentY += 0.26;
                as.setOffset(currentY);
            }
        }
    }

    /**
     * Adds armor stand to list and registers it to all nearby players
     *
     * @param   name
     *          Unique identifier of the text line
     * @param   as
     *          Armor stand to add
     */
    public void addArmorStand(String name, BackendArmorStand as) {
        armorStands.put(name, as);
        armorStandArray = armorStands.values().toArray(new BackendArmorStand[0]);
        for (BackendTabPlayer p : nearbyPlayers) as.spawn(p);
    }

    /**
     * Removes specified player from list of nearby players
     *
     * @param   viewer
     *          player to remove
     */
    public void unregisterPlayer(BackendTabPlayer viewer) {
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
    public void destroy(BackendTabPlayer viewer) {
        for (ArmorStand as : armorStandArray) {
            viewer.destroyEntities(as.getEntityId());
        }
        unregisterPlayer(viewer);
    }

    @Override
    public void destroy() {
        for (BackendTabPlayer viewer : nearbyPlayers) {
            for (ArmorStand as : armorStandArray) {
                viewer.destroyEntities(as.getEntityId());
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

    public void updateMetadata(BackendTabPlayer viewer) {
        for (BackendArmorStand a : armorStandArray) {
            viewer.updateEntityMetadata(a.entityId, a.createDataWatcher(a.getProperty().getFormat(viewer), viewer));
        }
    }
}
