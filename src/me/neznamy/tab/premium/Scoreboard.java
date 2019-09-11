package me.neznamy.tab.premium;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.Placeholders;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;

public class Scoreboard {

	private String name;
	private String title;
	private boolean permissionRequired;
	private String childBoard;
	private List<Score> scores = new ArrayList<Score>();
	private List<ITabPlayer> players = new ArrayList<ITabPlayer>();
	private String objectiveName;

	public Scoreboard(String name, String title, List<String> lines, boolean permissionRequired, String childBoard) {
		this.name = name;
		this.title = title;
		this.permissionRequired = permissionRequired;
		this.childBoard = childBoard;
		objectiveName = Math.random()*1000000+"";
		if (objectiveName.length() > 16) objectiveName = objectiveName.substring(0, 16);
		for (int i=0; i<lines.size(); i++) {
			scores.add(new Score(lines.size()-i, "TABSBTM"+i, getLineName(i),  lines.get(i)));
		}
	}
	public String getName() {
		return name;
	}
	public boolean isPermissionRequired() {
		return permissionRequired;
	}
	public String getChildScoreboard() {
		return childBoard;
	}
	public String getLineName(int i) {
		String id = i+"";
		if (id.length() == 1) id = "0" + id;
		return "§" + id.toCharArray()[0] + "§" + id.toCharArray()[1] + "§r";
	}
	public void register(ITabPlayer p) {
		if (!players.contains(p)) {
			p.setProperty("scoreboard-title", title);
			String replacedTitle = p.properties.get("scoreboard-title").get();
			PacketAPI.registerScoreboardObjective(p, objectiveName, replacedTitle, 1, EnumScoreboardHealthDisplay.INTEGER);
			for (Score s : scores) {
				s.register(p);
			}
			players.add(p);
		}
	}
	public void unregister() {
		for (ITabPlayer all : players.toArray(new ITabPlayer[0])) {
			unregister(all);
		}
		players.clear();
		scores.clear();
	}
	public void unregister(ITabPlayer p) {
		if (players.contains(p)) {
			PacketAPI.unregisterScoreboardObjective(p, objectiveName, p.properties.get("scoreboard-title").get(), EnumScoreboardHealthDisplay.INTEGER);
			for (Score s : scores) {
				s.unregister(p);
			}
			players.remove(p);
		}
	}
	public void refresh() {
		for (ITabPlayer p : players.toArray(new ITabPlayer[0])) {
			Property title = p.properties.get("scoreboard-title");
			if (title.isUpdateNeeded()) {
				String replacedTitle = title.get();
				PacketAPI.changeScoreboardObjectiveTitle(p, objectiveName, replacedTitle, EnumScoreboardHealthDisplay.INTEGER);
			}
		}
		for (Score s : scores) {
			s.updatePrefixSuffix();
		}
	}
	public class Score{

		private int score;
		private String rawtext;
		private String ID;
		private String player;

		public Score(int score, String ID, String player, String rawtext) {
			this.score = score;
			this.ID = ID;
			this.player = player;
			this.rawtext = rawtext;
		}
		private List<String> replaceText(ITabPlayer p, boolean force) {
			Property scoreproperty = p.properties.get("sb-"+ID);
			if (scoreproperty.isUpdateNeeded() || force) {
				String replaced = scoreproperty.get();
				if (replaced.length() > 16) {
					String prefix = replaced.substring(0, 16);
					String suffix = replaced.substring(16, replaced.length());
					if (prefix.toCharArray()[15] == '§') {
						prefix = prefix.substring(0, 15);
						suffix = "§" + suffix;
					}
					suffix = Placeholders.getLastColors(prefix) + suffix;
					return Lists.newArrayList(prefix, suffix);
				} else {
					return Lists.newArrayList(replaced, "");
				}
			} else return null; //update not needed
		}
		public void register(ITabPlayer p) {
			p.setProperty("sb-"+ID, rawtext);
			List<String> prefixsuffix = replaceText(p, true);
			if (prefixsuffix == null) prefixsuffix = Lists.newArrayList("", "");
			int score = (p.getVersion().getNumber() < ProtocolVersion.v1_8.getNumber() || ScoreboardManager.useNumbers) ? this.score : 0;
			PacketAPI.registerScoreboardScore(p, ID, player, prefixsuffix.get(0), prefixsuffix.get(1), objectiveName, score);
		}
		private void unregister(ITabPlayer p) {
			if (players.contains(p)) {
				PacketAPI.removeScoreboardScore(p, player, ID);
			}
		}
		public void unregister() {
			for (ITabPlayer p : players) {
				PacketAPI.removeScoreboardScore(p, player, ID);
			}
		}
		public void updatePrefixSuffix() {
			for (ITabPlayer p : players.toArray(new ITabPlayer[0])) {
				List<String> prefixsuffix = replaceText(p, false);
				if (prefixsuffix == null) continue;
				PacketAPI.sendScoreboardTeamPacket(p, ID, prefixsuffix.get(0), prefixsuffix.get(1), false, false, null, 2, 69);
			}
		}
	}
}