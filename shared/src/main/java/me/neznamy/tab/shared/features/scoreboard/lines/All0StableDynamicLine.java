package me.neznamy.tab.shared.features.scoreboard.lines;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardImpl;

/**
 * A stable (anti-flickering) line with dynamic text (supports placeholders) with 0 everywhere
 * Limitations:
 *   1.5.x - 1.12.x: up to 32 characters (depending on color/magic codes)
 */
public class All0StableDynamicLine extends StableDynamicLine {

	/**
	 * Constructs new instance with given parameter
	 * @param parent - scoreboard this line belongs to
	 * @param lineNumber - ID of this line
	 * @param text - text to display in the line
	 */
	public All0StableDynamicLine(ScoreboardImpl parent, int lineNumber, String text) {
		super(parent, lineNumber, text);
	}

	@Override
	public int getScoreFor(TabPlayer p) {
		//<1.8 does not support sorting by name which we abuse here
		return p.getVersion().getMinorVersion() >= 8 ? parent.getManager().getStaticNumber() : parent.getLines().size() + 1 - lineNumber;
	}
}