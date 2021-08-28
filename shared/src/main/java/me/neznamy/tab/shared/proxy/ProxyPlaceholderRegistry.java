package me.neznamy.tab.shared.proxy;

import me.neznamy.tab.api.PlaceholderManager;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.PlaceholderRegistry;

public abstract class ProxyPlaceholderRegistry implements PlaceholderRegistry {

	@Override
	public void registerPlaceholders(PlaceholderManager manager) {
		manager.registerPlayerPlaceholder("%online%", 2000, p -> {
			int count = 0;
			for (TabPlayer all : TAB.getInstance().getOnlinePlayers()){
				if (!all.isVanished() || p.hasPermission("tab.seevanished")) count++;
			}
			return count;
		});
		manager.registerPlayerPlaceholder("%staffonline%", 2000, p -> {
			int count = 0;
			for (TabPlayer all : TAB.getInstance().getOnlinePlayers()){
				if (all.hasPermission("tab.staff") && (!all.isVanished() || p.hasPermission("tab.seevanished"))) count++;
			}
			return count;
		});
		manager.registerPlayerPlaceholder("%internal:vanished%", 1000, p -> {
			((ProxyTabPlayer)p).getPluginMessageHandler().requestAttribute(p, "vanished");
			return p.isVanished();
		});
	}
}