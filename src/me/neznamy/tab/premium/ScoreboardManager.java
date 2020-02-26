package me.neznamy.tab.premium;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.features.SimpleFeature;

public class ScoreboardManager implements SimpleFeature{

	private String toggleCommand;
	private List<String> disabledWorlds;
	private String defaultScoreboard;
	private int refresh;
	private Map<String, String> perWorld;
	private Map<String, Scoreboard> scoreboards = new HashMap<String, Scoreboard>();
	public boolean useNumbers;

	private String scoreboard_on;
	private String scoreboard_off;

	@SuppressWarnings("unchecked")
	@Override
	public void load() {
		toggleCommand = Premium.premiumconfig.getString("scoreboard.toggle-command", "/sb");
		useNumbers = Premium.premiumconfig.getBoolean("scoreboard.use-numbers", false);
		disabledWorlds = Premium.premiumconfig.getStringList("scoreboard.disable-in-worlds", Arrays.asList("disabledworld"));
		defaultScoreboard = Premium.premiumconfig.getString("scoreboard.default-scoreboard", "MyDefaultScoreboard");
		refresh = Premium.premiumconfig.getInt("scoreboard.refresh-interval-ticks", 1)*50;
		perWorld = (Map<String, String>) Premium.premiumconfig.get("scoreboard.per-world");
		scoreboard_on = Premium.premiumconfig.getString("scoreboard-on", "&2Scorebord enabled");
		scoreboard_off = Premium.premiumconfig.getString("scoreboard-off", "&7Scoreboard disabled");
		if (Premium.premiumconfig.get("scoreboards") != null)
			for (String scoreboard : ((Map<String, Object>) Premium.premiumconfig.get("scoreboards")).keySet()) {
				boolean permissionRequired = Premium.premiumconfig.getBoolean("scoreboards." + scoreboard + ".permission-required", false);
				String childBoard = Premium.premiumconfig.getString("scoreboards." + scoreboard + ".if-permission-missing");
				String title = Premium.premiumconfig.getString("scoreboards." + scoreboard + ".title");
				List<String> lines = Premium.premiumconfig.getStringList("scoreboards." + scoreboard + ".lines");
				scoreboards.put(scoreboard, new Scoreboard(this, scoreboard, title, lines, permissionRequired, childBoard));
			}
		for (ITabPlayer p : Shared.getPlayers()) {
			onJoin(p);
		}
		Shared.cpu.startRepeatingMeasuredTask(refresh, "refreshing scoreboard", "Scoreboard", new Runnable() {
			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) {
					Scoreboard board = p.getActiveScoreboard();
					String current = board == null ? null : board.getName();
					String highest = getHighestScoreboard(p);
					if (current == null && highest == null) continue;
					if ((current == null && highest != null) || (current != null && highest == null) || (!current.equals(highest))) {
						if (p.getActiveScoreboard() != null) p.getActiveScoreboard().unregister(p);
						p.setActiveScoreboard(null);
						onJoin(p);
					}
				}
				for (Scoreboard board : scoreboards.values()) {
					board.refresh();
				}
			}
		});
	}
	@Override
	public void unload() {
		for (Scoreboard board : scoreboards.values()) {
			board.unregister();
		}
		for (ITabPlayer p : Shared.getPlayers()) {
			p.setActiveScoreboard(null);
		}
		scoreboards.clear();
	}
	@Override
	public void onJoin(ITabPlayer p) {
		if (disabledWorlds.contains(p.getWorldName()) || p.hiddenScoreboard || p.getActiveScoreboard() != null) return;
		String scoreboard = getHighestScoreboard(p);
		if (scoreboard != null) {
			Scoreboard board = scoreboards.get(scoreboard);
			if (board != null) {
				p.setActiveScoreboard(board);
				board.register(p);
			}
		}
	}
	@Override
	public void onQuit(ITabPlayer p) {
		if (p.getActiveScoreboard() != null) p.getActiveScoreboard().unregister(p);
		p.setActiveScoreboard(null);
	}
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		onQuit(p);
		onJoin(p);
	}
	public String getHighestScoreboard(ITabPlayer p) {
		String scoreboard = perWorld.get(p.getWorldName());
		if (scoreboard == null && !defaultScoreboard.equalsIgnoreCase("NONE")) scoreboard = defaultScoreboard;
		if (scoreboard != null) {
			Scoreboard board = scoreboards.get(scoreboard);
			while (board != null && board.isPermissionRequired() && !p.hasPermission("tab.scoreboard." + board.getName())) {
				board = scoreboards.get(board.getChildScoreboard());
				if (board == null) return null;
				scoreboard = board.getName();
			}
		}
		return scoreboard;
	}
	public boolean onCommand(ITabPlayer sender, String message) {
		if (disabledWorlds.contains(sender.getWorldName())) return false;
		if (message.equalsIgnoreCase(toggleCommand)) {
			sender.hiddenScoreboard = !sender.hiddenScoreboard;
			if (sender.hiddenScoreboard) {
				onQuit(sender);
				sender.sendMessage(scoreboard_off);
			} else {
				onJoin(sender);
				sender.sendMessage(scoreboard_on);
			}
			return true;
		}
		if (message.equalsIgnoreCase(toggleCommand + " on")) {
			if (sender.hiddenScoreboard) {
				onJoin(sender);
				sender.sendMessage(scoreboard_on);
				sender.hiddenScoreboard = false;
			}
			return true;
		}
		if (message.equalsIgnoreCase(toggleCommand + " off")) {
			if (!sender.hiddenScoreboard) {
				onQuit(sender);
				sender.sendMessage(scoreboard_off);
				sender.hiddenScoreboard = true;
			}
			return true;
		}
		return false;
	}
}