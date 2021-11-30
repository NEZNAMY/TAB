package me.neznamy.tab.shared.features.scoreboard.lines;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardImpl;

/**
 * Fully customizable line, to use this class user must follow the following formula in a line
 * "Custom|prefix|name|suffix|number" where even name supports placeholders, however has a chance to flicker on refresh
 * Not for public use
 */
public class CustomLine extends ScoreboardLine {

	//configured prefix
	private String prefix;
	
	//configured name
	private String name;
	
	//configured suffix
	private String suffix;
	
	//configured score
	private final int score;
	
	/**
	 * Constructs new instance with given parameters
	 * @param parent - scoreboard this line belongs to
	 * @param lineNumber - ID of this line
	 * @param prefix - prefix
	 * @param name - name
	 * @param suffix - suffix
	 * @param score - score
	 */
	public CustomLine(ScoreboardImpl parent, int lineNumber, String prefix, String name, String suffix, int score) {
		super(parent, lineNumber);
		this.prefix = prefix;
		this.name = name;
		this.suffix = suffix;
		this.score = score;
	}

	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (!parent.getPlayers().contains(refreshed)) return; //player has different scoreboard displayed
		String oldName = refreshed.getProperty(TabConstants.Property.scoreboardName(parent.getName(), lineNumber)).get();
		boolean prefixUpdate = refreshed.getProperty(TabConstants.Property.scoreboardPrefix(parent.getName(), lineNumber)).update();
		boolean nameUpdate = refreshed.getProperty(TabConstants.Property.scoreboardName(parent.getName(), lineNumber)).update();
		boolean suffixUpdate = refreshed.getProperty(TabConstants.Property.scoreboardSuffix(parent.getName(), lineNumber)).update();
		if (prefixUpdate || nameUpdate || suffixUpdate) {
			if (nameUpdate) {
				//name changed as well
				removeLine(refreshed, oldName);
				addLine(refreshed, refreshed.getProperty(TabConstants.Property.scoreboardName(parent.getName(), lineNumber)).get(), 
						refreshed.getProperty(TabConstants.Property.scoreboardPrefix(parent.getName(), lineNumber)).get(), refreshed.getProperty(TabConstants.Property.scoreboardSuffix(parent.getName(), lineNumber)).get());
			} else {
				//only prefix/suffix changed
				refreshed.sendCustomPacket(new PacketPlayOutScoreboardTeam(teamName, refreshed.getProperty(TabConstants.Property.scoreboardPrefix(parent.getName(), lineNumber)).get(), 
						refreshed.getProperty(TabConstants.Property.scoreboardSuffix(parent.getName(), lineNumber)).get(), "always", "always", 0), TabConstants.PacketCategory.SCOREBOARD_LINES);
			}
		}
	}

	@Override
	public void register(TabPlayer p) {
		p.setProperty(this, TabConstants.Property.scoreboardPrefix(parent.getName(), lineNumber), prefix);
		p.setProperty(this, TabConstants.Property.scoreboardName(parent.getName(), lineNumber), name);
		p.setProperty(this, TabConstants.Property.scoreboardSuffix(parent.getName(), lineNumber), suffix);
		addLine(p, p.getProperty(TabConstants.Property.scoreboardName(parent.getName(), lineNumber)).get(), p.getProperty(TabConstants.Property.scoreboardPrefix(parent.getName(), lineNumber)).get(),
				p.getProperty(TabConstants.Property.scoreboardSuffix(parent.getName(), lineNumber)).get());
	}

	@Override
	public void unregister(TabPlayer p) {
		if (parent.getPlayers().contains(p)) {
			removeLine(p, p.getProperty(TabConstants.Property.scoreboardName(parent.getName(), lineNumber)).get());
		}
	}

	@Override
	public void setText(String text) {
		super.text = text;
		String[] elements = text.split("\\|");
		prefix = elements[0];
		name = elements[1];
		suffix = elements[2];
	}
	
	@Override
	public int getNumber(TabPlayer p) {
		return score;
	}
}