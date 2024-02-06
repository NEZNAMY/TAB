package me.neznamy.tab.platforms.sponge8;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.EventListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.ExecuteCommandEvent;
import org.spongepowered.api.event.entity.ChangeEntityWorldEvent;
import org.spongepowered.api.event.entity.living.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

/**
 * Event listener for Sponge 8 to forward events to enabled features.
 */
public class SpongeEventListener implements EventListener<ServerPlayer> {

    /**
     * Listens to player quit event.
     *
     * @param   event
     *          Disconnect event
     */
    @Listener
    public void onQuit(ServerSideConnectionEvent.Disconnect event) {
        quit(event.player().uniqueId());
    }

    /**
     * Listens to player join event.
     *
     * @param   event
     *          Join event
     */
    @Listener
    public void onJoin(ServerSideConnectionEvent.Join event) {
        join(event.player());
    }

    /**
     * Listens to world change event.
     *
     * @param   event
     *          World change event
     * @param   player
     *          Player who changed world
     */
    @Listener
    public void onWorldChange(ChangeEntityWorldEvent event, @First Player player) {
        worldChange(event.entity().uniqueId(), event.destinationWorld().toString());
    }

    /**
     * Listens to command event to potentially cancel it.
     *
     * @param   event
     *          Command event
     * @param   player
     *          Player who executed the command
     */
    @Listener
    public void onCommand(ExecuteCommandEvent.Pre event, @First Player player) {
        if (command(player.uniqueId(), event.command())) event.setCancelled(true);
    }

    /**
     * Listens to player respawn event.
     *
     * @param   event
     *          Player respawn event
     */
    @Listener(order = Order.PRE)
    public void onRespawn(RespawnPlayerEvent.Recreate event) {
        replacePlayer(event.recreatedPlayer().uniqueId(), event.recreatedPlayer());
    }

    @Override
    @NotNull
    public TabPlayer createPlayer(@NotNull ServerPlayer player) {
        return new SpongeTabPlayer((SpongePlatform) TAB.getInstance().getPlatform(), player);
    }
}
