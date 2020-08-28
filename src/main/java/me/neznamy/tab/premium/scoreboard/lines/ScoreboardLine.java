package me.neznamy.tab.premium.scoreboard.lines;

import java.util.HashSet;
import java.util.Set;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * Abstract class representing a line of scoreboard
 */
public abstract class ScoreboardLine implements Refreshable {

	protected static final String ObjectiveName = "TAB-Scoreboard";
	protected Set<String> usedPlaceholders = new HashSet<String>();
	protected String teamName;
	private String playerName;
	
	public ScoreboardLine(int lineID) {
		teamName = "TAB-SB-TM-"+lineID;
		String id = 15-lineID+"";
		if (id.length() == 1) id = "0" + id;
		char c = Placeholders.colorChar;
		playerName = c + String.valueOf(id.charAt(0)) + c + String.valueOf(id.charAt(1)) + c + "r";
	}
	
	public abstract void register(ITabPlayer p);
	public abstract void unregister(ITabPlayer p);
	
	@Override
	public CPUFeature getRefreshCPU() {
		return CPUFeature.SCOREBOARD_LINES;
	}
	
	@Override
	public Set<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}
	
	protected String[] split(String string, int firstElementMaxLength) {
		if (string.length() <= firstElementMaxLength) return new String[] {string, ""};
		if (string.charAt(firstElementMaxLength-1) == Placeholders.colorChar) firstElementMaxLength--;
		return new String[] {string.substring(0, firstElementMaxLength), string.substring(firstElementMaxLength, string.length())};
	}
	
	protected String getPlayerName() {
		return playerName;
	}
}