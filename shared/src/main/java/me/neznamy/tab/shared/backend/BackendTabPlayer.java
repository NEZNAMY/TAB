package me.neznamy.tab.shared.backend;

import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class BackendTabPlayer extends TabPlayer {

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
    protected BackendTabPlayer(@NotNull Object player, @NotNull UUID uniqueId, @NotNull String name,
                               @NotNull String server, @NotNull String world, int protocolVersion) {
        super(player, uniqueId, name, server, world, protocolVersion, true);
    }

    public abstract void spawnEntity(int entityId, @NotNull UUID id, @NotNull Object entityType, @NotNull Location location, @NotNull EntityData data);

    public abstract void updateEntityMetadata(int entityId, @NotNull EntityData data);

    public abstract void teleportEntity(int entityId, @NotNull Location location);

    public abstract void destroyEntities(int... entities);
}
