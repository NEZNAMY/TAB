package me.neznamy.tab.premium.scoreboard.lines;

import me.neznamy.tab.premium.scoreboard.Scoreboard;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * A line with static text (no placeholders) with 0 everywhere
 * Limitations:
 *   1.5.x - 1.7.x: up to 42 characters (depending on color/magic codes)
 *   1.8.x - 1.12.x: up to 66 characters (depending on color/magic codes)
 */
public class All0StaticLine extends ScoreboardLine {

	private Scoreboard parent;
	private String text;
	
	private String prefix1_7;
	private String name1_7;
	private String suffix1_7;
	
	private String prefix;
	private String name;
	private String suffix;
	
	public All0StaticLine(Scoreboard parent, int lineID, String text) {
		super(lineID);
		this.parent = parent;
		this.text = IChatBaseComponent.fromColoredText(text).toColoredText(); //colorizing + translating RGB codes into legacy
		//1.8+
		if (this.text.length() <= 34) { //6 forced characters &x&x&r
			prefix = "";
			name = getPlayerName() + this.text;
			suffix = "";
		} else {
			String[] prefix_other = split(this.text, 16);
			prefix = prefix_other[0];
			String other = prefix_other[1];
			String lastColors = Placeholders.getLastColors(prefix);
			other = getPlayerName() + lastColors + other;
			String[] name_suffix = split(other, 40);
			name = name_suffix[0];
			suffix = name_suffix[1];
		}
		if (this.text.length() <= 10) { //6 forced characters &x&x&r
			prefix1_7= "";
			name1_7 = getPlayerName() + this.text;
			suffix1_7 = "";
		} else {
			String[] prefix_other = split(this.text, 16);
			prefix1_7 = prefix_other[0];
			String other = prefix_other[1];
			String lastColors = Placeholders.getLastColors(prefix1_7);
			other = getPlayerName() + lastColors + other;
			String[] name_suffix = split(other, 40);
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
			PacketAPI.registerScoreboardScore(p, teamName, name1_7, prefix1_7, suffix1_7, ObjectiveName, parent.manager.staticNumber);
		} else {
			PacketAPI.registerScoreboardScore(p, teamName, name, prefix, suffix, ObjectiveName, parent.manager.staticNumber);
		}
	}

	@Override
	public void unregister(ITabPlayer p) {
		if (p.properties.get(teamName).get().length() > 0) {
			PacketAPI.removeScoreboardScore(p, p.getVersion().getMinorVersion() < 8 ? name1_7 : name, teamName);
		}
	}
}