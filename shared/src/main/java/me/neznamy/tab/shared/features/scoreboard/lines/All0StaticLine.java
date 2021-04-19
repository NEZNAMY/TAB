package me.neznamy.tab.shared.features.scoreboard.lines;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.scoreboard.Scoreboard;

/**
 * A line with static text (no placeholders) with 0 everywhere
 * Limitations:
 *   1.5.x - 1.7.x: up to 42 characters (depending on color/magic codes)
 *   1.8.x - 1.12.x: up to 66 characters (depending on color/magic codes)
 */
public class All0StaticLine extends StaticLine {

	/**
	 * Constructs new instance with given parameters
	 * @param parent - scoreboard this line belongs to
	 * @param lineNumber - ID of this line
	 * @param text - text of line
	 */
	public All0StaticLine(Scoreboard parent, int lineNumber, String text) {
		super(parent, lineNumber, text, getPlayerName(lineNumber));
	}

	@Override
	public void register(TabPlayer p) {
		if (p.getVersion().getMinorVersion() >= 13) {
			addLine(p, teamName, playerName, text, "", parent.manager.staticNumber);
		} else if (p.getVersion().getMinorVersion() >= 8) {
			addLine(p, teamName, name, prefix, suffix, parent.manager.staticNumber);
		} else {
			//<1.8 does not support sorting by name which we abuse here
			addLine(p, teamName, name1_7, prefix1_7, suffix1_7, parent.lines.size() + 1 - lineNumber);
		}
	}

	@Override
	public void unregister(TabPlayer p) {
		if (text.length() > 0) {
			removeLine(p, p.getVersion().getMinorVersion() >= 13 ? playerName : p.getVersion().getMinorVersion() >= 8 ? name : name1_7, teamName);
		}
	}
}