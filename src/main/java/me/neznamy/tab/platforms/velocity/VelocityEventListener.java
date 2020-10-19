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

	@Subscribe
	public void onQuit(DisconnectEvent e){
		if (Shared.disabled) return;
		TabPlayer disconnectedPlayer = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (disconnectedPlayer == null) return; //player connected to bungeecord successfully, but not to the bukkit server anymore ? idk the check is needed
		Shared.data.remove(e.getPlayer().getUniqueId());
		Shared.featureManager.onQuit(disconnectedPlayer);
	}
	
	@Subscribe
	public void onConnect(ServerPostConnectEvent e){
		try{
			if (Shared.disabled) return;
			if (!Shared.data.containsKey(e.getPlayer().getUniqueId())) {
				//join
				TabPlayer p = new VelocityTabPlayer(e.getPlayer(), e.getPlayer().getCurrentServer().get().getServerInfo().getName());
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