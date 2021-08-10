package me.neznamy.tab.api.scoreboard;

import java.util.List;
import java.util.Set;

import me.neznamy.tab.api.TabPlayer;

/**
 * An interface to be worked with to manipulate scoreboard
 */
public interface Scoreboard {

	/**
	 * Registers this scoreboard to specified player
	 * @param p - player to register scoreboard for
	 */
	public void addPlayer(TabPlayer p);
	
	/**
	 * Unregisters this scoreboard from specified player
	 * @param p - player to unregister scoreboard for
	 */
	public void removePlayer(TabPlayer p);

	/**
	 * Returns list of all players who can see this scoreboard
	 * @return list of players who can see this scoreboard
	 */
	public Set<TabPlayer> getPlayers();
	
	/**
	 * Returns name of this scoreboard
	 * @return name of this scoreboard
	 */
	public String getName();
	
	/**
	 * Returns scoreboard title
	 * @return scoreboard title
	 */
	public String getTitle();
	
	/**
	 * Sets title to provided value. Supports RGB codes using any of the supported formats.
	 * @param title - title to use
	 */
	public void setTitle(String title);
	
	/**
	 * Returns list of lines of this scoreboard
	 * @return list of lines of this scoreboard
	 */
	public List<Line> getLines();
	
	/**
	 * Adds line with specified text on the bottom of scoreboard
	 * @param text - text to display
	 */
	public void addLine(String text);
	
	/**
	 * Removes line with specified index. Index starts at 0 and ends at getLines().size()-1
	 * @param index - index of line to remove
	 */
	public void removeLine(int index);
}