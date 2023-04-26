package me.neznamy.tab.platforms.sponge8;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.PlatformEventListener;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.ExecuteCommandEvent;
import org.spongepowered.api.event.entity.ChangeEntityWorldEvent;
import org.spongepowered.api.event.entity.living.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

public class SpongeEventListener extends PlatformEventListener {

    @Listener
    public void onQuit(ServerSideConnectionEvent.Disconnect event) {
        quit(event.player().uniqueId());
    }

    @Listener
    public void onJoin(ServerSideConnectionEvent.Join event) {
        join(new SpongeTabPlayer(event.player()));
    }

    @Listener(order = Order.PRE)
    public void onRespawn(RespawnPlayerEvent.Recreate event) {
        if (TAB.getInstance().isPluginDisabled()) return;
        SpongeTabPlayer player = (SpongeTabPlayer) TAB.getInstance().getPlayer(event.recreatedPlayer().uniqueId());
        if (player == null) return;
        player.setPlayer(event.recreatedPlayer());
    }

    @Listener
    public void onWorldChange(ChangeEntityWorldEvent event, @First Player player) {
        worldChange(event.entity().uniqueId(), event.destinationWorld().toString());
    }

    @Listener
    public void onCommand(ExecuteCommandEvent.Pre event, @First Player player) {
        if (command(player.uniqueId(), event.command())) event.setCancelled(true);
    }
}
