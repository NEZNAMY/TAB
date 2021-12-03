package me.neznamy.tab.api.placeholder;

import me.neznamy.tab.api.TabPlayer;

public interface PlayerPlaceholder extends Placeholder {

	void updateValue(TabPlayer player, Object value);
	
	Object request(TabPlayer p);
}