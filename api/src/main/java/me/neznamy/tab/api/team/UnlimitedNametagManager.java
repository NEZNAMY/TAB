package me.neznamy.tab.api.team;

import java.util.List;

import me.neznamy.tab.api.TabPlayer;

public interface UnlimitedNametagManager extends TeamManager {

	public void disableArmorStands(TabPlayer player);
	
	public void enableArmorStands(TabPlayer player);
	
	public boolean hasDisabledArmorStands(TabPlayer player);

	public void setName(TabPlayer player, String customname);
	
	public void setLine(TabPlayer player, String line, String value);

	public void resetName(TabPlayer player);
	
	public void resetLine(TabPlayer player, String line);

	public String getCustomName(TabPlayer player);
	
	public String getCustomLineValue(TabPlayer player, String line);

	public String getOriginalName(TabPlayer player);
	
	public String getOriginalLineValue(TabPlayer player, String line);
	
	public List<String> getDefinedLines();
}
