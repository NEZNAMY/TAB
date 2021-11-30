package me.neznamy.tab.api.placeholder;

import me.neznamy.tab.api.TabPlayer;

public interface PlayerPlaceholder extends Placeholder {

	public void updateValue(TabPlayer player, Object value);
	
	public Object request(TabPlayer p);
}