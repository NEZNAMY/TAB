package me.neznamy.tab.platforms.sponge;

import me.neznamy.tab.api.TabAPI;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.world.World;

public final class SpongeEventListener {

    @Listener
    public void onQuit(final ClientConnectionEvent.Disconnect event) {
        if (TabAPI.getInstance().isPluginDisabled()) return;
        TabAPI.getInstance().getThreadManager().runTask(() ->
                TabAPI.getInstance().getFeatureManager().onQuit(TabAPI.getInstance().getPlayer(event.getTargetEntity().getUniqueId())));
    }

    @Listener
    public void onJoin(final ClientConnectionEvent.Join event) {
        if (TabAPI.getInstance().isPluginDisabled()) return;
        TabAPI.getInstance().getThreadManager().runTask(() ->
                TabAPI.getInstance().getFeatureManager().onJoin(new SpongeTabPlayer(event.getTargetEntity())));
    }

    @Listener
    public void onWorldChange(final MoveEntityEvent event, @First Player player) {
        if (TabAPI.getInstance().isPluginDisabled()) return;
        final World fromWorld = event.getFromTransform().getExtent();
        final World toWorld = event.getToTransform().getExtent();
        if (fromWorld.getUniqueId().equals(toWorld.getUniqueId())) return;
        TabAPI.getInstance().getThreadManager().runTask(() ->
                TabAPI.getInstance().getFeatureManager().onWorldChange(event.getTargetEntity().getUniqueId(), event.getTargetEntity().getWorld().getName()));
    }

    @Listener
    public void onCommand(final SendCommandEvent event, @First Player player) {
        if (TabAPI.getInstance().isPluginDisabled()) return;
        if (TabAPI.getInstance().getFeatureManager().onCommand(TabAPI.getInstance().getPlayer(player.getUniqueId()), event.getCommand())) {
            event.setCancelled(true);
        }
    }
}
