package me.neznamy.tab.premium;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class Scoreboard implements me.neznamy.tab.api.Scoreboard{

	private static final String ObjectiveName = "TAB-Scoreboard";
	private static final int DisplaySlot = 1;
	
	private ScoreboardManager manager;
	private String name;
	private String title;
	private String displayCondition;
	private String childBoard;
	private List<Score> scores = new ArrayList<Score>();
	private List<ITabPlayer> players = new ArrayList<ITabPlayer>();
	private List<Placeholder> conditionPlaceholders = new ArrayList<Placeholder>();

	public Scoreboard(String name, String title, List<String> lines, String displayCondition, String childBoard) {
		this(name, title, lines);
		this.displayCondition = displayCondition;
		this.childBoard = childBoard;
	}
	public Scoreboard(String name, String title, List<String> lines) {
		this.manager = (ScoreboardManager) Shared.features.get("scoreboard");
		this.name = name;
		this.title = title;
		conditionPlaceholders = Placeholders.detectPlaceholders(displayCondition);
		for (int i=0; i<lines.size(); i++) {
			scores.add(new Score(lines.size()-i, "TAB-SB-TM-"+i, getLineName(i),  lines.get(i)));
		}
	}
	public String getName() {
		return name;
	}
	public boolean isConditionMet(ITabPlayer p) {
		if (displayCondition == null) return true;
		for (String condition : displayCondition.split(";")) {
			if (condition.startsWith("permission:")) {
				String permission = condition.split(":")[1];
				if (!p.hasPermission(permission)) return false;
			}
			if (condition.contains("%")) {
				if (condition.contains("=")) {
					String leftSide = condition.split("=")[0];
					String rightSide = condition.split("=")[1];
					for (Placeholder pl : conditionPlaceholders) {
						leftSide = pl.set(leftSide, p);
					}
					if (!leftSide.equals(rightSide)) return false;
				} else if (condition.contains("<")) {
					String leftSide = condition.split("<")[0];
					double rightSide = Shared.errorManager.parseDouble(condition.split("<")[1], 0, "Scoreboard condition with \"<\" - right side");
					for (Placeholder pl : conditionPlaceholders) {
						leftSide = pl.set(leftSide, p);
					}
					double numericValueLeftSide = Shared.errorManager.parseDouble(leftSide, 0, "Scoreboard condition with \"<\" - left side");
					if (numericValueLeftSide >= rightSide) return false;
				} else if (condition.contains(">")) {
					String leftSide = condition.split(">")[0];
					double rightSide = Shared.errorManager.parseDouble(condition.split(">")[1], 0, "Scoreboard condition with \">\" - right side");
					for (Placeholder pl : conditionPlaceholders) {
						leftSide = pl.set(leftSide, p);
					}
					double numericValueLeftSide = Shared.errorManager.parseDouble(leftSide, 0, "Scoreboard condition with \">\" - left side");
					if (numericValueLeftSide <= rightSide) return false;
				}
			}
		}
		return true;
	}
	public String getChildScoreboard() {
		return childBoard;
	}
	public String getLineName(int i) {
		String id = i+"";
		if (id.length() == 1) id = "0" + id;
		return Placeholders.colorChar + String.valueOf(id.toCharArray()[0]) + Placeholders.colorChar + String.valueOf(id.toCharArray()[1]) + Placeholders.colorChar + "r";
	}
	public List<ITabPlayer> getRegisteredUsers(){
		return players;
	}
	public void register(ITabPlayer p) {
		if (!players.contains(p)) {
			p.setProperty("scoreboard-title", title, null);
			String replacedTitle = p.properties.get("scoreboard-title").get();
			PacketAPI.registerScoreboardObjective(p, ObjectiveName, replacedTitle, DisplaySlot, EnumScoreboardHealthDisplay.INTEGER);
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
			PacketAPI.unregisterScoreboardObjective(p, ObjectiveName);
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
				PacketAPI.changeScoreboardObjectiveTitle(p, ObjectiveName, title.get(), EnumScoreboardHealthDisplay.INTEGER);
			}
		}
		for (Score s : scores.toArray(new Score[0])) {
			s.updatePrefixSuffix();
		}
	}
	
	//implementing interface
	public void sendTo(UUID player) {
		ITabPlayer p = Shared.getPlayer(player);
		if  (p.getActiveScoreboard() != null) p.getActiveScoreboard().unregister(p);
		p.setActiveScoreboard(this);
		register(p);
	}
	
	public void removeFrom(UUID player) {
		ITabPlayer p = Shared.getPlayer(player);
		p.setActiveScoreboard(null);
		unregister(p);
	}
	
	
	
	
	
	public class Score{

		private int score;
		private String rawtext;
		private String teamname;
		private String player;

		public Score(int score, String teamname, String player, String rawtext) {
			this.score = score;
			this.teamname = teamname;
			this.player = player;
			this.rawtext = rawtext;
		}
		private List<String> replaceText(ITabPlayer p, boolean force, boolean suppressToggle) {
			Property scoreproperty = p.properties.get("sb-"+teamname);
			boolean emptyBefore = scoreproperty.get().length() == 0;
			if (scoreproperty.isUpdateNeeded() || force) {
				String replaced = scoreproperty.get();
				String prefix;
				String suffix;
				if (replaced.length() > 16) {
					prefix = replaced.substring(0, 16);
					suffix = replaced.substring(16, replaced.length());
					if (prefix.toCharArray()[15] == Placeholders.colorChar) {
						prefix = prefix.substring(0, 15);
						suffix = Placeholders.colorChar + suffix;
					}
					suffix = Placeholders.getLastColors(prefix) + suffix;
				} else {
					prefix = replaced;
					suffix = "";
				}
				if (replaced.length() > 0) {
					if (emptyBefore) {
						//was "", now it is not
						int score = (p.getVersion().getMinorVersion() < 8 || manager.useNumbers) ? this.score : 0;
						PacketAPI.registerScoreboardScore(p, teamname, player, prefix, suffix, ObjectiveName, score);
						return null;
					} else {
						return Arrays.asList(prefix, suffix);
					}
				} else {
					if (!suppressToggle) {
						//new string is "", but before it was not
						PacketAPI.removeScoreboardScore(p, player, teamname);
					}
					return null;
				}
			} else return null; //update not needed
		}
		public void register(ITabPlayer p) {
			p.setProperty("sb-"+teamname, rawtext, null);
			List<String> prefixsuffix = replaceText(p, true, true);
			if (prefixsuffix == null) return;
			int score = (p.getVersion().getMinorVersion() < 8 || manager.useNumbers) ? this.score : 0;
			PacketAPI.registerScoreboardScore(p, teamname, player, prefixsuffix.get(0), prefixsuffix.get(1), ObjectiveName, score);
		}
		private void unregister(ITabPlayer p) {
			if (players.contains(p)) {
				if (p.properties.get("sb-"+teamname).get().length() > 0)
					PacketAPI.removeScoreboardScore(p, player, teamname);
			}
		}
		public void unregister() {
			for (ITabPlayer p : players) {
				if (p.properties.get("sb-"+teamname).get().length() > 0)
					PacketAPI.removeScoreboardScore(p, player, teamname);
			}
		}
		public void updatePrefixSuffix() {
			for (ITabPlayer p : players.toArray(new ITabPlayer[0])) {
				List<String> prefixsuffix = replaceText(p, false, false);
				if (prefixsuffix == null) continue;
				PacketAPI.updateScoreboardTeamPrefixSuffix(p, teamname, prefixsuffix.get(0), prefixsuffix.get(1), false, false);
			}
		}
	}
}