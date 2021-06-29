package me.neznamy.tab.platforms.bukkit.features;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.types.Loadable;

/**
 * Per-world-playerlist feature handler
 */
@SuppressWarnings("deprecation")
public class PerWorldPlayerlist implements Loadable, Listener {
	
	//plugin instance
	private JavaPlugin plugin;
	
	//config options
	private boolean allowBypass;
	private List<String> ignoredWorlds;
	private Map<String, List<String>> sharedWorlds;

	/**
	 * Constructs new instance with given parameters and loads config options
	 * @param plugin - plugin instance
	 * @param tab - tab instance
	 */
	public PerWorldPlayerlist(JavaPlugin plugin, TAB tab) {
		this.plugin = plugin;
		allowBypass = tab.getConfiguration().getConfig().getBoolean("per-world-playerlist.allow-bypass-permission", false);
		ignoredWorlds = tab.getConfiguration().getConfig().getStringList("per-world-playerlist.ignore-effect-in-worlds", Arrays.asList("ignoredworld", "build"));
		sharedWorlds = tab.getConfiguration().getConfig().getConfigurationSection("per-world-playerlist.shared-playerlist-world-groups");
		for (Entry<String, List<String>> group : sharedWorlds.entrySet()) {
			if (group.getValue() == null) {
				tab.getErrorManager().startupWarn("World group \"" + group + "\" in per-world-playerlist does not contain any worlds. You can just remove the group.");
			} else if (group.getValue().size() == 1) {
				tab.getErrorManager().startupWarn("World group \"" + group + "\" in per-world-playerlist only contain a single world (\"" + group.getValue().get(0) +
						"\"), which has no effect and only makes config less readable. Delete the group entirely for a cleaner config.");
			}
		}
		tab.debug(String.format("Loaded PerWorldPlayerlist feature with parameters allowBypass=%s, ignoredWorlds=%s, sharedWorlds=%s", allowBypass, ignoredWorlds, sharedWorlds));
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@Override
	public void load(){
		Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getOnlinePlayers().forEach(this::checkPlayer));
	}
	
	@Override
	public void unload(){
		for (Player p : Bukkit.getOnlinePlayers()) {
			for (Player pl : Bukkit.getOnlinePlayers()) {
				p.showPlayer(pl);
			}
		}
		HandlerList.unregisterAll(this);
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		long time = System.nanoTime();
		checkPlayer(e.getPlayer());
		TAB.getInstance().getCPUManager().addTime(TabFeature.PER_WORLD_PLAYERLIST, UsageType.PLAYER_JOIN_EVENT, System.nanoTime()-time);
	}
	
	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent e) {
		long time = System.nanoTime();
		checkPlayer(e.getPlayer());
		TAB.getInstance().getCPUManager().addTime(TabFeature.PER_WORLD_PLAYERLIST, UsageType.WORLD_SWITCH_EVENT, System.nanoTime()-time);
	}
	
	private void checkPlayer(Player p) {
		for (Player all : Bukkit.getOnlinePlayers()){
			if (all == p) continue;
			if (!shouldSee(p, all) && p.canSee(all)) p.hidePlayer(all);
			if (shouldSee(p, all) && !p.canSee(all)) p.showPlayer(all);
			if (!shouldSee(all, p) && all.canSee(p)) all.hidePlayer(p);
			if (shouldSee(all, p) && !all.canSee(p)) all.showPlayer(p);
		}
	}
	
	private boolean shouldSee(Player viewer, Player target) {
		if (target == viewer) return true;
		if ((allowBypass && viewer.hasPermission("tab.bypass")) || ignoredWorlds.contains(viewer.getWorld().getName())) return true;
		String viewerWorldGroup = viewer.getWorld().getName() + "-default"; //preventing unwanted behavior when some group is called exactly like a world
		String targetWorldGroup = target.getWorld().getName() + "-default";
		for (Entry<String, List<String>> group : sharedWorlds.entrySet()) {
			if (group.getValue() != null) {
				if (group.getValue().contains(viewer.getWorld().getName())) viewerWorldGroup = group.getKey();
				if (group.getValue().contains(target.getWorld().getName())) targetWorldGroup = group.getKey();
			}
		}
		return viewerWorldGroup.equals(targetWorldGroup);
	}
}