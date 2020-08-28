package me.neznamy.tab.platforms.bukkit.placeholders.afk;

import org.bukkit.Bukkit;

import me.neznamy.tab.shared.ITabPlayer;

/**
 * Hook into Essentials for %afk%
 */
public class Essentials implements AFKProvider {

	private com.earth2me.essentials.Essentials essentials;

	public Essentials() {
		essentials = (com.earth2me.essentials.Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
	}
	@Override
	public boolean isAFK(ITabPlayer p) {
		return essentials.getUser(p.getBukkitEntity()).isAfk();
	}
}