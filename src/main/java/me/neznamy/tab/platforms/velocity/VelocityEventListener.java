package me.neznamy.tab.platforms.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;

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
		if (Shared.disabled) return;
		TabPlayer disconnectedPlayer = Shared.getPlayer(e.getPlayer().getUniqueId());
		Shared.data.remove(e.getPlayer().getUniqueId());
		Shared.featureManager.onQuit(disconnectedPlayer);
	}
	
	/**
	 * Listener to join / server switch to forward the event to all features
	 * @param e
	 */
	@Subscribe
	public void onConnect(ServerPostConnectEvent e){
		try{
			if (Shared.disabled) return;
			if (!Shared.data.containsKey(e.getPlayer().getUniqueId())) {
				//join
				TabPlayer p = new VelocityTabPlayer(e.getPlayer());
				Shared.data.put(e.getPlayer().getUniqueId(), p);
				Main.inject(p.getUniqueId());
				Shared.featureManager.onJoin(p);
			} else {
				//server change
				TabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
				long time = System.nanoTime();
				String from = p.getWorldName();
				String to = e.getPlayer().getCurrentServer().get().getServerInfo().getName();
				p.setWorldName(to);
				Shared.cpu.addTime(TabFeature.OTHER, UsageType.WORLD_SWITCH_EVENT, System.nanoTime()-time);
				Shared.featureManager.onWorldChange(p, from, to);
			}
		} catch (Throwable ex){
			Shared.errorManager.criticalError("An error occurred when player joined/changed server", ex);
		}
	}
}