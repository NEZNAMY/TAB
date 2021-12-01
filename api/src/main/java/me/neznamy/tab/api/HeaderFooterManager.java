package me.neznamy.tab.api;

public interface HeaderFooterManager {

	void setHeader(TabPlayer player, String header);
	
	void setFooter(TabPlayer player, String footer);
	
	void setHeaderAndFooter(TabPlayer player, String header, String footer);
	
	void resetHeader(TabPlayer player);
	
	void resetFooter(TabPlayer player);
	
	void resetHeaderAndFooter(TabPlayer player);
}
