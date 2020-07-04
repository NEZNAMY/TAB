package me.neznamy.tab.platforms.bukkit.placeholders.afk;

import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.placeholders.Placeholders;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;

public interface AFKProvider {

	public boolean isAFK(ITabPlayer p);
	
	public default void register() {
		Shared.debug("Loaded AFK provider: " + getClass().getSimpleName());
		Placeholders.registerPlaceholder(new PlayerPlaceholder("%afk%", 500) {
			public String get(ITabPlayer p) {
				return isAFK(p) ? Configs.yesAfk : Configs.noAfk;
			}
			@Override
			public String[] getChilds(){
				return new String[] {Configs.yesAfk, Configs.noAfk};
			}
		});
	}
}
