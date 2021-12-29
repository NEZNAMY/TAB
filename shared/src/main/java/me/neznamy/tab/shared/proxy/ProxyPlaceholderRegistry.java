package me.neznamy.tab.shared.proxy;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.placeholder.PlaceholderManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.placeholders.PlaceholderRegistry;

public abstract class ProxyPlaceholderRegistry implements PlaceholderRegistry {

	@Override
	public void registerPlaceholders(PlaceholderManager manager) {
		manager.registerPlayerPlaceholder("%online%", 1000, p -> {
			int count = 0;
			for (TabPlayer all : TAB.getInstance().getOnlinePlayers()){
				if (!all.isVanished() || p.hasPermission(TabConstants.Permission.GLOBAL_PLAYERLIST_SEE_VANISHED)) count++;
			}
			return count;
		});
		manager.registerPlayerPlaceholder("%staffonline%", 2000, p -> {
			int count = 0;
			for (TabPlayer all : TAB.getInstance().getOnlinePlayers()){
				if (all.hasPermission(TabConstants.Permission.STAFF) && (!all.isVanished() || p.hasPermission(TabConstants.Permission.GLOBAL_PLAYERLIST_SEE_VANISHED))) count++;
			}
			return count;
		});
	}
}