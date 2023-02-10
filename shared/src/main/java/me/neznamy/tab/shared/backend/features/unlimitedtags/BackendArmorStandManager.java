package me.neznamy.tab.shared.backend.features.unlimitedtags;

import lombok.Getter;
import me.neznamy.tab.api.ArmorStand;
import me.neznamy.tab.api.ArmorStandManager;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.nametags.unlimited.NameTagX;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BackendArmorStandManager implements ArmorStandManager {

    /** Map of registered armor stands by name */
    private final Map<String, ArmorStand> armorStands = new LinkedHashMap<>();

    /** Armor stands in an array for speed while iterating */
    private ArmorStand[] armorStandArray = new ArmorStand[0];

    /** Players in entity tracking range of owner */
    private final List<TabPlayer> nearbyPlayerList = new ArrayList<>();

    /** Nearby players in an array for speed while iterating */
    @Getter
    private TabPlayer[] nearbyPlayers = new TabPlayer[0];

    /**
     * Constructs new instance with given parameters and loads armor stands.
     *
     * @param   nameTagX
     *          Main feature
     * @param   owner
     *          Owner of this armor stand manager
     */
    public BackendArmorStandManager(NameTagX nameTagX, TabPlayer owner) {
        owner.setProperty(nameTagX, TabConstants.Property.NAMETAG, owner.getProperty(TabConstants.Property.TAGPREFIX).getCurrentRawValue()
                + owner.getProperty(TabConstants.Property.CUSTOMTAGNAME).getCurrentRawValue()
                + owner.getProperty(TabConstants.Property.TAGSUFFIX).getCurrentRawValue());
        double height = 0;
        for (String line : nameTagX.getDynamicLines()) {
            addArmorStand(line, ((BackendNameTagX)nameTagX).createArmorStand(this, owner, line, height, false));
            height += 0.26;
        }
        for (Map.Entry<String, Object> line : nameTagX.getStaticLines().entrySet()) {
            addArmorStand(line.getKey(), ((BackendNameTagX)nameTagX).createArmorStand(this, owner, line.getKey(), Double.parseDouble(line.getValue().toString()), true));
        }
        fixArmorStandHeights();
    }

    /**
     * Teleports armor stands to player's current location for specified viewer
     *
     * @param   viewer
     *          player to teleport armor stands for
     */
    public void teleport(TabPlayer viewer) {
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
    public boolean isNearby(TabPlayer viewer) {
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
        for (ArmorStand a : armorStandArray) a.sneak(sneaking);
    }

    /**
     * Performs respawn operation on all armor stands to skip teleport animation
     */
    public void respawn() {
        for (ArmorStand a : armorStandArray) {
            for (TabPlayer viewer : nearbyPlayers) {
                a.respawn(viewer);
            }
        }
    }

    /**
     * Spawns all armor stands for specified viewer and adds them into nearby players
     *
     * @param   viewer
     *          player to spawn armor stands for
     */
    public void spawn(TabPlayer viewer) {
        nearbyPlayerList.add(viewer);
        nearbyPlayers = nearbyPlayerList.toArray(new TabPlayer[0]);
        if (viewer.getVersion().getMinorVersion() < 8) return;
        for (ArmorStand a : armorStandArray) a.spawn(viewer);
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
    public void addArmorStand(String name, ArmorStand as) {
        armorStands.put(name, as);
        armorStandArray = armorStands.values().toArray(new ArmorStand[0]);
        for (TabPlayer p : nearbyPlayers) as.spawn(p);
    }

    /**
     * Removes specified player from list of nearby players
     *
     * @param   viewer
     *          player to remove
     */
    public void unregisterPlayer(TabPlayer viewer) {
        if (nearbyPlayerList.remove(viewer)) nearbyPlayers = nearbyPlayerList.toArray(new TabPlayer[0]);
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
    public void destroy(TabPlayer viewer) {
        for (ArmorStand a : armorStandArray) a.destroy(viewer);
        unregisterPlayer(viewer);
    }

    @Override
    public void destroy() {
        for (ArmorStand a : armorStandArray) a.destroy();
        nearbyPlayerList.clear();
        nearbyPlayers = new TabPlayer[0];
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
}
