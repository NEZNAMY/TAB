package me.neznamy.tab.api.integration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public abstract class VanishIntegration {

    /** List of all registered VanishIntegration handlers */
    private static final List<VanishIntegration> HANDLERS = new ArrayList<>();

    /** Name of the plugin this integration is associated with */
    @NotNull
    private final String plugin;

    /**
     * Determines if a viewer can see a target player.
     *
     * @param   viewer
     *          The player attempting to view another player
     * @param   target
     *          The player being viewed
     * @return  {@code true} if the viewer can see the target, {@code false} otherwise
     */
    public abstract boolean canSee(@NotNull TabPlayer viewer, @NotNull TabPlayer target);

    /**
     * Checks if the specified player is in a vanished state.
     *
     * @param   player
     *          The player to check
     * @return  {@code true} if the player is vanished, {@code false} otherwise
     */
    public abstract boolean isVanished(@NotNull TabPlayer player);

    /**
     * Registers this integration handler to the global list of handlers.
     */
    public void register() {
        registerHandler(this);
    }

    /**
     * Unregisters this integration handler from the global list of handlers.
     */
    public void unregister() {
        unregisterHandler(this);
    }

    /**
     * Adds a VanishIntegration handler to the global list of handlers.
     *
     * @param handler
     *        The handler to register
     */
    public static void registerHandler(@NotNull VanishIntegration handler) {
        HANDLERS.add(handler);
    }

    /**
     * Removes a VanishIntegration handler from the global list of handlers.
     *
     * @param handler
     *        The handler to unregister
     */
    public static void unregisterHandler(@NotNull VanishIntegration handler) {
        HANDLERS.remove(handler);
    }

    /**
     * Retrieves the list of all registered VanishIntegration handlers.
     *
     * @return  A list of registered VanishIntegration handlers
     */
    @NotNull
    public static List<VanishIntegration> getHandlers() {
        return HANDLERS;
    }
}
