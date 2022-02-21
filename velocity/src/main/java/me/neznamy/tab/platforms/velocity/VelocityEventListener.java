package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.command.CommandExecuteEvent.CommandResult;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.proxy.ProxyPlatform;

/**
 * The core for velocity forwarding events into all enabled features
 */
public class VelocityEventListener {

    /**
     * Disconnect event listener to forward the event to all features
     * @param e - disconnect event
     */
    @Subscribe
    public void onQuit(DisconnectEvent e){
        if (TAB.getInstance().isDisabled()) return;
        TAB.getInstance().getCPUManager().runTask(() ->
                TAB.getInstance().getFeatureManager().onQuit(TAB.getInstance().getPlayer(e.getPlayer().getUniqueId())));
    }
    
    /**
     * Listener to join / server switch to forward the event to all features
     * @param    e
     *             connect event
     */
    @Subscribe
    public void onConnect(ServerPostConnectEvent e){
        Player p = e.getPlayer();
        if (TAB.getInstance().isDisabled()) return;
        if (TAB.getInstance().getPlayer(p.getUniqueId()) == null) {
            TAB.getInstance().getCPUManager().runTask(() -> TAB.getInstance().getFeatureManager().onJoin(new VelocityTabPlayer(p)));
        } else {
            TAB.getInstance().getCPUManager().runTaskLater(300, () ->
                TAB.getInstance().getFeatureManager().onServerChange(p.getUniqueId(), p.getCurrentServer().isPresent() ? p.getCurrentServer().get().getServerInfo().getName() : "null")
            );
        }
    }

    /**
     * Listener to commands to forward the event to all features
     * @param    e
     *             command event
     */
    @Subscribe
    public void onCommand(CommandExecuteEvent e) {
        if (TAB.getInstance().isDisabled()) return;
        if (e.getCommandSource() instanceof Player && TAB.getInstance().getFeatureManager().onCommand(TAB.getInstance().getPlayer(((Player)e.getCommandSource()).getUniqueId()), e.getCommand())) e.setResult(CommandResult.denied());
    }

    /**
     * Listener to plugin message event
     * @param event - plugin message event
     */
    @Subscribe
    public void onPluginMessageEvent(PluginMessageEvent event){
        if (!event.getIdentifier().getId().equalsIgnoreCase(TabConstants.PLUGIN_MESSAGE_CHANNEL_NAME)) return;
        if (event.getTarget() instanceof Player) {
            event.setResult(PluginMessageEvent.ForwardResult.handled());
            ((ProxyPlatform)TAB.getInstance().getPlatform()).getPluginMessageHandler().onPluginMessage(
                    ((Player) event.getTarget()).getUniqueId(), event.getData());
        }
    }
}