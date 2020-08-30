package me.neznamy.tab.premium.scoreboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.neznamy.tab.premium.Premium;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.interfaces.CommandListener;
import me.neznamy.tab.shared.features.interfaces.JoinEventListener;
import me.neznamy.tab.shared.features.interfaces.Loadable;
import me.neznamy.tab.shared.features.interfaces.QuitEventListener;
import me.neznamy.tab.shared.features.interfaces.WorldChangeListener;

/**
 * Feature handler for scoreboard feature
 */
public class ScoreboardManager implements Loadable, JoinEventListener, QuitEventListener, WorldChangeListener, CommandListener{

	private String toggleCommand;
	private List<String> disabledWorlds;
	private String defaultScoreboard;
	private Map<String, String> perWorld;
	private Map<String, Scoreboard> scoreboards = new HashMap<String, Scoreboard>();
	public boolean useNumbers;
	public boolean remember_toggle_choice;
	public List<String> sb_off_players;
	public List<Scoreboard> APIscoreboards = new ArrayList<>();
	public boolean permToToggle;
	public int staticNumber;

	public String scoreboard_on;
	public String scoreboard_off;

	@SuppressWarnings("unchecked")
	public ScoreboardManager() {
		toggleCommand = Premium.premiumconfig.getString("scoreboard.toggle-command", "/sb");
		useNumbers = Premium.premiumconfig.getBoolean("scoreboard.use-numbers", false);
		permToToggle = Premium.premiumconfig.getBoolean("scoreboard.permission-required-to-toggle", false);
		disabledWorlds = Premium.premiumconfig.getStringList("scoreboard.disable-in-worlds", Arrays.asList("disabledworld"));
		if (disabledWorlds == null) disabledWorlds = new ArrayList<>();
		defaultScoreboard = Premium.premiumconfig.getString("scoreboard.default-scoreboard", "MyDefaultScoreboard");
		perWorld = Premium.premiumconfig.getConfigurationSection("scoreboard.per-world");
		remember_toggle_choice = Premium.premiumconfig.getBoolean("scoreboard.remember-toggle-choice", false);
		scoreboard_on = Premium.premiumconfig.getString("scoreboard-on", "&2Scorebord enabled");
		scoreboard_off = Premium.premiumconfig.getString("scoreboard-off", "&7Scoreboard disabled");
		if (remember_toggle_choice) {
			sb_off_players = Configs.getPlayerData("scoreboard-off");
		}
		if (sb_off_players == null) sb_off_players = new ArrayList<String>();
		staticNumber = Premium.premiumconfig.getInt("scoreboard.static-number", 0);
		
		for (Object scoreboard : Premium.premiumconfig.getConfigurationSection("scoreboards").keySet()) {
			String condition = Premium.premiumconfig.getString("scoreboards." + scoreboard + ".display-condition");
			String childBoard = Premium.premiumconfig.getString("scoreboards." + scoreboard + ".if-condition-not-met");
			String title = Premium.premiumconfig.getString("scoreboards." + scoreboard + ".title");
			if (title == null) {
				title = "<Title not defined>";
				Shared.errorManager.missingAttribute("Scoreboard", scoreboard, "title");
			}
			List<String> lines = Premium.premiumconfig.getStringList("scoreboards." + scoreboard + ".lines");
			if (lines == null) {
				lines = new ArrayList<String>();
				Shared.errorManager.missingAttribute("Scoreboard", scoreboard, "lines");
			}
			Scoreboard sb = new Scoreboard(this, scoreboard+"", title, lines, condition, childBoard);
			scoreboards.put(scoreboard+"", sb);
			Shared.registerFeature("scoreboard-" + scoreboard, sb);
		}	
	}
	
	@Override
	public void load() {
		for (ITabPlayer p : Shared.getPlayers()) {
			onJoin(p);
		}
		Shared.cpu.startRepeatingMeasuredTask(1000, "refreshing scoreboard conditions", TabFeature.SCOREBOARD, UsageType.REPEATING_TASK, new Runnable() {
			public void run() {
				for (ITabPlayer p : Shared.getPlayers()) {
					if (!p.onJoinFinished) continue;
					if (p.forcedScoreboard != null) continue;
					Scoreboard board = p.getActiveScoreboard();
					String current = board == null ? "null" : board.getName();
					String highest = getHighestScoreboard(p);
					if (!current.equals(highest)) {
						if (p.getActiveScoreboard() != null) p.getActiveScoreboard().unregister(p);
						p.setActiveScoreboard(null);
						send(p);
					}
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
	
	public void send(ITabPlayer p) {
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
		unregisterScoreboard(p, false);
	}
	
	public void unregisterScoreboard(ITabPlayer p, boolean sendUnregisterPacket) {
		if (p.getActiveScoreboard() != null) {
			if (sendUnregisterPacket) {
				p.getActiveScoreboard().unregister(p);
			} else {
				p.getActiveScoreboard().getRegisteredUsers().remove(p);
			}
			p.setActiveScoreboard(null);
		}
	}
	
	@Override
	public void onWorldChange(ITabPlayer p, String from, String to) {
		unregisterScoreboard(p, true);
		send(p);
	}
	
	public String getHighestScoreboard(ITabPlayer p) {
		String scoreboard = perWorld.get(p.getWorldName());
		if (scoreboard == null && !defaultScoreboard.equalsIgnoreCase("NONE")) scoreboard = defaultScoreboard;
		if (scoreboard != null) {
			Scoreboard board = scoreboards.get(scoreboard);
			while (board != null && !board.isConditionMet(p)) {
				board = scoreboards.get(board.getChildScoreboard());
				if (board == null) return "null";
				scoreboard = board.getName();
			}
		}
		return scoreboard;
	}
	
	@Override
	public boolean onCommand(ITabPlayer sender, String message) {
		if (disabledWorlds.contains(sender.getWorldName())) return false;
		if (message.equalsIgnoreCase(toggleCommand)) {
			Shared.command.execute(sender, new String[] {"scoreboard"});
			return true;
		}
		if (message.equalsIgnoreCase(toggleCommand + " on")) {
			Shared.command.execute(sender, new String[] {"scoreboard on"});
			return true;
		}
		if (message.equalsIgnoreCase(toggleCommand + " off")) {
			Shared.command.execute(sender, new String[] {"scoreboard off"});
			return true;
		}
		return false;
	}
	
	public Map<String, Scoreboard> getScoreboards(){
		return scoreboards;
	}
	
	@Override
	public TabFeature getFeatureType() {
		return TabFeature.SCOREBOARD;
	}
}