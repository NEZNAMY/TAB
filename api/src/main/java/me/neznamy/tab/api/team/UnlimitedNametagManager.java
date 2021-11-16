package me.neznamy.tab.api.team;

import me.neznamy.tab.api.TabPlayer;

public interface UnlimitedNametagManager extends TeamManager {

	public void disableArmorStands(TabPlayer player);
	
	public void enableArmorStands(TabPlayer player);
	
	public boolean hasDisabledArmorStands(TabPlayer player);
}
