package me.neznamy.tab.platforms.sponge;

import me.neznamy.tab.api.TabAPI;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.ExecuteCommandEvent;
import org.spongepowered.api.event.entity.ChangeEntityWorldEvent;
import org.spongepowered.api.event.entity.living.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

public final class SpongeEventListener {

    @Listener
    public void onQuit(final ServerSideConnectionEvent.Disconnect event) {
        if (TabAPI.getInstance().isPluginDisabled()) return;
        TabAPI.getInstance().getThreadManager().runTask(() ->
                TabAPI.getInstance().getFeatureManager().onQuit(TabAPI.getInstance().getPlayer(event.player().uniqueId())));
    }

    @Listener
    public void onJoin(final ServerSideConnectionEvent.Join event) {
        if (TabAPI.getInstance().isPluginDisabled()) return;
        TabAPI.getInstance().getThreadManager().runTask(() ->
                TabAPI.getInstance().getFeatureManager().onJoin(new SpongeTabPlayer(event.player())));
    }

    @Listener(order = Order.PRE)
    public void onRespawn(final RespawnPlayerEvent.Recreate event) {
        if (TabAPI.getInstance().isPluginDisabled()) return;
        final SpongeTabPlayer player = (SpongeTabPlayer) TabAPI.getInstance().getPlayer(event.recreatedPlayer().uniqueId());
        player.setPlayer(event.recreatedPlayer());
    }

    @Listener
    public void onWorldChange(final ChangeEntityWorldEvent event, @First Player player) {
        if (TabAPI.getInstance().isPluginDisabled()) return;
        TabAPI.getInstance().getThreadManager().runTask(() ->
                TabAPI.getInstance().getFeatureManager().onWorldChange(event.entity().uniqueId(), event.destinationWorld().toString()));
    }

    @Listener
    public void onCommand(final ExecuteCommandEvent.Pre event, @First Player player) {
        if (TabAPI.getInstance().isPluginDisabled()) return;
        if (TabAPI.getInstance().getFeatureManager().onCommand(TabAPI.getInstance().getPlayer(player.uniqueId()), event.command())) {
            event.setCancelled(true);
        }
    }
}
