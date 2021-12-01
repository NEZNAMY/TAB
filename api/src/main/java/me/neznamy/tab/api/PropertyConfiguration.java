package me.neznamy.tab.api;

public interface PropertyConfiguration {

	void setProperty(String name, String property, String server, String world, String value);
	
	String[] getProperty(String name, String property, String server, String world);
	
	void remove(String name);
}