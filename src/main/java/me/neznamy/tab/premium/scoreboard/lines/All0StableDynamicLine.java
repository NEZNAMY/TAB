package me.neznamy.tab.premium.scoreboard.lines;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.premium.scoreboard.Scoreboard;

/**
 * A stable (anti-flickering) line with dynamic text (supports placeholders) with 0 everywhere
 * Limitations:
 *   1.5.x - 1.12.x: up to 32 characters (depending on color/magic codes)
 */
public class All0StableDynamicLine extends StableDynamicLine {

	public All0StableDynamicLine(Scoreboard parent, int lineID, String text) {
		super(parent, lineID, text);
	}

	@Override
	public int getScoreFor(TabPlayer p) {
		//<1.8 does not support sorting by name which we abuse here
		return p.getVersion().getMinorVersion() >= 8 ? parent.manager.staticNumber : lineID;
	}
}