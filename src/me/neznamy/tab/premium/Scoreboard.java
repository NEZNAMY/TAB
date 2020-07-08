package me.neznamy.tab.premium;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PacketAPI;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.features.interfaces.Refreshable;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;
import me.neznamy.tab.shared.packets.EnumChatFormat;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective.EnumScoreboardHealthDisplay;
import me.neznamy.tab.shared.placeholders.Placeholder;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class Scoreboard implements me.neznamy.tab.api.Scoreboard, Refreshable{

	private final String ObjectiveName = "TAB-Scoreboard";
	private final int DisplaySlot = 1;

	private ScoreboardManager manager;
	private String name;
	private String title;
	private String displayCondition;
	private String childBoard;
	private List<Score> scores = new ArrayList<Score>();
	private List<ITabPlayer> players = new ArrayList<ITabPlayer>();
	private List<Placeholder> conditionPlaceholders = new ArrayList<Placeholder>();
	private Set<String> usedPlaceholders;

	public Scoreboard(String name, String title, List<String> lines, String displayCondition, String childBoard) {
		this(name, title, lines);
		this.displayCondition = displayCondition;
		this.childBoard = childBoard;
		conditionPlaceholders = Placeholders.detectPlaceholders(displayCondition);
		usedPlaceholders = Placeholders.getUsedPlaceholderIdentifiersRecursive(title);
	}
	public Scoreboard(String name, String title, List<String> lines) {
		this.manager = (ScoreboardManager) Shared.features.get("scoreboard");
		this.name = name;
		this.title = title;
		for (int i=0; i<lines.size(); i++) {
			Score score = new Score(lines.size()-i, "TAB-SB-TM-"+i, getLineName(i),  lines.get(i));
			scores.add(score);
			Shared.registerFeature("scoreboard-score-" + name + "-" + i, score);
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
		return Placeholders.colorChar + String.valueOf(id.charAt(0)) + Placeholders.colorChar + String.valueOf(id.charAt(1));
	}
	public List<ITabPlayer> getRegisteredUsers(){
		return players;
	}
	public void register(ITabPlayer p) {
		if (!players.contains(p)) {
			p.setProperty("scoreboard-title", title, null);
			PacketAPI.registerScoreboardObjective(p, ObjectiveName, p.properties.get("scoreboard-title").get(), DisplaySlot, EnumScoreboardHealthDisplay.INTEGER);
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
			p.sendCustomPacket(PacketPlayOutScoreboardObjective.UNREGISTER(ObjectiveName));
			for (Score s : scores) {
				s.unregister(p);
			}
			players.remove(p);
		}
	}

	@Override
	public void refresh(ITabPlayer refreshed, boolean force) {
		if (refreshed.properties.get("scoreboard-title") == null) return;
		refreshed.sendCustomPacket(PacketPlayOutScoreboardObjective.UPDATE_TITLE(ObjectiveName, refreshed.properties.get("scoreboard-title").updateAndGet(), EnumScoreboardHealthDisplay.INTEGER));
	}
	@Override
	public CPUFeature getRefreshCPU() {
		return CPUFeature.SCOREBOARD_TITLE;
	}
	@Override
	public Set<String> getUsedPlaceholders() {
		return usedPlaceholders;
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



	private class Score implements Refreshable {

		private int score;
		private String rawtext;
		private String teamname;
		private String player;
		private Set<String> usedPlaceholders;
		private boolean Static;
		private String staticPrefix;
		private String staticName;
		private String staticSuffix;

		public Score(int score, String teamname, String player, String rawtext) {
			this.score = score;
			this.teamname = teamname;
			this.player = player;
			this.rawtext = rawtext;
			usedPlaceholders = Placeholders.getUsedPlaceholderIdentifiersRecursive(rawtext);
			Static = usedPlaceholders.isEmpty();
			if (Static) {
				rawtext = Placeholders.color(rawtext);
				if (rawtext.length() < 35) {
					//1-34
					staticPrefix = "";
					staticName = player + Placeholders.colorChar + "r" + rawtext;
					staticSuffix = "";
				} else {
					String[] sub = substring(rawtext, 16);
					staticPrefix = sub[0];
					String rest = sub[1];
					String last = Placeholders.getLastColors(staticPrefix);
					if (last.length() == 0) last = Placeholders.colorChar + "r";
					rest = player + last + rest;
					sub = substring(rest, 40);
					staticName = sub[0];
					staticSuffix = sub[1];
					if (staticSuffix.length() > 16) staticSuffix = staticSuffix.substring(0, 16);
				}
			}
		}
		private String[] substring(String string, int index) {
			if (string.length() < index) return new String[] {string, ""};
			if (string.charAt(index) == Placeholders.colorChar) index--;
			return new String[] {string.substring(0, index), string.substring(index, string.length())};
		}
		private List<String> replaceText(ITabPlayer p, boolean force, boolean suppressToggle) {
			if (Static && p.getVersion().getMinorVersion() < 13) {
				return Arrays.asList(staticPrefix, staticSuffix);
			}
			Property scoreproperty = p.properties.get("sb-"+teamname);
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
				String last = Placeholders.getLastColors(prefix);
				if (last.length() == 0) last = Placeholders.colorChar + "r";
				suffix = last + suffix;
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

		}
		public void register(ITabPlayer p) {
			p.setProperty("sb-"+teamname, rawtext, null);
			List<String> prefixsuffix = replaceText(p, true, true);
			if (prefixsuffix == null) return;
			int score = (p.getVersion().getMinorVersion() < 8 || manager.useNumbers) ? this.score : 0;
			PacketAPI.registerScoreboardScore(p, teamname, getName(p), prefixsuffix.get(0), prefixsuffix.get(1), ObjectiveName, score);
		}
		private void unregister(ITabPlayer p) {
			if (players.contains(p) && p.properties.get("sb-"+teamname).get().length() > 0) {
				PacketAPI.removeScoreboardScore(p, getName(p), teamname);
			}
		}
		private String getName(ITabPlayer p) {
			if (Static && p.getVersion().getMinorVersion() < 13) return staticName;
			return player;
		}
		@Override
		public void refresh(ITabPlayer refreshed, boolean force) {
			if (!players.contains(refreshed)) return; //player has different scoreboard displayed
			List<String> prefixsuffix = replaceText(refreshed, force, false);
			if (prefixsuffix == null) return;
			PacketPlayOutScoreboardTeam update = PacketPlayOutScoreboardTeam.UPDATE_TEAM_INFO(teamname, prefixsuffix.get(0), prefixsuffix.get(1), "always", "always", 69);
			update.setColor(EnumChatFormat.RESET);
			refreshed.sendCustomPacket(update);
		}
		@Override
		public CPUFeature getRefreshCPU() {
			return CPUFeature.SCOREBOARD_LINES;
		}
		@Override
		public Set<String> getUsedPlaceholders() {
			return usedPlaceholders;
		}
	}
}