package me.neznamy.tab.api.bossbar;

public interface BossBar {

	public String getName();
	
	public int getEntityId();
	
	public void setTitle(String title);
	
	public void setProgress(String progress);
	
	public void setProgress(float progress);
	
	public void setColor(String color);
	
	public void setColor(BarColor color);
	
	public void setStyle(String style);
	
	public void setStyle(BarStyle style);
}
