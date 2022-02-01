package me.neznamy.tab.shared.features.scoreboard.lines;

import me.neznamy.tab.api.Property;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.rgb.RGBUtils;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardImpl;

/**
 * Line of text with placeholder support
 * Limitations:
 *   1.5.x - 1.12.x: 28 - 32 characters (depending on used magic codes)
 *   1.13+: unlimited
 */
public class StableDynamicLine extends ScoreboardLine {
	
	private static final String[] EMPTY_ARRAY = new String[0];

	/**
	 * Constructs new instance with given parameters
	 * @param parent - scoreboard this line belongs to
	 * @param lineNumber - ID of this line
	 * @param text - text to display
	 */
	public StableDynamicLine(ScoreboardImpl parent, int lineNumber, String text) {
		super(parent, lineNumber);
		this.text = text;
	}

	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (!parent.getPlayers().contains(refreshed)) return; //player has different scoreboard displayed
		String[] prefixSuffix = replaceText(refreshed, force, false);
		if (prefixSuffix.length == 0) return;
		refreshed.sendCustomPacket(new PacketPlayOutScoreboardTeam(teamName, prefixSuffix[0], prefixSuffix[1], "always", "always", 0), TabConstants.PacketCategory.SCOREBOARD_LINES);
	}

	@Override
	public void register(TabPlayer p) {
		p.setProperty(this, parent.getName() + "-" + teamName, text);
		String[] prefixSuffix = replaceText(p, true, true);
		if (prefixSuffix.length == 0) return;
		addLine(p, getPlayerName(), prefixSuffix[0], prefixSuffix[1]);
	}

	@Override
	public void unregister(TabPlayer p) {
		if (parent.getPlayers().contains(p) && p.getProperty(parent.getName() + "-" + teamName).get().length() > 0) {
			removeLine(p, getPlayerName());
		}
	}

	/**
	 * Applies all placeholders and splits the result into prefix/suffix based on client version
	 * or hides the line entirely if result is empty (and shows back once it's not)
	 * @param p - player to replace text for
	 * @param force - if action should be done despite update seemingly not needed
	 * @param suppressToggle - if line should NOT be removed despite being empty
	 * @return list of 2 elements for prefix/suffix
	 */
	private String[] replaceText(TabPlayer p, boolean force, boolean suppressToggle) {
		Property scoreProperty = p.getProperty(parent.getName() + "-" + teamName);
		if (scoreProperty == null) return EMPTY_ARRAY; //not actually loaded yet (force refresh called from placeholder manager register method)
		boolean emptyBefore = scoreProperty.get().length() == 0;
		if (!scoreProperty.update() && !force) return EMPTY_ARRAY;
		String replaced = scoreProperty.get();
		if (p.getVersion().getMinorVersion() < 16) {
			replaced = RGBUtils.getInstance().convertRGBtoLegacy(replaced); //converting RGB to legacy here to avoid splitting in the middle of RGB code
		}
		String[] split = split(p, replaced);
		if (replaced.length() > 0) {
			if (emptyBefore) {
				//was "", now it is not
				addLine(p, getPlayerName(), split[0], split[1]);
				parent.recalculateScores(p);
				return EMPTY_ARRAY;
			} else {
				return split;
			}
		} else {
			if (!suppressToggle) {
				//new string is "", but before it was not
				removeLine(p, getPlayerName());
				parent.recalculateScores(p);
			}
			return EMPTY_ARRAY;
		}
	}
	
	/**
	 * Splits text into 2 values (prefix/suffix) based on client version and text itself
	 * @param p - player to split text for
	 * @param text - text to split
	 * @return array of 2 elements for prefix and suffix
	 */
	private String[] split(TabPlayer p, String text) {
		int charLimit = 16;
		if (!TAB.getInstance().getPlatform().isProxy() && 
			TAB.getInstance().getServerVersion().getMinorVersion() >= 13 && 
			p.getVersion().getMinorVersion() < 13) {
			//ProtocolSupport bug
			String lastColors = EnumChatFormat.getLastColors(text.substring(0, Math.min(16, text.length())));
			charLimit -= lastColors.length() == 0 ? 2 : lastColors.length();
		}
		if (text.length() > charLimit && p.getVersion().getMinorVersion() < 13) {
			StringBuilder prefix = new StringBuilder(text);
			StringBuilder suffix = new StringBuilder(text);
			prefix.setLength(charLimit);
			suffix.delete(0, charLimit);
			if (prefix.charAt(charLimit-1) == EnumChatFormat.COLOR_CHAR) {
				prefix.setLength(prefix.length()-1);
				suffix.insert(0, EnumChatFormat.COLOR_CHAR);
			}
			String prefixString = prefix.toString();
			suffix.insert(0, EnumChatFormat.getLastColors(RGBUtils.getInstance().convertRGBtoLegacy(prefixString)));
			return new String[] {prefixString, suffix.toString()};
		} else {
			return new String[] {text, ""};
		}
	}

	@Override
	public void setText(String text) {
		this.text = text;
		for (TabPlayer p : parent.getPlayers()) {
			p.setProperty(this, teamName, text);
			refresh(p, true);
		}
	}
}