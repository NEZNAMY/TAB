package me.neznamy.tab.platforms.sponge7;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.EventListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;

/**
 * Event listener for Sponge 7 to forward events to enabled features.
 */
public class SpongeEventListener implements EventListener<Player> {

    /**
     * Listens to player quit event.
     *
     * @param   event
     *          Disconnect event
     */
    @Listener
    public void onQuit(ClientConnectionEvent.Disconnect event) {
        quit(event.getTargetEntity().getUniqueId());
    }

    /**
     * Listens to player join event.
     *
     * @param   event
     *          Join event
     */
    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
        join(event.getTargetEntity());
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
    public void onWorldChange(MoveEntityEvent.Teleport event, @First Player player) {
        worldChange(event.getTargetEntity().getUniqueId(), event.getTargetEntity().getWorld().getName());
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
    public void onCommand(SendCommandEvent event, @First Player player) {
        if (command(player.getUniqueId(), event.getCommand())) event.setCancelled(true);
    }

    /**
     * Listens to player respawn event.
     *
     * @param   event
     *          Player respawn event
     */
    @Listener(order = Order.PRE)
    public void onRespawn(RespawnPlayerEvent event) {
        replacePlayer(event.getTargetEntity().getUniqueId(), event.getTargetEntity());
    }

    @Override
    @NotNull
    public TabPlayer createPlayer(@NotNull Player player) {
        return new SpongeTabPlayer((SpongePlatform) TAB.getInstance().getPlatform(), player);
    }
}
