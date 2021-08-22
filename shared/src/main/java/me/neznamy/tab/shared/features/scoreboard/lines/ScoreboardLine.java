package me.neznamy.tab.shared.features.scoreboard.lines;

import java.util.Arrays;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.api.scoreboard.Line;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore.Action;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardImpl;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManagerImpl;

/**
 * Abstract class representing a line of scoreboard
 */
public abstract class ScoreboardLine extends TabFeature implements Line {

	protected final String packetDisplayName = "Scoreboard - Lines";
	
	//ID of this line
	protected int lineNumber;
	
	//text to display
	protected String text;
	
	//scoreboard this line belongs to
	protected ScoreboardImpl parent;
	
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
		super(parent.getFeatureName());
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

	/**
	 * Builds forced name start based on line number
	 * @param lineNumber - ID of line
	 * @return forced name start
	 */
	protected String getPlayerName(int lineNumber) {
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
	protected void addLine(TabPlayer p, String fakeplayer, String prefix, String suffix) {
		p.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, ScoreboardManagerImpl.OBJECTIVE_NAME, fakeplayer, getNumber(p)), packetDisplayName);
		if (p.getVersion().getMinorVersion() >= 8 && TAB.getInstance().getConfiguration().isUnregisterBeforeRegister()) {
			p.sendCustomPacket(new PacketPlayOutScoreboardTeam(teamName), packetDisplayName);
		}
		p.sendCustomPacket(new PacketPlayOutScoreboardTeam(teamName, prefix, suffix, "never", "never", Arrays.asList(fakeplayer), 0), packetDisplayName);
	}
	
	/**
	 * Removes this line from player
	 * @param p - player to remove line from
	 * @param fakeplayer - player name
	 * @param teamName - team name
	 */
	protected void removeLine(TabPlayer p, String fakeplayer) {
		p.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.REMOVE, ScoreboardManagerImpl.OBJECTIVE_NAME, fakeplayer, 0), packetDisplayName);
		p.sendCustomPacket(new PacketPlayOutScoreboardTeam(teamName), packetDisplayName);
	}
	
	@Override
	public String getText() {
		return text;
	}
	
	/**
	 * Returns number that should be displayed as score for specified player
	 * @param p - player to get number for
	 * @return number displayed
	 */
	public int getNumber(TabPlayer p) {
		if (parent.getManager().isUsingNumbers() || p.getVersion().getMinorVersion() < 8) {
			return parent.getLines().size() + 1 - lineNumber;
		} else {
			return parent.getManager().getStaticNumber();
		}
	}
}