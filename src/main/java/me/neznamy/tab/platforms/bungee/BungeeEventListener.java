package me.neznamy.tab.platforms.bungee;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

/**
 * The core for bungeecord forwarding events into all enabled features
 */
public class BungeeEventListener implements Listener {

	@EventHandler
	public void onTabComplete(TabCompleteEvent e) {
		if (Shared.disabled) return;
		if (e.getCursor().startsWith("/btab ")) {
			String arg = e.getCursor();
			while (arg.contains("  ")) arg = arg.replace("  ", " ");
			String[] args = arg.split(" ");
			args = Arrays.copyOfRange(args, 1, args.length);
			if (arg.endsWith(" ")) {
				List<String> list = Lists.newArrayList(args);
				list.add("");
				args = list.toArray(new String[0]);
			}
			e.getSuggestions().clear();
			e.getSuggestions().addAll(Shared.command.complete(Shared.getPlayer(((ProxiedPlayer)e.getSender()).getUniqueId()), args));
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onQuit(PlayerDisconnectEvent e){
		if (Shared.disabled) return;
		TabPlayer disconnectedPlayer = Shared.getPlayer(e.getPlayer().getUniqueId());
		if (disconnectedPlayer == null) return; //player connected to bungeecord successfully, but not to the bukkit server anymore ? idk the check is needed
		Shared.data.remove(e.getPlayer().getUniqueId());
		Shared.featureManager.onQuit(disconnectedPlayer);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onSwitch(ServerSwitchEvent e){
		try{
			if (Shared.disabled) return;
			if (!Shared.data.containsKey(e.getPlayer().getUniqueId())) {
				ITabPlayer p = new BungeeTabPlayer(e.getPlayer());
				Shared.data.put(e.getPlayer().getUniqueId(), p);
				Main.inject(p.getUniqueId());
				Shared.featureManager.onJoin(p);
			} else {
				TabPlayer p = Shared.getPlayer(e.getPlayer().getUniqueId());
				long time = System.nanoTime();
				String from = p.getWorldName();
				String to = e.getPlayer().getServer().getInfo().getName();
				p.setWorldName(to);
				Shared.cpu.addTime(TabFeature.OTHER, UsageType.WORLD_SWITCH_EVENT, System.nanoTime()-time);
				Shared.featureManager.onWorldChange(p, from, to);
			}
		} catch (Throwable ex){
			Shared.errorManager.criticalError("An error occurred when player joined/changed server", ex);
		}
	}

	@EventHandler
	public void onChat(ChatEvent e) {
		TabPlayer sender = Shared.getPlayer(((ProxiedPlayer)e.getSender()).getUniqueId());
		if (sender == null) return;
		if (e.getMessage().equalsIgnoreCase("/btab")) {
			Shared.sendPluginInfo(sender);
			return;
		}
		if (Shared.featureManager.onCommand(sender, e.getMessage())) e.setCancelled(true);
	}
}