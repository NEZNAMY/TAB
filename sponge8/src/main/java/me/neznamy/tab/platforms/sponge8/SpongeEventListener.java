package me.neznamy.tab.platforms.sponge8;

import me.neznamy.tab.shared.platform.EventListener;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.ExecuteCommandEvent;
import org.spongepowered.api.event.entity.ChangeEntityWorldEvent;
import org.spongepowered.api.event.entity.living.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

public class SpongeEventListener extends EventListener<ServerPlayer> {

    @Listener
    public void onQuit(ServerSideConnectionEvent.Disconnect event) {
        quit(event.player().uniqueId());
    }

    @Listener
    public void onJoin(ServerSideConnectionEvent.Join event) {
        join(event.player());
    }

    @Listener
    public void onWorldChange(ChangeEntityWorldEvent event, @First Player player) {
        worldChange(event.entity().uniqueId(), event.destinationWorld().toString());
    }

    @Listener
    public void onCommand(ExecuteCommandEvent.Pre event, @First Player player) {
        if (command(player.uniqueId(), event.command())) event.setCancelled(true);
    }

    @Listener(order = Order.PRE)
    public void onRespawn(RespawnPlayerEvent.Recreate event) {
        replacePlayer(event.recreatedPlayer().uniqueId(), event.recreatedPlayer());
    }

    @Override
    public TabPlayer createPlayer(ServerPlayer player) {
        return new SpongeTabPlayer(player);
    }
}
