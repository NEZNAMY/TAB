package me.neznamy.tab.premium.scoreboard.lines;

import me.neznamy.tab.premium.scoreboard.Scoreboard;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * Fully customizable line
 * Not for public use
 */
public class CustomLine extends ScoreboardLine {

	private Scoreboard parent;
	private String prefix;
	private String name;
	private String suffix;
	private int score;
	
	public CustomLine(Scoreboard parent, int lineID, String prefix, String name, String suffix, int score) {
		super(lineID);
		this.parent = parent;
		this.prefix = prefix;
		this.name = name;
		this.suffix = suffix;
		this.score = score;
		refreshUsedPlaceholders();
	}

	@Override
	public void refresh(ITabPlayer refreshed, boolean force) {
		String oldName = refreshed.properties.get(teamName + "-name").get();
		boolean prefix = refreshed.properties.get(teamName + "-prefix").update();
		boolean name = refreshed.properties.get(teamName + "-name").update();
		boolean suffix = refreshed.properties.get(teamName + "-suffix").update();
		if (prefix || name || suffix) {
			if (name) {
				//name changed as well
				PacketAPI.removeScoreboardScore(refreshed, oldName, teamName);
				PacketAPI.registerScoreboardScore(refreshed, teamName, refreshed.properties.get(teamName + "-name").get(), 
						refreshed.properties.get(teamName + "-prefix").get(), refreshed.properties.get(teamName + "-suffix").get(), ObjectiveName, score);
			} else {
				//only prefix/suffix changed
				refreshed.sendCustomPacket(PacketPlayOutScoreboardTeam.UPDATE_TEAM_INFO(teamName, refreshed.properties.get(teamName + "-prefix").get(), 
						refreshed.properties.get(teamName + "-suffix").get(), "always", "always", 69));
			}
		}
	}

	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = Placeholders.getUsedPlaceholderIdentifiersRecursive(prefix, name, suffix);
	}

	@Override
	public void register(ITabPlayer p) {
		p.setProperty(teamName + "-prefix", prefix, null);
		p.setProperty(teamName + "-name", name, null);
		p.setProperty(teamName + "-suffix", suffix, null);
		PacketAPI.registerScoreboardScore(p, teamName, p.properties.get(teamName + "-name").get(), p.properties.get(teamName + "-prefix").get(),
				p.properties.get(teamName + "-suffix").get(), ObjectiveName, score);
	}

	@Override
	public void unregister(ITabPlayer p) {
		if (parent.players.contains(p)) {
			PacketAPI.removeScoreboardScore(p, p.properties.get(teamName + "-name").get(), teamName);
		}
	}
}