package me.neznamy.tab.premium.scoreboard.lines;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.premium.scoreboard.Scoreboard;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.packets.IChatBaseComponent;

public abstract class StaticLine extends ScoreboardLine {

	protected Scoreboard parent;
	protected int lineNumber;
	protected String text;
	
	protected String prefix1_7;
	protected String name1_7;
	protected String suffix1_7;

	protected String prefix;
	protected String name;
	protected String suffix;
	
	public StaticLine(Scoreboard parent, int lineNumber, String text, String forcedNameStart) {
		super(lineNumber);
		this.parent = parent;
		this.lineNumber = lineNumber;
		this.text = IChatBaseComponent.fromColoredText(text).toLegacyText(); //colorizing + translating RGB codes into legacy
		//1.8+
		if (this.text.length() <= (40 - forcedNameStart.length())) {
			prefix = "";
			name = forcedNameStart + this.text;
			suffix = "";
		} else {
			String[] prefix_other = split(this.text, 16);
			prefix = prefix_other[0];
			String other = prefix_other[1];
			if (forcedNameStart.length() > 0) {
				other = forcedNameStart + PlaceholderManager.getLastColors(prefix) + other;
			}
			String[] name_suffix = split(other, 40);
			name = name_suffix[0];
			suffix = name_suffix[1];
		}
		//1.7-
		if (this.text.length() <= (16 - forcedNameStart.length())) {
			prefix1_7 = "";
			name1_7 = forcedNameStart + this.text;
			suffix1_7 = "";
		} else {
			String[] prefix_other = split(this.text, 16);
			prefix1_7 = prefix_other[0];
			String other = prefix_other[1];
			if (forcedNameStart.length() > 0) {
				other = forcedNameStart + PlaceholderManager.getLastColors(prefix1_7) + other;
			}
			String[] name_suffix = split(other, 16);
			name1_7 = name_suffix[0];
			suffix1_7 = name_suffix[1];
		}
	}
	
	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		//nothing to refresh
	}

	@Override
	public void refreshUsedPlaceholders() {
		//no placeholders
	}
}