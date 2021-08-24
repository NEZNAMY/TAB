package me.neznamy.tab.api.bossbar;

/**
 * An enum representing bossbar styles using same names as NMS
 */
public enum BarStyle {

	PROGRESS("SOLID"),
	NOTCHED_6("SEGMENTED_6"),
	NOTCHED_10("SEGMENTED_10"),
	NOTCHED_12("SEGMENTED_12"),
	NOTCHED_20("SEGMENTED_20");
	
	private String bukkitName;
	
	private BarStyle(String bukkitName){
		this.bukkitName = bukkitName;
	}
	
	public String getBukkitName() {
		return bukkitName;
	}
}