package me.neznamy.tab.shared.backend;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants.Placeholder;
import me.neznamy.tab.shared.hook.ViaVersionHook;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * TabPlayer extension for backend platforms, which have access to
 * more data and can display it.
 */
public abstract class BackendTabPlayer extends TabPlayer {

    /** Vanish status of the player */
    private boolean vanished;

    /** Last time vanish status was retrieved (in milliseconds) */
    private long lastVanishCheck;

    /**
     * Constructs new instance with given parameters
     *
     * @param   platform
     *          Server platform reference
     * @param   player
     *          platform-specific player object
     * @param   uniqueId
     *          Player's unique ID
     * @param   name
     *          Player's name
     * @param   world
     *          Player's world
     * @param   serverVersion
     *          Server version
     */
    protected BackendTabPlayer(@NotNull BackendPlatform platform, @NotNull Object player, @NotNull UUID uniqueId,
                               @NotNull String name, @NotNull String world, int serverVersion) {
        super(platform, player, uniqueId, name, TAB.getInstance().getConfiguration().getConfig().getServerName(),
                world, ViaVersionHook.getInstance().getPlayerVersion(uniqueId, name, serverVersion), true);
    }

    @Override
    public boolean isVanished() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastVanishCheck >= 900) {
            lastVanishCheck = currentTime;
            vanished = isVanished0();
        }
        return vanished;
    }

    /**
     * Returns player's health for {@link Placeholder#HEALTH} placeholder.
     *
     * @return  player's health
     */
    public abstract double getHealth();

    /**
     * Returns player's display name for {@link Placeholder#DISPLAY_NAME} placeholder.
     *
     * @return  player's display name
     */
    public abstract String getDisplayName();

    /**
     * Calls platform's vanish check and returns the result.
     *
     * @return  {@code true} if player is vanished, {@code false} if not
     */
    public abstract boolean isVanished0();

    /**
     * Returns number of player's deaths for placeholder.
     *
     * @return  number of player's deaths
     */
    public abstract int getDeaths();
}
