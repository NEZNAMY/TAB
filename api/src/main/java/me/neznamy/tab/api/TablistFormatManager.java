package me.neznamy.tab.api;

public interface TablistFormatManager {

	public void setPrefix(TabPlayer player, String prefix);
	
	public void setName(TabPlayer player, String customname);
	
	public void setSuffix(TabPlayer player, String suffix);
	
	public void resetPrefix(TabPlayer player);
	
	public void resetName(TabPlayer player);
	
	public void resetSuffix(TabPlayer player);
	
	public String getCustomPrefix(TabPlayer player);
	
	public String getCustomName(TabPlayer player);
	
	public String getCustomSuffix(TabPlayer player);
	
	public String getOriginalPrefix(TabPlayer player);
	
	public String getOriginalName(TabPlayer player);
	
	public String getOriginalSuffix(TabPlayer player);
	
}
