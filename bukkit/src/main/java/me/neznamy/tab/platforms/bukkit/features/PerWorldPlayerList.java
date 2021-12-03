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

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;

/**
 * Per-world-PlayerList feature handler
 */
@SuppressWarnings("deprecation")
public class PerWorldPlayerList extends TabFeature implements Listener {
	
	//plugin instance
	private final JavaPlugin plugin;
	
	//config options
	private final boolean allowBypass = TAB.getInstance().getConfiguration().getConfig().getBoolean("per-world-playerlist.allow-bypass-permission", false);
	private final List<String> ignoredWorlds = TAB.getInstance().getConfiguration().getConfig().getStringList("per-world-playerlist.ignore-effect-in-worlds", Arrays.asList("ignoredworld", "build"));
	private final Map<String, List<String>> sharedWorlds = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("per-world-playerlist.shared-playerlist-world-groups");

	/**
	 * Constructs new instance with given parameters and loads config options
	 * @param plugin - plugin instance
	 */
	public PerWorldPlayerList(JavaPlugin plugin) {
		super("Per world PlayerList", null);
		this.plugin = plugin;
		TAB.getInstance().debug(String.format("Loaded PerWorldPlayerList feature with parameters allowBypass=%s, ignoredWorlds=%s, sharedWorlds=%s", allowBypass, ignoredWorlds, sharedWorlds));
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
		TAB.getInstance().getCPUManager().addTime(getFeatureName(), TabConstants.CpuUsageCategory.PLAYER_JOIN, System.nanoTime()-time);
	}
	
	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent e) {
		long time = System.nanoTime();
		checkPlayer(e.getPlayer());
		TAB.getInstance().getCPUManager().addTime(getFeatureName(), TabConstants.CpuUsageCategory.WORLD_SWITCH, System.nanoTime()-time);
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
		if ((allowBypass && viewer.hasPermission(TabConstants.Permission.PER_WORLD_PLAYERLIST_BYPASS)) || ignoredWorlds.contains(viewer.getWorld().getName())) return true;
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