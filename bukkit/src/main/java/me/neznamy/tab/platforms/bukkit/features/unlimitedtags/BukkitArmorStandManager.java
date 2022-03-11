package me.neznamy.tab.platforms.bukkit.features.unlimitedtags;

import me.neznamy.tab.api.ArmorStand;
import me.neznamy.tab.api.ArmorStandManager;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.nametags.unlimited.NameTagX;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A helper class for easy management of armor stands of a player
 */
public class BukkitArmorStandManager implements ArmorStandManager {

    private final NameTagX nameTagX;
    //map of registered armor stands
    private final Map<String, ArmorStand> armorStands = new LinkedHashMap<>();

    //players in entity tracking range
    private final List<TabPlayer> nearbyPlayers = new ArrayList<>();

    //array to iterate over to avoid concurrent modification and slightly boost performance & memory
    private ArmorStand[] armorStandArray = new ArmorStand[0];

    private TabPlayer[] nearbyPlayerArray = new TabPlayer[0];

    public BukkitArmorStandManager(NameTagX nameTagX, TabPlayer owner) {
        this.nameTagX = nameTagX;
        owner.setProperty(nameTagX, TabConstants.Property.NAMETAG, owner.getProperty(TabConstants.Property.TAGPREFIX).getCurrentRawValue()
                + owner.getProperty(TabConstants.Property.CUSTOMTAGNAME).getCurrentRawValue()
                + owner.getProperty(TabConstants.Property.TAGSUFFIX).getCurrentRawValue());
        double height = 0;
        for (String line : nameTagX.getDynamicLines()) {
            addArmorStand(line, new BukkitArmorStand(this, owner, line, height, false));
            height += nameTagX.getSpaceBetweenLines();
        }
        for (Map.Entry<String, Object> line : nameTagX.getStaticLines().entrySet()) {
            addArmorStand(line.getKey(), new BukkitArmorStand(this, owner, line.getKey(), Double.parseDouble(line.getValue().toString()), true));
        }
        fixArmorStandHeights();
    }

    /**
     * Teleports armor stands to player's current location for specified viewer
     * @param viewer - player to teleport armor stands for
     */
    public void teleport(TabPlayer viewer) {
        for (ArmorStand a : armorStandArray) a.teleport(viewer);
    }

    /**
     * Returns array of nearby players
     * @return array of nearby players
     */
    public TabPlayer[] getNearbyPlayers(){
        return nearbyPlayerArray;
    }

    /**
     * Returns {@code true} if requested player is nearby, {@code false} if not
     * @param    viewer
     *             Player to check for
     * @return    {@code true} if player nearby, {@code false} if not
     */
    public boolean isNearby(TabPlayer viewer) {
        return nearbyPlayers.contains(viewer);
    }

    /**
     * Returns true if manager contains armor stand with specified entity id, false if not
     * @param entityId - entity id
     * @return true if armor stand with specified entity id exists, false if not
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
     * @param sneaking - new sneaking status
     */
    public void sneak(boolean sneaking) {
        for (ArmorStand a : armorStandArray) a.sneak(sneaking);
    }

    /**
     * Teleports armor stands to player's current location for all nearby players
     */
    public void teleport() {
        for (ArmorStand a : armorStandArray) a.teleport();
    }

    /**
     * Performs respawn operation on all armor stands to skip teleport animation
     */
    public void respawn() {
        for (ArmorStand a : armorStandArray) {
            for (TabPlayer viewer : nearbyPlayerArray) {
                a.respawn(viewer);
            }
        }
    }

    /**
     * Spawns all armor stands for specified viewer and adds them into nearby players
     * @param viewer - player to spawn armor stands for
     */
    public void spawn(TabPlayer viewer) {
        nearbyPlayers.add(viewer);
        nearbyPlayerArray = nearbyPlayers.toArray(new TabPlayer[0]);
        if (viewer.getVersion().getMinorVersion() < 8) return;
        for (ArmorStand a : armorStandArray) a.spawn(viewer);
    }

    /**
     * Sends destroy packet of all armor stands to specified player and removes them from nearby players list
     * @param viewer - player to destroy armor stands for
     */
    public void destroy(TabPlayer viewer) {
        for (ArmorStand a : armorStandArray) a.destroy(viewer);
        unregisterPlayer(viewer);
    }

    /**
     * Fixes heights of all armor stands due to dynamic lines
     */
    public void fixArmorStandHeights() {
        double currentY = -nameTagX.getSpaceBetweenLines();
        for (ArmorStand as : armorStandArray) {
            if (as.hasStaticOffset()) continue;
            if (as.getProperty().get().length() != 0) {
                currentY += nameTagX.getSpaceBetweenLines();
                as.setOffset(currentY);
            }
        }
    }

    /**
     * Adds armor stand to list and registers it to all nearby players
     *
     * @param    name
     *             Unique identifier of the text line
     * @param    as
     *             Armor stand to add
     */
    public void addArmorStand(String name, ArmorStand as) {
        armorStands.put(name, as);
        armorStandArray = armorStands.values().toArray(new ArmorStand[0]);
        for (TabPlayer p : nearbyPlayerArray) as.spawn(p);
    }

    /**
     * Gets armor stand by name
     *
     * @param    name
     *             Name to get armor stand by
     * @return    Armor stand with given name or null if not found
     */
    public ArmorStand getArmorStand(String name) {
        return armorStands.get(name);
    }

    /**
     * Removes armor stand by given name if exists
     *
     * @param    name
     *             Name of line to remove
     */
    public void removeArmorStand(String name) {
        if (!armorStands.containsKey(name)) return;
        armorStands.get(name).destroy();
        armorStands.remove(name);
        armorStandArray = armorStands.values().toArray(new ArmorStand[0]);
    }

    /**
     * Removes specified player from list of nearby players
     * @param viewer - player to remove
     */
    public void unregisterPlayer(TabPlayer viewer) {
        if (nearbyPlayers.remove(viewer)) nearbyPlayerArray = nearbyPlayers.toArray(new TabPlayer[0]);
    }

    public void updateVisibility(boolean force) {
        for (ArmorStand a : armorStandArray) a.updateVisibility(force);
    }

    @Override
    public void destroy() {
        for (ArmorStand a : armorStandArray) a.destroy();
        nearbyPlayers.clear();
        nearbyPlayerArray = new TabPlayer[0];
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