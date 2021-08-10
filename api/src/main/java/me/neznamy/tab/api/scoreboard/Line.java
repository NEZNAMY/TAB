package me.neznamy.tab.api.scoreboard;

public interface Line {

	/**
	 * Returns configured raw text of this line
	 * @return raw text of this line
	 */
	public String getText();
	
	/**
	 * Changes text to new value. If value is identical to previous one, nothing happens.
	 * @param text - text to change line to
	 */
	public void setText(String text);
}