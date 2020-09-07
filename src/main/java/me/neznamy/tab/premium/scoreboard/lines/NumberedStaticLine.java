package me.neznamy.tab.premium.scoreboard.lines;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.packets.IChatBaseComponent;

/**
 * A line with static text (no placeholders) with numbers 1-15
 * Limitations:
 *   1.5.x - 1.7.x: 48 characters
 *   1.8.x - 1.12.x: 72 characters
 */
public class NumberedStaticLine extends ScoreboardLine {

	private int lineID;
	private String text;
	
	private String prefix1_7;
	private String name1_7;
	private String suffix1_7;
	
	private String prefix;
	private String name;
	private String suffix;
	
	public NumberedStaticLine(int lineID, String text) {
		super(lineID);
		this.lineID = lineID;
		this.text = IChatBaseComponent.fromColoredText(text).toColoredText(); //colorizing + translating RGB codes into legacy
		//1.8+
		if (this.text.length() <= 40) {
			prefix = "";
			name = this.text;
			suffix = "";
		} else {
			String[] prefix_other = split(this.text, 16);
			prefix = prefix_other[0];
			String other = prefix_other[1];
			String[] name_suffix = split(other, 40);
			name = name_suffix[0];
			suffix = name_suffix[1];
		}
		//1.7-
		if (this.text.length() <= 16) {
			prefix1_7 = "";
			name1_7 = this.text;
			suffix1_7 = "";
		} else {
			String[] prefix_other = split(this.text, 16);
			prefix1_7 = prefix_other[0];
			String other = prefix_other[1];
			String[] name_suffix = split(other, 16);
			name1_7 = name_suffix[0];
			suffix1_7 = name_suffix[1];
		}
	}
	
	@Override
	public void refresh(ITabPlayer refreshed, boolean force) {
		//nothing to refresh
	}

	@Override
	public void refreshUsedPlaceholders() {
		//no placeholders
	}

	@Override
	public void register(ITabPlayer p) {
		p.setProperty(teamName, text, null);
		if (p.getVersion().getMinorVersion() < 8) {
			PacketAPI.registerScoreboardScore(p, teamName, name1_7, prefix1_7, suffix1_7, ObjectiveName, lineID);
		} else {
			PacketAPI.registerScoreboardScore(p, teamName, name, prefix, suffix, ObjectiveName, lineID);
		}
	}

	@Override
	public void unregister(ITabPlayer p) {
		if (p.getProperty(teamName).get().length() > 0) {
			PacketAPI.removeScoreboardScore(p, p.getVersion().getMinorVersion() < 8 ? name1_7 : name, teamName);
		}
	}
}