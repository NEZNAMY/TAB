package me.neznamy.tab.api;

import me.neznamy.tab.shared.Property;

public interface ArmorStand {

	public boolean hasStaticOffset();
	
	public void setOffset(double offset);
	
	public double getOffset();
	
	public Property getProperty();
	
	public void teleport();
	
	public void teleport(TabPlayer viewer);
	
	public void sneak(boolean sneaking);
	
	public void destroy();
	
	public void destroy(TabPlayer viewer);
	
	public void refresh();
	
	public void updateVisibility();
	
	public void removeFromRegistered(TabPlayer viewer);
	
	public int getEntityId();
	
	public void spawn(TabPlayer viewer, boolean addToRegistered);
}
