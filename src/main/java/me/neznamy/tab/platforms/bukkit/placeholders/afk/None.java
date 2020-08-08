package me.neznamy.tab.platforms.bukkit.placeholders.afk;

import me.neznamy.tab.shared.ITabPlayer;

public class None implements AFKProvider {

	@Override
	public boolean isAFK(ITabPlayer p) {
		return false;
	}
}