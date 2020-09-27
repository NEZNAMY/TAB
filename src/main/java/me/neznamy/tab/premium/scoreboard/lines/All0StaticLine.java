package me.neznamy.tab.premium.scoreboard.lines;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.premium.scoreboard.Scoreboard;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * A line with static text (no placeholders) with 0 everywhere
 * Limitations:
 *   1.5.x - 1.7.x: up to 42 characters (depending on color/magic codes)
 *   1.8.x - 1.12.x: up to 66 characters (depending on color/magic codes)
 */
public class All0StaticLine extends StaticLine {

	private Scoreboard parent;
	private String originalText;

	public All0StaticLine(Scoreboard parent, int lineNumber, String text) {
		super(parent, lineNumber, text, getPlayerName(lineNumber));
		this.parent = parent;
		this.originalText = Placeholders.color(text);
	}

	@Override
	public void register(TabPlayer p) {
		p.setProperty(teamName, text);
		if (p.getVersion().getMinorVersion() >= 13) {
			PacketAPI.registerScoreboardScore(p, teamName, getPlayerName(), originalText, "", ObjectiveName, parent.manager.staticNumber);
		} else if (p.getVersion().getMinorVersion() >= 8) {
			PacketAPI.registerScoreboardScore(p, teamName, name, prefix, suffix, ObjectiveName, parent.manager.staticNumber);
		} else {
			//<1.8 does not support sorting by name which we abuse here
			PacketAPI.registerScoreboardScore(p, teamName, name1_7, prefix1_7, suffix1_7, ObjectiveName, parent.lines.size() + 1 - lineNumber);
		}
	}

	@Override
	public void unregister(TabPlayer p) {
		if (p.getProperty(teamName).get().length() > 0) {
			PacketAPI.removeScoreboardScore(p, p.getVersion().getMinorVersion() >= 13 ? getPlayerName() : p.getVersion().getMinorVersion() >= 8 ? this.name : name1_7, teamName);
		}
	}
}