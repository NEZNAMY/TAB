package me.neznamy.tab.shared.backend;

import me.neznamy.tab.shared.ITabPlayer;

import java.util.UUID;

public abstract class BackendTabPlayer extends ITabPlayer {

    /**
     * Constructs new instance with given parameters
     *
     * @param player          platform-specific player object
     * @param uniqueId        Player's unique ID
     * @param name            Player's name
     * @param server          Player's server
     * @param world           Player's world
     * @param protocolVersion Player's game version
     */
    protected BackendTabPlayer(Object player, UUID uniqueId, String name, String server, String world, int protocolVersion) {
        super(player, uniqueId, name, server, world, protocolVersion, true);
    }

    public abstract void spawnEntity(int entityId, UUID id, Object entityType, Location location, EntityData data);

    public abstract void updateEntityMetadata(int entityId, EntityData data);

    public abstract void teleportEntity(int entityId, Location location);

    public abstract void destroyEntities(int... entities);
}
