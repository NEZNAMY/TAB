package me.neznamy.tab.shared.features.scoreboard.lines;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardImpl;

/**
 * A line with static text (no placeholders) with numbers 1-15
 * Limitations:
 *   1.5.x - 1.7.x: 48 characters
 *   1.8.x - 1.12.x: 72 characters
 */
public class NumberedStaticLine extends StaticLine {

	/**
	 * Constructs new instance with given parameters
	 * @param parent - scoreboard this line belongs to
	 * @param lineNumber - ID of this line
	 * @param text - text to display
	 */
	public NumberedStaticLine(ScoreboardImpl parent, int lineNumber, String text) {
		super(parent, lineNumber, text, "");
	}

	@Override
	public void register(TabPlayer p) {
		if (p.getVersion().getMinorVersion() >= 13) {
			addLine(p, teamName, playerName, text, "", parent.getLines().size() + 1 - lineNumber);
		} else if (p.getVersion().getMinorVersion() >= 8) {
			addLine(p, teamName, name, prefix, suffix, parent.getLines().size() + 1 - lineNumber);
		} else {
			//<1.8 does not support sorting by name which we abuse here
			addLine(p, teamName, name17, prefix17, suffix17, parent.getLines().size() + 1 - lineNumber);
		}
	}

	@Override
	public void unregister(TabPlayer p) {
		removeLine(p, getPlayerName(p), teamName);
	}
}