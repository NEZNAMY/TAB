package me.neznamy.tab.premium.scoreboard.lines;

import java.util.Arrays;
import java.util.List;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.premium.scoreboard.Scoreboard;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.shared.placeholders.Placeholders;

public abstract class StableDynamicLine extends ScoreboardLine {

	protected Scoreboard parent;
	protected int lineNumber;
	protected String text;

	public StableDynamicLine(Scoreboard parent, int lineNumber, String text) {
		super(lineNumber);
		this.parent = parent;
		this.lineNumber = lineNumber;
		this.text = text;
		refreshUsedPlaceholders();
	}

	@Override
	public void refreshUsedPlaceholders() {
		usedPlaceholders = Placeholders.getUsedPlaceholderIdentifiersRecursive(text);
	}

	@Override
	public void refresh(TabPlayer refreshed, boolean force) {
		if (!parent.players.contains(refreshed)) return; //player has different scoreboard displayed
		List<String> prefixsuffix = replaceText(refreshed, force, false);
		if (prefixsuffix == null) return;
		refreshed.sendCustomPacket(PacketPlayOutScoreboardTeam.UPDATE_TEAM_INFO(teamName, prefixsuffix.get(0), prefixsuffix.get(1), "always", "always", 69));
	}

	@Override
	public void register(TabPlayer p) {
		p.setProperty(teamName, text);
		List<String> prefixsuffix = replaceText(p, true, true);
		if (prefixsuffix == null) return;
		PacketAPI.registerScoreboardScore(p, teamName, getPlayerName(), prefixsuffix.get(0), prefixsuffix.get(1), ObjectiveName, getScoreFor(p));
	}

	@Override
	public void unregister(TabPlayer p) {
		if (parent.players.contains(p) && p.getProperty(teamName).get().length() > 0) {
			PacketAPI.removeScoreboardScore(p, getPlayerName(), teamName);
		}
	}

	protected List<String> replaceText(TabPlayer p, boolean force, boolean suppressToggle) {
		Property scoreproperty = p.getProperty(teamName);
		boolean emptyBefore = scoreproperty.get().length() == 0;
		if (!scoreproperty.update() && !force) return null;
		String replaced = scoreproperty.get();
		String prefix;
		String suffix;
		//ProtocolSupport limiting length to 14 for <1.13 on 1.13+ server
		int charLimit = Shared.platform.getSeparatorType().equals("world") && 
				ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13 && 
				p.getVersion().getMinorVersion() < 13 ? 14 : 16;
				
		if (replaced.length() > charLimit && p.getVersion().getMinorVersion() < 13) {
			replaced = IChatBaseComponent.fromColoredText(replaced).toLegacyText(); //converting RGB to legacy here to avoid splitting in the middle of RGB code
			int prefixLength = Math.min(charLimit, replaced.length()); //in case of short RGB text and it changed length
			prefix = replaced.substring(0, prefixLength);
			suffix = replaced.substring(prefixLength, replaced.length());
			if (prefix.charAt(charLimit-1) == Placeholders.colorChar) {
				prefix = prefix.substring(0, charLimit-1);
				suffix = Placeholders.colorChar + suffix;
			}
			String last = Placeholders.getLastColors(IChatBaseComponent.fromColoredText(prefix).toLegacyText());
			suffix = last + suffix;
		} else {
			prefix = replaced;
			suffix = "";
		}
		if (replaced.length() > 0) {
			if (emptyBefore) {
				//was "", now it is not
				PacketAPI.registerScoreboardScore(p, teamName, getPlayerName(), prefix, suffix, ObjectiveName, getScoreFor(p));
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

	public abstract int getScoreFor(TabPlayer p);
}