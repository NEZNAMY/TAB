package me.neznamy.tab.premium.scoreboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.premium.Premium;
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
	public List<me.neznamy.tab.api.Scoreboard> APIscoreboards = new ArrayList<>();
	public boolean permToToggle;
	public int staticNumber;

	public String scoreboard_on;
	public String scoreboard_off;

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
			Shared.featureManager.registerFeature("scoreboard-" + scoreboard, sb);
		}
		if (!defaultScoreboard.equalsIgnoreCase("NONE") && !scoreboards.containsKey(defaultScoreboard)) {
			Shared.errorManager.startupWarn("Unknown scoreboard &e\"" + defaultScoreboard + "\"&c set as default scoreboard.");
			defaultScoreboard = "NONE";
		}
		for (Entry<String, String> entry : perWorld.entrySet()) {
			if (!scoreboards.containsKey(entry.getValue())) {
				Shared.errorManager.startupWarn("Unknown scoreboard &e\"" + entry.getValue() + "\"&c set as per-world scoreboard in world &e\"" + entry.getKey() + "\"&c.");
			}
		}
		for (Scoreboard scoreboard : scoreboards.values()) {
			if (scoreboard.getChildScoreboard() != null && !scoreboards.containsKey(scoreboard.getChildScoreboard())) {
				Shared.errorManager.startupWarn("Unknown scoreboard &e\"" + scoreboard.getChildScoreboard() + "\"&c set as if-condition-not-met of scoreboard &e\"" + scoreboard.getName() + "\"&c.");
			}
		}
	}

	@Override
	public void load() {
		for (TabPlayer p : Shared.getPlayers()) {
			onJoin(p);
		}
		Shared.cpu.startRepeatingMeasuredTask(1000, "refreshing scoreboard conditions", TabFeature.SCOREBOARD, UsageType.REPEATING_TASK, new Runnable() {
			public void run() {
				for (TabPlayer p : Shared.getPlayers()) {
					if (!p.isLoaded() || p.hasForcedScoreboard() || !p.isScoreboardVisible()) continue;
					me.neznamy.tab.api.Scoreboard board = p.getActiveScoreboard();
					String current = board == null ? "null" : board.getName();
					String highest = detectHighestScoreboard(p);
					if (!current.equals(highest)) {
						if (p.getActiveScoreboard() != null) p.getActiveScoreboard().unregister(p);
						p.setActiveScoreboard(null);
						sendHighestScoreboard(p);
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
		for (TabPlayer p : Shared.getPlayers()) {
			p.setActiveScoreboard(null);
		}
		scoreboards.clear();
	}

	@Override
	public void onJoin(TabPlayer p) {
		p.setScoreboardVisible(!sb_off_players.contains(p.getName()), false);
	}

	public void sendHighestScoreboard(TabPlayer p) {
		if (disabledWorlds.contains(p.getWorldName()) || !p.isScoreboardVisible()) return;
		String scoreboard = detectHighestScoreboard(p);
		if (scoreboard != null) {
			Scoreboard board = scoreboards.get(scoreboard);
			if (board != null) {
				p.setActiveScoreboard(board);
				board.register(p);
			}
		}
	}

	@Override
	public void onQuit(TabPlayer p) {
		unregisterScoreboard(p, false);
	}

	public void unregisterScoreboard(TabPlayer p, boolean sendUnregisterPacket) {
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
	public void onWorldChange(TabPlayer p, String from, String to) {
		unregisterScoreboard(p, true);
		sendHighestScoreboard(p);
	}

	public String detectHighestScoreboard(TabPlayer p) {
		String scoreboard = perWorld.get(p.getWorldName());
		if (scoreboard == null) {
			if (defaultScoreboard.equalsIgnoreCase("NONE")) {
				return "null";
			} else {
				scoreboard = defaultScoreboard;
			}
		}
		Scoreboard board = scoreboards.get(scoreboard);
		while (board != null && !board.isConditionMet(p)) {
			board = scoreboards.get(board.getChildScoreboard());
			if (board == null) return "null";
			scoreboard = board.getName();
		}
		return scoreboard;
	}

	@Override
	public boolean onCommand(TabPlayer sender, String message) {
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