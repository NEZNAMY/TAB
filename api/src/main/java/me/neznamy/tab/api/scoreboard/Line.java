package me.neznamy.tab.api.scoreboard;

/**
 * An interface allowing to work with a line of
 * text in scoreboard.
 *
 */
public interface Line {

	/**
	 * Returns configured raw text of this line. Placeholders
	 * remain in raw format.
	 * 
	 * @return	raw text of this line
	 */
	String getText();
	
	/**
	 * Changes text to new value. Supports placeholders, which are
	 * automatically registered if needed and refreshed periodically
	 * based on configuration. No need to call this method to try to
	 * keep placeholders up to date. If value is identical to previous 
	 * one, nothing happens.
	 * 
	 * @param	text
	 * 			Raw text to display in the line
	 */
	void setText(String text);
}