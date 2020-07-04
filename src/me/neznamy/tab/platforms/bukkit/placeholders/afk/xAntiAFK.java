package me.neznamy.tab.platforms.bukkit.placeholders.afk;

import org.bukkit.entity.Player;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;

public class xAntiAFK implements AFKProvider {

	@Override
	public boolean isAFK(ITabPlayer p) {
		try {
			return (boolean) Class.forName("ch.soolz.xantiafk.xAntiAFKAPI").getMethod("isAfk", Player.class).invoke(null, p.getBukkitEntity());
		} catch (Throwable t) {
			return Shared.errorManager.printError(false, "Failed to check AFK status of " + p.getName() + " using xAntiAFK", t);
		}
	}
}