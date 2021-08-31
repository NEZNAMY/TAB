package me.neznamy.tab.api;

public interface PropertyConfiguration {

	public void setProperty(String name, String property, String server, String world, String value);
	
	public String[] getProperty(String name, String property, String server, String world);
	
	public void remove(String name);
}