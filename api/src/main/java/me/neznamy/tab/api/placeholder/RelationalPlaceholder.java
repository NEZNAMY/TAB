package me.neznamy.tab.api.placeholder;

import me.neznamy.tab.api.TabPlayer;

public interface RelationalPlaceholder extends Placeholder {

	void updateValue(TabPlayer viewer, TabPlayer target, Object value);
	
	Object request(TabPlayer viewer, TabPlayer target);
}