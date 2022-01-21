package me.neznamy.tab.shared.features.scoreboard.lines;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardImpl;

/**
 * Line using all 3 values - prefix, name and suffix. Line may flicker when placeholder changes value.
 */
public class LongLine extends ScoreboardLine {

	private final String textProperty;
	private final String nameProperty;
	/**
	 * Constructs new instance with given parameters
	 * @param parent - scoreboard this line belongs to
	 * @param lineNumber - ID of this line
	 * @param text - line text
	 */
	public LongLine(ScoreboardImpl parent, int lineNumber, String text) {
		super(parent, lineNumber);
		this.text = text;
		nameProperty = TabConstants.Property.scoreboardName(parent.getName(), lineNumber);
		textProperty = parent.getName() + "-" + teamName;
	}

	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (!parent.getPlayers().contains(refreshed)) return; //player has different scoreboard displayed
		if (refreshed.getProperty(textProperty).update()) {
			if (refreshed.getVersion().getMinorVersion() >= 13) {
				refreshed.sendCustomPacket(new PacketPlayOutScoreboardTeam(teamName, refreshed.getProperty(textProperty).get(),
						"", "always", "always", 0), TabConstants.PacketCategory.SCOREBOARD_LINES);
			} else {
				removeLine(refreshed, refreshed.getProperty(nameProperty).get());
				String[] values = splitText(getPlayerName(lineNumber), refreshed.getProperty(textProperty).get(), refreshed.getVersion().getMinorVersion() >= 8 ? 40 : 16);
				addLine(refreshed, values[1], values[0], values[2]);
				refreshed.setProperty(this, nameProperty, values[1]);
			}
		}
	}

	@Override
	public void register(TabPlayer p) {
		p.setProperty(this, textProperty, text);
		String value = p.getProperty(textProperty).get();
		if (p.getVersion().getMinorVersion() >= 13) {
			addLine(p, playerName, text, "");
			p.setProperty(this, nameProperty, playerName);
		} else {
			String[] values = splitText(playerName, value, p.getVersion().getMinorVersion() >= 8 ? 40 : 16);
			addLine(p, values[1], values[0], values[2]);
			p.setProperty(this, nameProperty, values[1]);
		}
	}

	@Override
	public void unregister(TabPlayer p) {
		if (parent.getPlayers().contains(p)) {
			removeLine(p, p.getProperty(nameProperty).get());
		}
	}

	@Override
	public void setText(String text) {
		this.text = text;
		for (TabPlayer p : parent.getPlayers()) {
			p.setProperty(this, textProperty, text);
			refresh(p, true);
		}
	}
}