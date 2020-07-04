package me.neznamy.tab.platforms.bukkit.placeholders.afk;

import java.util.UUID;

import org.bukkit.Bukkit;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;

public class AFKPlus implements AFKProvider {

	@Override
	public boolean isAFK(ITabPlayer p) {
		try {
			Object AFKPlus = Bukkit.getPluginManager().getPlugin("AFKPlus");
			Object AFKPlusPlayer = AFKPlus.getClass().getMethod("getPlayer", UUID.class).invoke(AFKPlus, p.getUniqueId());
			return (boolean) AFKPlusPlayer.getClass().getMethod("isAFK").invoke(AFKPlusPlayer);
		} catch (Throwable t) {
			return Shared.errorManager.printError(false, "Failed to check AFK status of " + p.getName() + " using AFKPlus", t);
		}
	}
}