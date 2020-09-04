package me.neznamy.tab.platforms.bukkit.placeholders.afk;

import java.util.UUID;

import org.bukkit.Bukkit;

import me.neznamy.tab.api.AFKProvider;
import me.neznamy.tab.api.TabPlayer;

/**
 * Hook into https://www.spigotmc.org/resources/35065/ for %afk%
 */
public class AFKPlus implements AFKProvider {

	@Override
	public boolean isAFK(TabPlayer p) throws Exception {
		Object AFKPlus = Bukkit.getPluginManager().getPlugin("AFKPlus");
		Object AFKPlusPlayer = AFKPlus.getClass().getMethod("getPlayer", UUID.class).invoke(AFKPlus, p.getUniqueId());
		return (boolean) AFKPlusPlayer.getClass().getMethod("isAFK").invoke(AFKPlusPlayer);
	}
}