package me.neznamy.tab.shared.proxy;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.PlaceholderRegistry;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;

public abstract class ProxyPlaceholderRegistry implements PlaceholderRegistry {

	@Override
	public List<Placeholder> registerPlaceholders() {
		List<Placeholder> placeholders = new ArrayList<>();
		placeholders.add(new PlayerPlaceholder("%canseeonline%", 2000) {
			public String get(TabPlayer p) {
				int count = 0;
				for (TabPlayer all : TAB.getInstance().getPlayers()){
					if (!all.isVanished() || p.hasPermission("tab.seevanished")) count++;
				}
				return String.valueOf(count);
			}
		});
		placeholders.add(new PlayerPlaceholder("%canseestaffonline%", 2000) {
			public String get(TabPlayer p) {
				int count = 0;
				for (TabPlayer all : TAB.getInstance().getPlayers()){
					if (all.isStaff() && (!all.isVanished() || p.hasPermission("tab.seevanished"))) count++;
				}
				return String.valueOf(count);
			}
		});
		return placeholders;
	}
}