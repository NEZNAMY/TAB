package me.neznamy.tab.platforms.sponge7;

import me.neznamy.tab.shared.platform.EventListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.world.World;

public final class SpongeEventListener extends EventListener<Player> {

    @Listener
    public void onQuit(ClientConnectionEvent.Disconnect event) {
        quit(event.getTargetEntity().getUniqueId());
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
        join(event.getTargetEntity());
    }

    @Listener
    public void onWorldChange(MoveEntityEvent event, @First Player player) {
        World fromWorld = event.getFromTransform().getExtent();
        World toWorld = event.getToTransform().getExtent();
        if (fromWorld.getUniqueId().equals(toWorld.getUniqueId())) return;
        worldChange(event.getTargetEntity().getUniqueId(), event.getTargetEntity().getWorld().getName());
    }

    @Listener
    public void onCommand(SendCommandEvent event, @First Player player) {
        if (command(player.getUniqueId(), event.getCommand())) event.setCancelled(true);
    }

    @Override
    public TabPlayer createPlayer(Player player) {
        return new SpongeTabPlayer(player);
    }
}
