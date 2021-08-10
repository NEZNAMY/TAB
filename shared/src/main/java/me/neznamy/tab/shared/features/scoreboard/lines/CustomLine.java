package me.neznamy.tab.shared.features.scoreboard.lines;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardTeam;
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
	private int score;
	
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
		String oldName = refreshed.getProperty(teamName + "-name").get();
		boolean prefixUpdate = refreshed.getProperty(teamName + "-prefix").update();
		boolean nameUpdate = refreshed.getProperty(teamName + "-name").update();
		boolean suffixUpdate = refreshed.getProperty(teamName + "-suffix").update();
		if (prefixUpdate || nameUpdate || suffixUpdate) {
			if (nameUpdate) {
				//name changed as well
				removeLine(refreshed, oldName);
				addLine(refreshed, refreshed.getProperty(teamName + "-name").get(), 
						refreshed.getProperty(teamName + "-prefix").get(), refreshed.getProperty(teamName + "-suffix").get());
			} else {
				//only prefix/suffix changed
				refreshed.sendCustomPacket(new PacketPlayOutScoreboardTeam(teamName, refreshed.getProperty(teamName + "-prefix").get(), 
						refreshed.getProperty(teamName + "-suffix").get(), "always", "always", 0), this);
			}
		}
	}

	@Override
	public void register(TabPlayer p) {
		p.setProperty(this, teamName + "-prefix", prefix);
		p.setProperty(this, teamName + "-name", name);
		p.setProperty(this, teamName + "-suffix", suffix);
		addLine(p, p.getProperty(teamName + "-name").get(), p.getProperty(teamName + "-prefix").get(),
				p.getProperty(teamName + "-suffix").get());
	}

	@Override
	public void unregister(TabPlayer p) {
		if (parent.getPlayers().contains(p)) {
			removeLine(p, p.getProperty(teamName + "-name").get());
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