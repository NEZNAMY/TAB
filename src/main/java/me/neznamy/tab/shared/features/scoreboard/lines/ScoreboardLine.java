package me.neznamy.tab.shared.features.scoreboard.lines;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore.Action;

/**
 * Abstract class representing a line of scoreboard
 */
public abstract class ScoreboardLine implements Refreshable {

	protected final String ObjectiveName = "TAB-Scoreboard";
	protected List<String> usedPlaceholders = new ArrayList<String>();
	protected String teamName;
	protected String playerName;
	
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
		if (string.charAt(splitIndex-1) == '\u00a7') splitIndex--;
		return new String[] {string.substring(0, splitIndex), string.substring(splitIndex, string.length())};
	}
	
	protected String getPlayerName() {
		return playerName;
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.SCOREBOARD;
	}
	
	protected static String getPlayerName(int lineNumber) {
		String id = lineNumber+"";
		if (id.length() == 1) id = "0" + id;
		char c = '\u00a7';
		return c + String.valueOf(id.charAt(0)) + c + String.valueOf(id.charAt(1)) + c + "r";
	}
	
	protected void addLine(TabPlayer p, String team, String fakeplayer, String prefix, String suffix, int value) {
		p.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, ObjectiveName, fakeplayer, value));
		PacketAPI.registerScoreboardTeam(p, team, prefix, suffix, false, false, Arrays.asList(fakeplayer), null);
	}
	
	protected void removeLine(TabPlayer p, String fakeplayer, String teamName) {
		p.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.REMOVE, ObjectiveName, fakeplayer, 0));
		p.sendCustomPacket(new PacketPlayOutScoreboardTeam(teamName).setTeamOptions(69));
	}
}