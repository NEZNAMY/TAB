package me.neznamy.tab.platforms.bukkit.placeholders.afk;

import org.bukkit.Bukkit;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;

public class Essentials implements AFKProvider {

	private com.earth2me.essentials.Essentials essentials;
	
	public Essentials() {
		essentials = (com.earth2me.essentials.Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
	}
	@Override
	public boolean isAFK(ITabPlayer p) {
		try {
			return essentials.getUser(p.getBukkitEntity()).isAfk();
		} catch (Throwable t) {
			return Shared.errorManager.printError(false, "Failed to check AFK status of " + p.getName() + " using Essentials", t);
		}
	}
}