package me.neznamy.tab.shared.features.scoreboard.lines;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.rgb.RGBUtils;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardImpl;

/**
 * A line with static text (no placeholders)
 * Limitations:
 *   1.5.x - 1.7.x: 48 characters (42 if using same number on all lines)
 *   1.8.x - 1.12.x: 72 characters (66 if using same number on all lines)
 *   1.13+: unlimited
 */
public class StaticLine extends ScoreboardLine {

	//values for 1.7 clients with 16-character limit for player name
	protected String prefix17;
	protected String name17;
	protected String suffix17;

	//values for 1.8-1.12 clients with 40-character limit for player name
	protected String prefix;
	protected String name;
	protected String suffix;
	
	public StaticLine(ScoreboardImpl parent, int lineNumber, String text) {
		super(parent, lineNumber);
		this.text = EnumChatFormat.color(text);
		setValues(this.text);
	}
	
	private void setValues(String text) {
		super.text = text;
		String forcedNameStart = parent.getManager().isUsingNumbers() ? "" : getPlayerName(lineNumber);
		String legacy = RGBUtils.getInstance().convertRGBtoLegacy(this.text);
		//1.8+
		String[] v18 = splitText(forcedNameStart, legacy, 40);
		prefix = v18[0];
		name = v18[1];
		suffix = v18[2];
		//1.7-
		String[] v17 = splitText(forcedNameStart, legacy, 16);
		prefix17 = v17[0];
		name17 = v17[1];
		suffix17 = v17[2];
	}

	protected String getPlayerName(TabPlayer viewer) {
		if (viewer.getVersion().getMinorVersion() >= 13) {
			return playerName;
		} else if (viewer.getVersion().getMinorVersion() >= 8) {
			return name;
		} else {
			return name17;
		}
	}
	
	@Override
	public void register(TabPlayer p) {
		if (p.getVersion().getMinorVersion() >= 13) {
			addLine(p, playerName, text, "");
		} else if (p.getVersion().getMinorVersion() >= 8) {
			addLine(p, name, prefix, suffix);
		} else {
			addLine(p, name17, prefix17, suffix17);
		}
	}

	@Override
	public void unregister(TabPlayer p) {
		if (text.length() > 0) {
			removeLine(p, getPlayerName(p));
		}
	}

	@Override
	public void setText(String text) {
		setValues(text);
		for (TabPlayer p : parent.getPlayers()) {
			unregister(p);
			register(p);
		}
	}
}