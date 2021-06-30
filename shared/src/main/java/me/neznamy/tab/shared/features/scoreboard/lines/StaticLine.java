package me.neznamy.tab.shared.features.scoreboard.lines;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardImpl;
import me.neznamy.tab.shared.rgb.RGBUtils;

public abstract class StaticLine extends ScoreboardLine {

	//text for 1.13+
	protected String text;
	
	//values for 1.7 clients with 16 character limit for player name
	protected String prefix17;
	protected String name17;
	protected String suffix17;

	//values for 1.8-1.12 clients with 40 character limit for player name
	protected String prefix;
	protected String name;
	protected String suffix;
	
	protected StaticLine(ScoreboardImpl parent, int lineNumber, String text, String forcedNameStart) {
		super(parent, lineNumber);
		this.text = TAB.getInstance().getPlaceholderManager().color(text);
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
	
	private String[] splitText(String playerNameStart, String text, int maxNameLength) {
		String prefixValue;
		String nameValue;
		String suffixValue;
		if (text.length() <= (maxNameLength - playerNameStart.length())) {
			prefixValue = "";
			nameValue = playerNameStart + text;
			suffixValue = "";
		} else {
			String[] prefixOther = split(text, 16);
			prefixValue = prefixOther[0];
			String other = prefixOther[1];
			if (playerNameStart.length() > 0) {
				other = playerNameStart + TAB.getInstance().getPlaceholderManager().getLastColors(prefixValue) + other;
			}
			String[] nameSuffix = split(other, maxNameLength);
			nameValue = nameSuffix[0];
			suffixValue = nameSuffix[1];
		}
		return new String[]{prefixValue, nameValue, suffixValue};
	}
	
	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		//nothing to refresh
	}

	@Override
	public void refreshUsedPlaceholders() {
		//no placeholders
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
}