package me.neznamy.tab.api.bossbar;

public interface BossBar {

	/**
	 * Returns name of this bossbar
	 * @return name of bossbar
	 */
	public String getName();
	
	/**
	 * Returns entity id of this wither on 1.8
	 * @return entity id of this wither
	 */
	public int getEntityId();
	
	/**
	 * Sets bossbar title to specified string, supporting placeholders
	 * @param title - new title
	 */
	public void setTitle(String title);
	
	/**
	 * Sets progress to specified string with placeholder support
	 * @param progress - progress placeholder
	 */
	public void setProgress(String progress);
	
	/**
	 * Sets progress to specified value
	 * @param progress - new progress
	 */
	public void setProgress(float progress);
	
	/**
	 * Sets color to specified value supporting placeholders
	 * @param color - color placeholder / value
	 */
	public void setColor(String color);
	
	/**
	 * Sets color to specified value
	 * @param color - new color
	 */
	public void setColor(BarColor color);
	
	/**
	 * Sets style to specified value supporting placeholders
	 * @param style - style placeholder / value
	 */
	public void setStyle(String style);
	
	/**
	 * Sets style to specified value
	 * @param style - new style
	 */
	public void setStyle(BarStyle style);
}
