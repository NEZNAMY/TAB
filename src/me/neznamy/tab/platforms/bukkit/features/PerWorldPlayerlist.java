package me.neznamy.tab.platforms.bukkit.features;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.neznamy.tab.platforms.bukkit.Main;
import me.neznamy.tab.platforms.bukkit.TabPlayer;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.features.SimpleFeature;

@SuppressWarnings("deprecation")
public class PerWorldPlayerlist implements SimpleFeature{

	private boolean allowBypass;
	private List<String> ignoredWorlds;
	
	@Override
	public void load(){
		allowBypass = Configs.advancedconfig.getBoolean("allow-pwp-bypass-permission", false);
		ignoredWorlds = Configs.advancedconfig.getStringList("ignore-pwp-in-worlds", Arrays.asList("ignoredworld", "spawn"));
		for (Player p : Main.getOnlinePlayers()){
			hidePlayer(p);
			showInSameWorld(p);
		}
	}
	@Override
	public void unload(){
		for (Player p : Main.getOnlinePlayers()) for (Player pl : Main.getOnlinePlayers()) p.showPlayer(pl);
	}
	@Override
	public void onJoin(ITabPlayer p) {
		hidePlayer(((TabPlayer)p).player);
		showInSameWorld(((TabPlayer)p).player);
	}
	@Override
	public void onQuit(ITabPlayer p) {
	}
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		hidePlayer(((TabPlayer)p).player);
		showInSameWorld(((TabPlayer)p).player);
	}
	private void showInSameWorld(Player p){
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.instance, new Runnable() {

			@Override
			public void run() {
				for (Player pl : Main.getOnlinePlayers()){
					if (pl == p) continue;
					if (p.getWorld() == pl.getWorld()) {
						p.showPlayer(pl);
						pl.showPlayer(p);
					}
					if ((allowBypass && p.hasPermission("tab.bypass")) || ignoredWorlds.contains(p.getWorld().getName())) {
						p.showPlayer(pl);
					}
					if ((allowBypass && pl.hasPermission("tab.bypass")) || ignoredWorlds.contains(pl.getWorld().getName())) {
						pl.showPlayer(p);
					}
				}
			}
		});
	}
	public void hidePlayer(Player p){
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.instance, new Runnable() {

			@Override
			public void run() {
				for (Player pl : Main.getOnlinePlayers()){
					if (pl == p) continue;
					p.hidePlayer(pl);
					pl.hidePlayer(p);
				}
			}
		});
	}
}