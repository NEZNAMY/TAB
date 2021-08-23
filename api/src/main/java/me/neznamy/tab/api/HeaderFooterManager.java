package me.neznamy.tab.api;

public interface HeaderFooterManager {

	public void setHeader(TabPlayer player, String header);
	
	public void setFooter(TabPlayer player, String footer);
	
	public void setHeaderAndFooter(TabPlayer player, String header, String footer);
	
	public void resetHeader(TabPlayer player);
	
	public void resetFooter(TabPlayer player);
	
	public void resetHeaderAndFooter(TabPlayer player);
}
