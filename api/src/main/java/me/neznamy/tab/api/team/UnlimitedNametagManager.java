package me.neznamy.tab.api.team;

import java.util.List;

import me.neznamy.tab.api.TabPlayer;

public interface UnlimitedNametagManager extends TeamManager {

	void disableArmorStands(TabPlayer player);
	
	void enableArmorStands(TabPlayer player);
	
	boolean hasDisabledArmorStands(TabPlayer player);

	void setName(TabPlayer player, String customName);
	
	void setLine(TabPlayer player, String line, String value);

	void resetName(TabPlayer player);
	
	void resetLine(TabPlayer player, String line);

	String getCustomName(TabPlayer player);
	
	String getCustomLineValue(TabPlayer player, String line);

	String getOriginalName(TabPlayer player);
	
	String getOriginalLineValue(TabPlayer player, String line);
	
	List<String> getDefinedLines();
}
