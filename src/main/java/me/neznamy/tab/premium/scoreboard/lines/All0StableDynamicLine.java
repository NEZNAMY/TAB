package me.neznamy.tab.premium.scoreboard.lines;

import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.premium.scoreboard.Scoreboard;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * A stable (anti-flickering) line with dynamic text (supports placeholders) with 0 everywhere
 * Limitations:
 *   1.5.x - 1.12.x: up to 32 characters (depending on color/magic codes)
 */
public class All0StableDynamicLine extends ScoreboardLine {

	private Scoreboard parent;
	private int lineID;
	private String text;
	
	public All0StableDynamicLine(Scoreboard parent, int lineID, String text) {
		super(lineID);
		this.parent = parent;
		this.lineID = lineID;
		this.text = text;
		refreshUsedPlaceholders();
	}
	
	@Override
	public void refresh(ITabPlayer refreshed, boolean force) {
		if (!parent.players.contains(refreshed)) return; //player has different scoreboard displayed
		List<String> prefixsuffix = replaceText(refreshed, force, false);
		if (prefixsuffix == null) return;
		refreshed.sendCustomPacket(PacketPlayOutScoreboardTeam.UPDATE_TEAM_INFO(teamName, prefixsuffix.get(0), prefixsuffix.get(1), "always", "always", 69));
	}

	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = Placeholders.getUsedPlaceholderIdentifiersRecursive(text);
	}

	@Override
	public void register(ITabPlayer p) {
		p.setProperty(teamName, text, null);
		List<String> prefixsuffix = replaceText(p, true, true);
		if (prefixsuffix == null) return;
		PacketAPI.registerScoreboardScore(p, teamName, getPlayerName(), prefixsuffix.get(0), prefixsuffix.get(1), ObjectiveName, parent.manager.staticNumber);
	}

	@Override
	public void unregister(ITabPlayer p) {
		if (parent.players.contains(p) && p.properties.get(teamName).get().length() > 0) {
			PacketAPI.removeScoreboardScore(p, getPlayerName(), teamName);
		}
	}
	
	private List<String> replaceText(ITabPlayer p, boolean force, boolean suppressToggle) {
		Property scoreproperty = p.properties.get(teamName);
		boolean emptyBefore = scoreproperty.get().length() == 0;
		if (!scoreproperty.update() && !force) return null;
		String replaced = scoreproperty.get();
		String prefix;
		String suffix;
		if (replaced.length() > 16 && p.getVersion().getMinorVersion() < 13) {
			prefix = replaced.substring(0, 16);
			suffix = replaced.substring(16, replaced.length());
			if (prefix.charAt(15) == Placeholders.colorChar) {
				prefix = prefix.substring(0, 15);
				suffix = Placeholders.colorChar + suffix;
			}
			String last = Placeholders.getLastColors(IChatBaseComponent.fromColoredText(prefix).toColoredText());
			suffix = last + suffix;
		} else {
			prefix = replaced;
			suffix = "";
		}
		if (replaced.length() > 0) {
			if (emptyBefore) {
				//was "", now it is not
				PacketAPI.registerScoreboardScore(p, teamName, getPlayerName(), prefix, suffix, ObjectiveName, lineID);
				return null;
			} else {
				return Arrays.asList(prefix, suffix);
			}
		} else {
			if (!suppressToggle) {
				//new string is "", but before it was not
				PacketAPI.removeScoreboardScore(p, getPlayerName(), teamName);
			}
			return null;
		}

	}
}