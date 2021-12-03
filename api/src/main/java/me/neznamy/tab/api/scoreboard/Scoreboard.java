package me.neznamy.tab.api.scoreboard;

import java.util.List;

/**
 * An interface allowing to work with scoreboard, such as adding players,
 * removing players and changing lines. New instance can be created using
 * {@link ScoreboardManager#createScoreboard(String, String, List)},
 * for scoreboards from config {@link ScoreboardManager#getRegisteredScoreboards()}.
 */
public interface Scoreboard {

	/**
	 * Returns internal name of this scoreboard. Name is defined in registration,
	 * scoreboards from config use name they were defined with. This value is
	 * used internally and in /tab announce scoreboard command.
	 * 
	 * @return	custom name of this scoreboard
	 */
	String getName();

	/**
	 * Returns raw title of this scoreboard. Placeholders stay in their
	 * raw format.
	 * 
	 * @return	scoreboard title
	 * @see		#setTitle(String)
	 */
	String getTitle();

	/**
	 * Sets title to specified value. Placeholders are refreshed automatically
	 * with refresh intervals defined in config. No need to call this method
	 * to try to keep placeholders up to date. Supports RGB codes using any of 
	 * the supported formats.
	 * <p>
	 * Length is limited to 32 characters on <1.13. If the limit is exceeded,
	 * text will be cut to 32 characters.
	 * <p>
	 * Calling this method with same title as before will not do anything.
	 * 
	 * @param	title
	 * 			New title to use with placeholder support
	 * @see		#getTitle()
	 */
	void setTitle(String title);

	/**
	 * Returns list of lines in this scoreboard in the order they appear
	 * in game (first line is on top). This list should only be used for reading,
	 * for adding/removing lines see {@link #addLine(String)} and {@link #removeLine(int)}.
	 * 
	 * @return	list of lines in this scoreboard
	 * @see		#addLine(String)
	 * @see		#removeLine(int)
	 */
	List<Line> getLines();

	/**
	 * Adds line with specified text on the bottom of scoreboard. Supports
	 * placeholders, which will automatically be refreshed.
	 * 
	 * @param	text
	 * 			Text to display
	 * @see		#getLines()
	 * @see		#removeLine(int)
	 */
	void addLine(String text);

	/**
	 * Removes line with specified index. Index starts at {@code 0} and ends at 
	 * {@link #getLines()}.size()-1.
	 * 
	 * @param	index
	 * 			Index of line to remove, starting at 0
	 * @throws	IndexOutOfBoundsException
	 * 			if the index is out of range (index < 0 || index >= {@link #getLines()}.size())
	 * @see		#getLines()
	 * @see		#addLine(String)
	 */
	void removeLine(int index);

	/**
	 * Unregisters this scoreboard from all players who can see it.
	 */
	void unregister();
}