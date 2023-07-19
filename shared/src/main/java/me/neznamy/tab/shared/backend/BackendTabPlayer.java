package me.neznamy.tab.shared.backend;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.hook.ViaVersionHook;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * TabPlayer extension for backend platforms, which have access to
 * more data and can display it.
 */
public abstract class BackendTabPlayer extends TabPlayer {

    /**
     * Constructs new instance with given parameters
     *
     * @param   player
     *          platform-specific player object
     * @param   uniqueId
     *          Player's unique ID
     * @param   name
     *          Player's name
     * @param   world
     *          Player's world
     */
    protected BackendTabPlayer(@NotNull Object player, @NotNull UUID uniqueId, @NotNull String name, @NotNull String world) {
        super(
                player,
                uniqueId,
                name,
                TAB.getInstance().getConfiguration().getServerName(),
                world,
                ViaVersionHook.getInstance().getPlayerVersion(uniqueId, name),
                true
        );
    }

    /**
     * Returns player's health for {@link me.neznamy.tab.shared.TabConstants.Placeholder#HEALTH} placeholder.
     *
     * @return  player's health
     */
    public abstract double getHealth();

    /**
     * Returns player's display name for {@link me.neznamy.tab.shared.TabConstants.Placeholder#DISPLAY_NAME} placeholder.
     *
     * @return  player's display name
     */
    public abstract String getDisplayName();

    public abstract void spawnEntity(int entityId, @NotNull UUID id, @NotNull Object entityType, @NotNull Location location, @NotNull EntityData data);

    public abstract void updateEntityMetadata(int entityId, @NotNull EntityData data);

    public abstract void teleportEntity(int entityId, @NotNull Location location);

    public abstract void destroyEntities(int... entities);
}
