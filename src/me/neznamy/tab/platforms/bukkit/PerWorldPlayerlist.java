package me.neznamy.tab.platforms.bukkit;

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
//		System.out.println("triggering world change/join for " + p.getName());
		hidePlayer(p);
		showInSameWorld(p);
	}
	public static void showInSameWorld(Player p) {
		for (Player pl : Bukkit.getOnlinePlayers()){
			if (pl == p) continue;
			if (p.getWorld() == pl.getWorld()) {
//				System.out.println("showing " + p.getName() + " and " + pl.getName() + " mutually because they are in the same world");
				p.showPlayer(pl);
				pl.showPlayer(p);
			}
			if ((allowBypass && p.hasPermission("tab.bypass")) || ignoredWorlds.contains(p.getWorld().getName())) {
//				System.out.println("showing " + pl.getName() + " to " + p.getName() + " because  bypassPermision & has bypass permission OR " + p.getName() + " is in ignored world");
				p.showPlayer(pl);
			}
			if ((allowBypass && pl.hasPermission("tab.bypass")) || ignoredWorlds.contains(pl.getWorld().getName())) {
//				System.out.println("showing " + p.getName() + " to " + pl.getName() + " because  bypassPermision & has bypass permission OR " + pl.getName() + " is in ignored world");
				pl.showPlayer(p);
			}
		}
	}
	public static void hidePlayer(Player p) {
		for (Player pl : Bukkit.getOnlinePlayers()){
			if (pl == p) continue;
//			System.out.println("hiding players " + p.getName() + " and " + pl.getName() + " mutually");
			p.hidePlayer(pl);
			pl.hidePlayer(p);
		}
	}
}