package me.neznamy.tab.premium.scoreboard.lines;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.premium.scoreboard.Scoreboard;

/**
 * A stable (anti-flickering) line with dynamic text (supports placeholders) with numbers 1-15
 * Limitations:
 *   1.5.x - 1.12.x: up to 32 characters (depending on color/magic codes)
 */
public class NumberedStableDynamicLine extends StableDynamicLine {

	public NumberedStableDynamicLine(Scoreboard parent, int lineID, String text) {
		super(parent, lineID, text);
	}

	@Override
	public int getScoreFor(TabPlayer p) {
		return lineID;
	}
}