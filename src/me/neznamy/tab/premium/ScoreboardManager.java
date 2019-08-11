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
			playerJoin(p);
		}
		Shared.scheduleRepeatingTask(refresh*50, "refreshing scoreboard", Feature.SCOREBOARD, new Runnable() {
			public void run() {
				for (Scoreboard board : scoreboards.values()) {
					board.refresh();
				}
			}
		});
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
	public static void playerJoin(ITabPlayer p) {
		if (!enabled) return;
		if (disabledWorlds.contains(p.getWorldName())) return;
		String scoreboard = perWorld.get(p.getWorldName());
		if (scoreboard == null && !defaultScoreboard.equalsIgnoreCase("NONE")) scoreboard = defaultScoreboard;
		if (scoreboard != null) {
			Scoreboard board = scoreboards.get(scoreboard);
			board.register(p);
			p.setActiveScoreboard(board);
		}
	}
	public static void playerQuit(ITabPlayer p) {
		if (!enabled) return;
		if (p.getActiveScoreboard() != null) p.getActiveScoreboard().unregister(p);
	}
	public static boolean onCommand(final ITabPlayer sender, String message) {
		if (!enabled) return false;
		if (disabledWorlds.contains(sender.getWorldName())) return false;
		if (message.equalsIgnoreCase(toggleCommand)) {
			if (sender.hiddenScoreboard) {
				playerJoin(sender);
				sender.sendMessage(scoreboard_on);
			} else {
				if (sender.getActiveScoreboard() != null) sender.getActiveScoreboard().unregister(sender);
				sender.setActiveScoreboard(null);
				sender.sendMessage(scoreboard_off);
			}
			sender.hiddenScoreboard = !sender.hiddenScoreboard;
			return true;
		}
		return false;
	}
}
