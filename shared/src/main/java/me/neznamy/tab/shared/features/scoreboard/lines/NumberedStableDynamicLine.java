package me.neznamy.tab.shared.features.scoreboard.lines;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardImpl;

/**
 * A stable (anti-flickering) line with dynamic text (supports placeholders) with numbers 1-15
 * Limitations:
 *   1.5.x - 1.12.x: up to 32 characters (depending on color/magic codes)
 */
public class NumberedStableDynamicLine extends StableDynamicLine {

	/**
	 * Constructs new instance with given parameters
	 * @param parent - scoreboard this line belongs to
	 * @param lineNumber - ID of this line
	 * @param text - text to display
	 */
	public NumberedStableDynamicLine(ScoreboardImpl parent, int lineNumber, String text) {
		super(parent, lineNumber, text);
	}

	@Override
	public int getScoreFor(TabPlayer p) {
		return parent.getLines().size() + 1 - lineNumber;
	}
}