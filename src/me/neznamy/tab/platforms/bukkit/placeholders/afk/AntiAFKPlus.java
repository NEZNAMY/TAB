package me.neznamy.tab.platforms.bukkit.placeholders.afk;

import org.bukkit.entity.Player;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;

public class AntiAFKPlus implements AFKProvider {

	@Override
	public boolean isAFK(ITabPlayer p) {
		try {
			Object api = Class.forName("de.kinglol12345.AntiAFKPlus.api.AntiAFKPlusAPI").getDeclaredMethod("getAPI").invoke(null);
			return (boolean) api.getClass().getMethod("isAFK", Player.class).invoke(api, p.getBukkitEntity());
		} catch (Throwable t) {
			return Shared.errorManager.printError(false, "Failed to check AFK status of " + p.getName() + " using AntiAFKPlus", t);
		}
	}
}