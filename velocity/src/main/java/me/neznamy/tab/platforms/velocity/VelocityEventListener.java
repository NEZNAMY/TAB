package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.command.CommandExecuteEvent.CommandResult;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabConstants;

/**
 * The core for velocity forwarding events into all enabled features
 */
public class VelocityEventListener {

    /**
     * Disconnect event listener to forward the event to all features
     *
     * @param   e
     *          disconnect event
     */
    @Subscribe
    public void onQuit(DisconnectEvent e){
        if (TabAPI.getInstance().isPluginDisabled()) return;
        TabAPI.getInstance().getThreadManager().runTask(() ->
                TabAPI.getInstance().getFeatureManager().onQuit(TabAPI.getInstance().getPlayer(e.getPlayer().getUniqueId())));
    }
    
    /**
     * Listener to join / server switch to forward the event to all features
     *
     * @param   e
     *          connect event
     */
    @Subscribe
    public void onConnect(ServerPostConnectEvent e){
        if (TabAPI.getInstance().isPluginDisabled()) return;
        Player p = e.getPlayer();
        TabAPI.getInstance().getThreadManager().runTask(() -> {
            if (TabAPI.getInstance().getPlayer(p.getUniqueId()) == null) {
                TabAPI.getInstance().getFeatureManager().onJoin(new VelocityTabPlayer(p));
            } else {
                String server = p.getCurrentServer().isPresent() ? p.getCurrentServer().get().getServerInfo().getName() : "null";
                TabAPI.getInstance().getFeatureManager().onServerChange(p.getUniqueId(), server);
            }
        });
    }

    /**
     * Listener to commands to forward the event to all features
     *
     * @param   e
     *          command event
     */
    @Subscribe
    public void onCommand(CommandExecuteEvent e) {
        if (TabAPI.getInstance().isPluginDisabled()) return;
        if (e.getCommandSource() instanceof Player && TabAPI.getInstance().getFeatureManager().onCommand(
                TabAPI.getInstance().getPlayer(((Player)e.getCommandSource()).getUniqueId()), e.getCommand()))
            e.setResult(CommandResult.denied());
    }

    /**
     * Listener to plugin message event
     *
     * @param   event
     *          plugin message event
     */
    @Subscribe
    public void onPluginMessageEvent(PluginMessageEvent event){
        if (!event.getIdentifier().getId().equalsIgnoreCase(TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME)) return;
        if (event.getTarget() instanceof Player) {
            event.setResult(PluginMessageEvent.ForwardResult.handled());
            Main.getInstance().getPlatform().getPluginMessageHandler().onPluginMessage(
                    ((Player) event.getTarget()).getUniqueId(), event.getData());
        }
    }
}