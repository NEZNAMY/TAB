package me.neznamy.tab.platforms.bukkit.placeholders.afk;

import org.bukkit.entity.Player;

import me.neznamy.tab.api.TabPlayer;

/**
 * Hook into https://www.spigotmc.org/resources/21208/ for %afk%
 */
public class AntiAFKPlus implements AFKProvider {

	@Override
	public boolean isAFK(TabPlayer p) throws Exception {
		Object api = Class.forName("de.kinglol12345.AntiAFKPlus.api.AntiAFKPlusAPI").getDeclaredMethod("getAPI").invoke(null);
		return (boolean) api.getClass().getMethod("isAFK", Player.class).invoke(api, p.getBukkitEntity());
	}
}