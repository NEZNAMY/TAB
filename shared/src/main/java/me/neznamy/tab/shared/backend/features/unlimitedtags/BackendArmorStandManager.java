package me.neznamy.tab.shared.backend.features.unlimitedtags;

import lombok.Getter;
import me.neznamy.tab.api.*;
import me.neznamy.tab.shared.backend.protocol.PacketPlayOutEntityDestroy;
import me.neznamy.tab.shared.backend.protocol.PacketPlayOutEntityMetadata;
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
    private final List<TabPlayer> nearbyPlayerList = new ArrayList<>();

    /** Nearby players in an array for speed while iterating */
    @Getter private TabPlayer[] nearbyPlayers = new TabPlayer[0];

    /**
     * Packets to destroy armor stands. On 1.17 it's a list of individual armor stands
     * due to packet only containing one entity, on all other server versions
     * it's a single packet with all armor stand ids.
     */
    private final PacketPlayOutEntityDestroy[] destroyPackets;

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

        List<PacketPlayOutEntityDestroy> destroyPackets = new ArrayList<>();
        if (TabAPI.getInstance().getServerVersion() == ProtocolVersion.V1_17) { // Mojank
            armorStands.values().forEach(as -> destroyPackets.add(new PacketPlayOutEntityDestroy(as.getEntityId())));
        } else {
            destroyPackets.add(new PacketPlayOutEntityDestroy(armorStands.values().stream().mapToInt(BackendArmorStand::getEntityId).toArray()));
        }
        this.destroyPackets = destroyPackets.toArray(new PacketPlayOutEntityDestroy[0]);
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
        if (this.sneaking == sneaking) return;
        this.sneaking = sneaking;
        for (TabPlayer viewer : nearbyPlayers) {
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
        for (TabPlayer viewer : nearbyPlayers) {
            respawn(viewer);
        }
    }

    public void respawn(TabPlayer viewer) {
        // 1.8.0 will not see entity that respawned in the same tick
        // creating new delayed task every time someone sneaks can be abused and cause OOM
        // RIP 1.8.0
        for (PacketPlayOutEntityDestroy packet : destroyPackets) {
            viewer.sendCustomPacket(packet, TabConstants.PacketCategory.UNLIMITED_NAMETAGS_DESPAWN);
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
    public void addArmorStand(String name, BackendArmorStand as) {
        armorStands.put(name, as);
        armorStandArray = armorStands.values().toArray(new BackendArmorStand[0]);
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
        for (PacketPlayOutEntityDestroy packet : destroyPackets) {
            viewer.sendCustomPacket(packet, TabConstants.PacketCategory.UNLIMITED_NAMETAGS_DESPAWN);
        }
        unregisterPlayer(viewer);
    }

    @Override
    public void destroy() {
        for (TabPlayer viewer : nearbyPlayers) {
            for (PacketPlayOutEntityDestroy packet : destroyPackets) {
                viewer.sendCustomPacket(packet, TabConstants.PacketCategory.UNLIMITED_NAMETAGS_DESPAWN);
            }
        }
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

    public void updateMetadata(TabPlayer viewer) {
        for (BackendArmorStand a : armorStandArray) {
            viewer.sendCustomPacket(new PacketPlayOutEntityMetadata(a.getEntityId(), a.createDataWatcher(a.getProperty().getFormat(viewer), viewer)), TabConstants.PacketCategory.UNLIMITED_NAMETAGS_METADATA);
        }
    }
}
