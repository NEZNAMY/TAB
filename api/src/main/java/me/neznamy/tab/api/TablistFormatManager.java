package me.neznamy.tab.api;

public interface TablistFormatManager {

	void setPrefix(TabPlayer player, String prefix);
	
	void setName(TabPlayer player, String customName);
	
	void setSuffix(TabPlayer player, String suffix);
	
	void resetPrefix(TabPlayer player);
	
	void resetName(TabPlayer player);
	
	void resetSuffix(TabPlayer player);
	
	String getCustomPrefix(TabPlayer player);
	
	String getCustomName(TabPlayer player);
	
	String getCustomSuffix(TabPlayer player);
	
	String getOriginalPrefix(TabPlayer player);
	
	String getOriginalName(TabPlayer player);
	
	String getOriginalSuffix(TabPlayer player);
	
}
