package me.neznamy.tab.platforms.bukkit.placeholders.afk;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.neznamy.tab.api.AFKProvider;
import me.neznamy.tab.api.TabPlayer;

/**
 * Hook into Essentials for %afk%
 */
public class Essentials implements AFKProvider {

	private Object essentials;

	public Essentials() {
		essentials = Bukkit.getPluginManager().getPlugin("Essentials");
	}
	
	@Override
	public boolean isAFK(TabPlayer p) throws Exception {
		Object user = essentials.getClass().getMethod("getUser", Player.class).invoke(essentials, p.getPlayer());
		return (boolean) user.getClass().getMethod("isAfk").invoke(user);
	}
}