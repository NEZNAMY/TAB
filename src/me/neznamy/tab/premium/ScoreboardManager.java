package me.neznamy.tab.premium;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.neznamy.tab.shared.Configs;
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
	private boolean remember_toggle_choice;
	private List<String> sb_off_players;

	private String scoreboard_on;
	private String scoreboard_off;

	@SuppressWarnings("unchecked")
	@Override
	public void load() {
		toggleCommand = Premium.premiumconfig.getString("scoreboard.toggle-command", "/sb");
		useNumbers = Premium.premiumconfig.getBoolean("scoreboard.use-numbers", false);
		disabledWorlds = Premium.premiumconfig.getStringList("scoreboard.disable-in-worlds", Arrays.asList("disabledworld"));
		if (disabledWorlds == null) disabledWorlds = new ArrayList<>();
		defaultScoreboard = Premium.premiumconfig.getString("scoreboard.default-scoreboard", "MyDefaultScoreboard");
		refresh = Premium.premiumconfig.getInt("scoreboard.refresh-interval-milliseconds", 50);
		if (refresh < 50) Shared.errorManager.refreshTooLow("Scoreboard", refresh);
		perWorld = Premium.premiumconfig.getConfigurationSection("scoreboard.per-world");
		remember_toggle_choice = Premium.premiumconfig.getBoolean("scoreboard.remember-toggle-choice", false);
		scoreboard_on = Premium.premiumconfig.getString("scoreboard-on", "&2Scorebord enabled");
		scoreboard_off = Premium.premiumconfig.getString("scoreboard-off", "&7Scoreboard disabled");
		if (remember_toggle_choice) {
			sb_off_players = Configs.getPlayerData("scoreboard-off");
		}
		if (sb_off_players == null) sb_off_players = new ArrayList<String>();
		for (Object scoreboard : Premium.premiumconfig.getConfigurationSection("scoreboards").keySet()) {
			String condition = Premium.premiumconfig.getString("scoreboards." + scoreboard + ".display-condition");
			String childBoard = Premium.premiumconfig.getString("scoreboards." + scoreboard + ".if-condition-not-met");
			String title = Premium.premiumconfig.getString("scoreboards." + scoreboard + ".title");
			List<String> lines = Premium.premiumconfig.getStringList("scoreboards." + scoreboard + ".lines");
			scoreboards.put(scoreboard+"", new Scoreboard(this, scoreboard+"", title, lines, condition, childBoard));
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
						send(p);
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
		p.hiddenScoreboard = sb_off_players.contains(p.getName());
		send(p);
	}
	private void send(ITabPlayer p) {
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
		if (p.getActiveScoreboard() != null) p.getActiveScoreboard().getRegisteredUsers().remove(p);
		p.setActiveScoreboard(null);
	}
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		onQuit(p);
		send(p);
	}
	public String getHighestScoreboard(ITabPlayer p) {
		String scoreboard = perWorld.get(p.getWorldName());
		if (scoreboard == null && !defaultScoreboard.equalsIgnoreCase("NONE")) scoreboard = defaultScoreboard;
		if (scoreboard != null) {
			Scoreboard board = scoreboards.get(scoreboard);
			while (board != null && !board.isConditionMet(p)) {
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
				if (remember_toggle_choice && !sb_off_players.contains(sender.getName())) {
					sb_off_players.add(sender.getName());
					Configs.playerdata.set("scoreboard-off", sb_off_players);
				}
			} else {
				send(sender);
				sender.sendMessage(scoreboard_on);
				if (remember_toggle_choice) {
					sb_off_players.remove(sender.getName());
					Configs.playerdata.set("scoreboard-off", sb_off_players);
				}
			}
			return true;
		}
		if (message.equalsIgnoreCase(toggleCommand + " on")) {
			if (sender.hiddenScoreboard) {
				send(sender);
				sender.sendMessage(scoreboard_on);
				sender.hiddenScoreboard = false;
				if (remember_toggle_choice) {
					sb_off_players.remove(sender.getName());
					Configs.playerdata.set("scoreboard-off", sb_off_players);
				}
			}
			return true;
		}
		if (message.equalsIgnoreCase(toggleCommand + " off")) {
			if (!sender.hiddenScoreboard) {
				onQuit(sender);
				sender.sendMessage(scoreboard_off);
				sender.hiddenScoreboard = true;
				if (remember_toggle_choice) {
					sb_off_players.add(sender.getName());
					Configs.playerdata.set("scoreboard-off", sb_off_players);
				}
			}
			return true;
		}
		return false;
	}
}