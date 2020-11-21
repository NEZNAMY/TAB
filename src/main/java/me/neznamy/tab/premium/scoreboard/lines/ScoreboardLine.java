package me.neznamy.tab.premium.scoreboard.lines;

import java.util.ArrayList;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * Abstract class representing a line of scoreboard
 */
public abstract class ScoreboardLine implements Refreshable {

	protected static final String ObjectiveName = "TAB-Scoreboard";
	protected List<String> usedPlaceholders = new ArrayList<String>();
	protected String teamName;
	private String playerName;
	
	public ScoreboardLine(int lineNumber) {
		teamName = "TAB-SB-TM-"+lineNumber;
		playerName = getPlayerName(lineNumber);
	}
	
	public abstract void register(TabPlayer p);
	public abstract void unregister(TabPlayer p);
	
	@Override
	public List<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}
	
	protected String[] split(String string, int firstElementMaxLength) {
		if (string.length() <= firstElementMaxLength) return new String[] {string, ""};
		int splitIndex = firstElementMaxLength;
		if (string.charAt(splitIndex-1) == Placeholders.colorChar) splitIndex--;
		return new String[] {string.substring(0, splitIndex), string.substring(splitIndex, string.length())};
	}
	
	protected String getPlayerName() {
		return playerName;
	}
	
	/**
	 * Returns name of the feature displayed in /tab cpu
	 * @return name of the feature displayed in /tab cpu
	 */
	@Override
	public TabFeature getFeatureType() {
		return TabFeature.SCOREBOARD;
	}
	
	protected static String getPlayerName(int lineNumber) {
		String id = lineNumber+"";
		if (id.length() == 1) id = "0" + id;
		char c = Placeholders.colorChar;
		return c + String.valueOf(id.charAt(0)) + c + String.valueOf(id.charAt(1)) + c + "r";
	}
}