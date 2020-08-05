package me.neznamy.tab.platforms.bukkit.placeholders.afk;

import org.bukkit.Bukkit;

import com.Zrips.CMI.Containers.CMIUser;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;

public class CMI implements AFKProvider {

	@Override
	public boolean isAFK(ITabPlayer p) {
		//cannot be accessed via reflection due to a java issue
		CMIUser user = com.Zrips.CMI.CMI.getInstance().getPlayerManager().getUser(p.getBukkitEntity());
		if (user == null) {
			Shared.errorManager.printError("CMI v" + Bukkit.getPluginManager().getPlugin("CMI") + "returned null user for " + p.getName() + " (" + p.getUniqueId() + ")");
			return false;
		}
		return user.isAfk();
	}
}