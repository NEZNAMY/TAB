package me.neznamy.tab.shared.features.scoreboard.lines;

import java.util.Collections;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.api.scoreboard.Line;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardScore.Action;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardImpl;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardManagerImpl;

/**
 * Abstract class representing a line of scoreboard
 */
public abstract class ScoreboardLine extends TabFeature implements Line {

	//ID of this line
	protected final int lineNumber;
	
	//text to display
	protected String text;
	
	//scoreboard this line belongs to
	protected final ScoreboardImpl parent;
	
	//scoreboard team name of player in this line
	protected final String teamName;
	
	//forced player name start to make lines unique & sort them by names
	protected final String playerName;
	
	/**
	 * Constructs new instance with given parameters
	 * @param parent - scoreboard this line belongs to
	 * @param lineNumber - ID of this line
	 */
	protected ScoreboardLine(ScoreboardImpl parent, int lineNumber) {
		super(parent.getFeatureName(), "Updating scoreboard lines");
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
		if (string.charAt(splitIndex-1) == EnumChatFormat.COLOR_CHAR) splitIndex--;
		return new String[] {string.substring(0, splitIndex), string.substring(splitIndex)};
	}
	
	/**
	 * Returns forced name start of this player
	 * @return forced name start of this player
	 */
	public String getPlayerName() {
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
		return EnumChatFormat.COLOR_STRING + id.charAt(0) + EnumChatFormat.COLOR_STRING + id.charAt(1) + EnumChatFormat.COLOR_STRING + "r";
	}
	
	/**
	 * Sends this line to player
	 * @param p - player to send line to
	 * @param fakePlayer - player name
	 * @param prefix - prefix
	 * @param suffix - suffix
	 */
	protected void addLine(TabPlayer p, String fakePlayer, String prefix, String suffix) {
		p.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.CHANGE, ScoreboardManagerImpl.OBJECTIVE_NAME, fakePlayer, getNumber(p)), TabConstants.PacketCategory.SCOREBOARD_LINES);
		if (p.getVersion().getMinorVersion() >= 8 && TAB.getInstance().getConfiguration().isUnregisterBeforeRegister()) {
			p.sendCustomPacket(new PacketPlayOutScoreboardTeam(teamName), TabConstants.PacketCategory.SCOREBOARD_LINES);
		}
		p.sendCustomPacket(new PacketPlayOutScoreboardTeam(teamName, prefix, suffix, "never", "never", Collections.singletonList(fakePlayer), 0), TabConstants.PacketCategory.SCOREBOARD_LINES);
	}
	
	/**
	 * Removes this line from player
	 * @param p - player to remove line from
	 * @param fakePlayer - player name
	 */
	protected void removeLine(TabPlayer p, String fakePlayer) {
		p.sendCustomPacket(new PacketPlayOutScoreboardScore(Action.REMOVE, ScoreboardManagerImpl.OBJECTIVE_NAME, fakePlayer, 0), TabConstants.PacketCategory.SCOREBOARD_LINES);
		p.sendCustomPacket(new PacketPlayOutScoreboardTeam(teamName), TabConstants.PacketCategory.SCOREBOARD_LINES);
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
		if (parent.getManager().isUsingNumbers() || p.getVersion().getMinorVersion() < 8 || p.isBedrockPlayer()) {
			return parent.getLines().size() + 1 - lineNumber;
		} else {
			return parent.getManager().getStaticNumber();
		}
	}

	public String getTeamName() {
		return teamName;
	}

	/**
	 * Splits entered text into 3 parts - prefix, name and suffix respecting all limits.
	 * Returns the values as an array of 3 elements.
	 * @param	playerNameStart
	 * 			forced start of name field (used to secure unique names & line order)
	 * @param	text
	 * 			text to display
	 * @param	maxNameLength
	 * 			maximum length of name field, used values are 16 characters for <1.8 and 40 for 1.8+
	 * @return	Split text as an array of 3 elements
	 */
	protected String[] splitText(String playerNameStart, String text, int maxNameLength) {
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
				other = playerNameStart + EnumChatFormat.getLastColors(prefixValue) + other;
			}
			String[] nameSuffix = split(other, maxNameLength);
			nameValue = nameSuffix[0];
			suffixValue = nameSuffix[1];
		}
		return new String[]{prefixValue, nameValue, suffixValue};
	}
}