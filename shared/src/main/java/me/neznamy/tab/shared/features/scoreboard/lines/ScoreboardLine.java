package me.neznamy.tab.shared.features.scoreboard.lines;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardImpl;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManager;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore.Action;

/**
 * Abstract class representing a line of scoreboard
 */
public abstract class ScoreboardLine implements Refreshable {

	//ID of this line
	protected int lineNumber;
	
	//scoreboard this line belongs to
	protected ScoreboardImpl parent;
	
	//list of used placeholders
	protected List<String> usedPlaceholders = new ArrayList<>();
	
	//scoreboard team name of player in this line
	protected String teamName;
	
	//forced player name start to make lines unique & sort them by names
	protected String playerName;
	
	/**
	 * Constructs new instance with given parameters
	 * @param parent - scoreboard this line belongs to
	 * @param lineNumber - ID of this line
	 */
	protected ScoreboardLine(ScoreboardImpl parent, int lineNumber) {
		this.parent = parent;
		this.lineNumber = lineNumber;
		teamName = "TAB-SB-TM-" + lineNumber;
		playerName = getPlayerName(lineNumber);
	}
	
	/**
	 * Registers this line to the player
	 * @param p - player to register line to
	 */
	public abstract void register(TabPlayer p);
	
	/**
	 * Unregisters this line to the player
	 * @param p - player to unregister line to
	 */
	public abstract void unregister(TabPlayer p);
	
	@Override
	public List<String> getUsedPlaceholders() {
		return usedPlaceholders;
	}
	
	/**
	 * Splits the text into 2 with given max length of first string
	 * @param string - string to split
	 * @param firstElementMaxLength - max length of first string
	 * @return array of 2 strings where second one might be empty
	 */
	protected String[] split(String string, int firstElementMaxLength) {
		if (string.length() <= firstElementMaxLength) return new String[] {string, ""};
		int splitIndex = firstElementMaxLength;
		if (string.charAt(splitIndex-1) == '\u00a7') splitIndex--;
		return new String[] {string.substring(0, splitIndex), string.substring(splitIndex, string.length())};
	}
	
	/**
	 * Returns forced name start of this player
	 * @return forced name start of this player
	 */
	protected String getPlayerName() {
		return playerName;
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.SCOREBOARD;
	}
	
	/**
	 * Builds forced name start based on line number
	 * @param lineNumber - ID of line
	 * @return forced name start
	 */
	protected static String getPlayerName(int lineNumber) {
		String id = String.valueOf(lineNumber);
		if (id.length() == 1) id = "0" + id;
		char c = '\u00a7';
		return c + String.valueOf(id.charAt(0)) + c + String.valueOf(id.charAt(1)) + c + "r";
	}
	
	/**
	 * Sends this line to player
	 * @param p - player to send line to
	 * @param team - team name of the line
	 * @param fakeplayer - player name
	 * @param prefix - prefix
	 * @param suffix - suffix
	 * @param value - number
	 */
	protected void addLine(TabPlayer p, String team, String fakeplayer, String prefix, String suffix, int value) {
		p.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, ScoreboardManager.OBJECTIVE_NAME, fakeplayer, value), TabFeature.SCOREBOARD);
		PacketAPI.registerScoreboardTeam(p, team, prefix, suffix, false, false, Arrays.asList(fakeplayer), null, TabFeature.SCOREBOARD);
	}
	
	/**
	 * Removes this line from player
	 * @param p - player to remove line from
	 * @param fakeplayer - player name
	 * @param teamName - team name
	 */
	protected void removeLine(TabPlayer p, String fakeplayer, String teamName) {
		p.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.REMOVE, ScoreboardManager.OBJECTIVE_NAME, fakeplayer, 0), TabFeature.SCOREBOARD);
		p.sendCustomPacket(new PacketPlayOutScoreboardTeam(teamName), TabFeature.SCOREBOARD);
	}
}