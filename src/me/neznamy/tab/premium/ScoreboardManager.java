package me.neznamy.tab.premium;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.Shared.Feature;

public class ScoreboardManager {

	public static boolean enabled;
	public static String toggleCommand;
	public static List<Object> disabledWorlds;
	public static String defaultScoreboard;
	public static int refresh;
	public static Map<String, String> perWorld;
	public static Map<String, Scoreboard> scoreboards = new HashMap<String, Scoreboard>();

	public static String scoreboard_on;
	public static String scoreboard_off;

	public static void load() {
		if (!enabled) return;
		for (ITabPlayer p : Shared.getPlayers()) {
			register(p);
		}
		Shared.scheduleRepeatingTask(refresh*50, "refreshing scoreboard", Feature.SCOREBOARD, new Runnable() {
			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) {
					Scoreboard board = p.getActiveScoreboard();
					String current = board == null ? null : board.getName();
					String highest = getHighestScoreboard(p);
					if (current == null && highest == null) continue;
					if ((current == null && highest != null) || (current != null && highest == null) || (!current.equals(highest))) {
						unregister(p);
						register(p);
					}
				}
				for (Scoreboard board : scoreboards.values()) {
					board.refresh();
				}
			}
		});
	}
	public static String getHighestScoreboard(ITabPlayer p) {
		String scoreboard = perWorld.get(p.getWorldName());
		if (scoreboard == null && !defaultScoreboard.equalsIgnoreCase("NONE")) scoreboard = defaultScoreboard;
		if (scoreboard != null) {
			Scoreboard board = scoreboards.get(scoreboard);
			while (board.isPermissionRequired() && !p.hasPermission("tab.scoreboard." + board.getName())) {
				board = scoreboards.get(board.getChildScoreboard());
				if (board == null) return null;
				scoreboard = board.getName();
			}
		}
		return scoreboard;
	}
	public static void unload() {
		if (!enabled) return;
		for (Scoreboard board : scoreboards.values()) {
			board.unregister();
		}
		for (ITabPlayer p : Shared.getPlayers()) {
			p.setActiveScoreboard(null);
		}
		scoreboards.clear();
	}
	public static void register(ITabPlayer p) {
		if (!enabled) return;
		if (disabledWorlds.contains(p.getWorldName())) return;
		if (p.hiddenScoreboard) return;
		String scoreboard = getHighestScoreboard(p);
		if (scoreboard != null) {
			Scoreboard board = scoreboards.get(scoreboard);
			if (board != null) {
				board.register(p);
				p.setActiveScoreboard(board);
			}
		}
	}
	public static void unregister(ITabPlayer p) {
		if (!enabled) return;
		if (p.getActiveScoreboard() != null) p.getActiveScoreboard().unregister(p);
		p.setActiveScoreboard(null);
	}
	public static boolean onCommand(final ITabPlayer sender, String message) {
		if (!enabled) return false;
		if (disabledWorlds.contains(sender.getWorldName())) return false;
		if (message.equalsIgnoreCase(toggleCommand)) {
			sender.hiddenScoreboard = !sender.hiddenScoreboard;
			if (sender.hiddenScoreboard) {
				unregister(sender);
				sender.sendMessage(scoreboard_off);
			} else {
				register(sender);
				sender.sendMessage(scoreboard_on);
			}
			return true;
		}
		return false;
	}
}
