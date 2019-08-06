package me.neznamy.tab.bukkit;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@SuppressWarnings("deprecation")
public class PerWorldPlayerlist {

	public static boolean enabled;
	public static boolean allowBypass;
	public static List<Object> ignoredWorlds;
	
	public static void load(){
		if (!enabled) return;
		for (Player p : Bukkit.getOnlinePlayers()){
			hidePlayer(p);
			showInSameWorld(p);
		}
	}
	public static void unload(){
		if (enabled) for (Player p : Bukkit.getOnlinePlayers()) for (Player pl : Bukkit.getOnlinePlayers()) p.showPlayer(pl);
	}
	public static void trigger(Player p){
		if (!enabled) return;
		hidePlayer(p);
		showInSameWorld(p);
	}
	public static void showInSameWorld(Player p) {
		for (Player pl : Bukkit.getOnlinePlayers()){
			if (p.getWorld().getName().equals(pl.getWorld().getName())) {
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
	public static void hidePlayer(Player p) {
		for (Player pl : Bukkit.getOnlinePlayers()){
			if (pl.getName().equals(p.getName())) continue;
			p.hidePlayer(pl);
			pl.hidePlayer(p);
		}
	}
}