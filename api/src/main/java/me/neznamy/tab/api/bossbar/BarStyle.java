package me.neznamy.tab.api.bossbar;

/**
 * An enum representing all available BossBar styles.
 * Calling ordinal() will return style's network ID.
 * Style names are equal to NMS names.
 */
public enum BarStyle {

	PROGRESS("SOLID"),
	NOTCHED_6("SEGMENTED_6"),
	NOTCHED_10("SEGMENTED_10"),
	NOTCHED_12("SEGMENTED_12"),
	NOTCHED_20("SEGMENTED_20");

	/** Style's name used in Bukkit API */
	private final String bukkitName;

	/**
	 * Initializes enum constant with given bukkit name
	 * 
	 * @param 	bukkitName
	 * 			Name of color to be returned in {@code getBukkitName()}
	 */
	BarStyle(String bukkitName){
		this.bukkitName = bukkitName;
	}

	/**
	 * Returns name of this style in Bukkit API
	 * @return	name of this style in Bukkit API
	 */
	public String getBukkitName() {
		return bukkitName;
	}
}