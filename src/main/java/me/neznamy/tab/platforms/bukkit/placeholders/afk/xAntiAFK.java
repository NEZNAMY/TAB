package me.neznamy.tab.platforms.bukkit.placeholders.afk;

import org.bukkit.entity.Player;

import me.neznamy.tab.shared.ITabPlayer;

/**
 * Hook into https://www.spigotmc.org/resources/63466/ for %afk%
 */
public class xAntiAFK implements AFKProvider {

	@Override
	public boolean isAFK(ITabPlayer p) throws Exception {
		return (boolean) Class.forName("ch.soolz.xantiafk.xAntiAFKAPI").getMethod("isAfk", Player.class).invoke(null, p.getBukkitEntity());
	}
}