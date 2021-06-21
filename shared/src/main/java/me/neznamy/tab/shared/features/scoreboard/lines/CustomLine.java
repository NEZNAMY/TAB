package me.neznamy.tab.shared.features.scoreboard.lines;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.features.scoreboard.Scoreboard;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;

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
	public CustomLine(Scoreboard parent, int lineNumber, String prefix, String name, String suffix, int score) {
		super(parent, lineNumber);
		this.prefix = prefix;
		this.name = name;
		this.suffix = suffix;
		this.score = score;
		refreshUsedPlaceholders();
	}

	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (!parent.players.contains(refreshed)) return; //player has different scoreboard displayed
		String oldName = refreshed.getProperty(teamName + "-name").get();
		boolean prefix = refreshed.getProperty(teamName + "-prefix").update();
		boolean name = refreshed.getProperty(teamName + "-name").update();
		boolean suffix = refreshed.getProperty(teamName + "-suffix").update();
		if (prefix || name || suffix) {
			if (name) {
				//name changed as well
				removeLine(refreshed, oldName, teamName);
				addLine(refreshed, teamName, refreshed.getProperty(teamName + "-name").get(), 
						refreshed.getProperty(teamName + "-prefix").get(), refreshed.getProperty(teamName + "-suffix").get(), score);
			} else {
				//only prefix/suffix changed
				refreshed.sendCustomPacket(new PacketPlayOutScoreboardTeam(teamName, refreshed.getProperty(teamName + "-prefix").get(), 
						refreshed.getProperty(teamName + "-suffix").get(), "always", "always", 0), TabFeature.SCOREBOARD);
			}
		}
	}

	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = TAB.getInstance().getPlaceholderManager().getUsedPlaceholderIdentifiersRecursive(prefix, name, suffix);
	}

	@Override
	public void register(TabPlayer p) {
		p.setProperty(teamName + "-prefix", prefix);
		p.setProperty(teamName + "-name", name);
		p.setProperty(teamName + "-suffix", suffix);
		addLine(p, teamName, p.getProperty(teamName + "-name").get(), p.getProperty(teamName + "-prefix").get(),
				p.getProperty(teamName + "-suffix").get(), score);
	}

	@Override
	public void unregister(TabPlayer p) {
		if (parent.players.contains(p)) {
			removeLine(p, p.getProperty(teamName + "-name").get(), teamName);
		}
	}
}