package me.neznamy.tab.platforms.sponge7;

import me.neznamy.tab.shared.platform.EventListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class SpongeEventListener extends EventListener<Player> {

    @Listener
    public void onQuit(ClientConnectionEvent.Disconnect event) {
        quit(event.getTargetEntity().getUniqueId());
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
        join(event.getTargetEntity());
    }

    @Listener
    public void onWorldChange(MoveEntityEvent.Teleport event, @First Player player) {
        worldChange(event.getTargetEntity().getUniqueId(), event.getTargetEntity().getWorld().getName());
    }

    @Listener
    public void onCommand(SendCommandEvent event, @First Player player) {
        if (command(player.getUniqueId(), event.getCommand())) event.setCancelled(true);
    }

    @Listener(order = Order.PRE)
    public void onRespawn(RespawnPlayerEvent event) {
        replacePlayer(event.getTargetEntity().getUniqueId(), event.getTargetEntity());
    }

    @Override
    public TabPlayer createPlayer(Player player) {
        return new SpongeTabPlayer(player);
    }
}
