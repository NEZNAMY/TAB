package me.neznamy.tab.premium;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Lists;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.Placeholders;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;

public class Scoreboard {

	private String name;
	private String title;
	private boolean permissionRequired;
	private String childBoard;
	private HashMap<Integer, Score> scores = new HashMap<Integer, Score>();
	private ConcurrentHashMap<ITabPlayer, String> players = new ConcurrentHashMap<ITabPlayer, String>();
	private String objectiveName;

	public Scoreboard(String name, String title, List<String> lines, boolean permissionRequired, String childBoard) {
		this.name = name;
		this.title = title;
		this.permissionRequired = permissionRequired;
		this.childBoard = childBoard;
		objectiveName = Math.random()*1000000+"";
		if (objectiveName.length() > 16) objectiveName = objectiveName.substring(0, 16);
		for (int i=0; i<lines.size(); i++) {
			scores.put(i, new Score("TABSBTM"+i, getLineName(i),  lines.get(i), 0));
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
		if (!players.containsKey(p)) {
			String replacedTitle = Placeholders.replace(title, p);
			PacketAPI.registerScoreboardObjective(p, objectiveName, replacedTitle, 1, EnumScoreboardHealthDisplay.INTEGER);
			for (Score s : scores.values()) {
				s.register(p);
			}
			players.put(p, replacedTitle);
		}
	}
	public void unregister() {
		for (ITabPlayer all : players.keySet()) {
			unregister(all);
		}
		players.clear();
		scores.clear();
	}
	public void unregister(ITabPlayer p) {
		if (players.containsKey(p)) {
			PacketAPI.unregisterScoreboardObjective(p, objectiveName, players.get(p), EnumScoreboardHealthDisplay.INTEGER);
			for (Score s : scores.values()) {
				s.unregister(p);
			}
			players.remove(p);
		}
	}
	public void refresh() {
		for (ITabPlayer p : players.keySet()) {
			String replacedTitle = Placeholders.replace(title, p);
			if (replacedTitle.equals(players.get(p))) continue;
			PacketAPI.changeScoreboardObjectiveTitle(p, objectiveName, replacedTitle, EnumScoreboardHealthDisplay.INTEGER);
			players.put(p, replacedTitle);
		}
		for (Score s : scores.values()) {
			s.updatePrefixSuffix();
		}
	}
	public class Score{

		private String rawtext;
		private int score;
		private String ID;
		private String player;
		private HashMap<ITabPlayer, String> lastReplacedText = new HashMap<ITabPlayer, String>();

		public Score(String ID, String player, String rawtext, int score) {
			this.ID = ID;
			this.player = player;
			this.rawtext = rawtext;
			this.score = score;
		}
		private List<String> replaceText(ITabPlayer p) {
			try {
				String replaced = Placeholders.replace(rawtext, p);
				if (replaced.equals(lastReplacedText.get(p))) return null;
				lastReplacedText.put(p, replaced);
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
			} catch (Exception e) {
				e.printStackTrace();
				return Lists.newArrayList("", "");
			}
		}
		public void register(ITabPlayer p) {
			lastReplacedText.put(p, "");
			List<String> prefixsuffix = replaceText(p);
			if (prefixsuffix == null) prefixsuffix = Lists.newArrayList("", "");
			PacketAPI.registerScoreboardScore(p, ID, player, prefixsuffix.get(0), prefixsuffix.get(1), objectiveName, score);
		}
		private void unregister(ITabPlayer p) {
			if (players.containsKey(p)) {
				PacketAPI.removeScoreboardScore(p, player, ID);
				lastReplacedText.remove(p);
			}
		}
		public void unregister() {
			for (ITabPlayer p : players.keySet()) {
				PacketAPI.removeScoreboardScore(p, player, ID);
				lastReplacedText.remove(p);
			}
		}
		public void updatePrefixSuffix() {
			for (ITabPlayer p : players.keySet()) {
				List<String> prefixsuffix = replaceText(p);
				if (prefixsuffix == null) continue;
				PacketAPI.sendScoreboardTeamPacket(p, ID, prefixsuffix.get(0), prefixsuffix.get(1), false, false, null, 2, 69);
			}
		}
	}
}