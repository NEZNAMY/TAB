package me.neznamy.tab.shared.features.scoreboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.types.Loadable;
import me.neznamy.tab.shared.features.types.event.CommandListener;
import me.neznamy.tab.shared.features.types.event.JoinEventListener;
import me.neznamy.tab.shared.features.types.event.QuitEventListener;
import me.neznamy.tab.shared.features.types.event.WorldChangeListener;
import me.neznamy.tab.shared.features.types.packet.DisplayObjectivePacketListener;
import me.neznamy.tab.shared.features.types.packet.ObjectivePacketListener;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective;

/**
 * Feature handler for scoreboard feature
 */
public class ScoreboardManager implements Loadable, JoinEventListener, QuitEventListener, WorldChangeListener, CommandListener, ObjectivePacketListener, DisplayObjectivePacketListener{

	public static final String ObjectiveName = "TAB-Scoreboard";
	public static final int DisplaySlot = 1;
	
	public TAB tab;
	private String toggleCommand;
	private List<String> disabledWorlds;
	private String defaultScoreboard;
	private Map<String, String> perWorld;
	private Map<String, Scoreboard> scoreboards = new HashMap<String, Scoreboard>();
	public boolean useNumbers;
	public boolean remember_toggle_choice;
	public List<String> sb_off_players = new ArrayList<String>();
	public List<me.neznamy.tab.api.Scoreboard> APIscoreboards = new ArrayList<>();
	public boolean permToToggle;
	public int staticNumber;
	private boolean hiddenByDefault;

	public String scoreboard_on;
	public String scoreboard_off;
	
	public me.neznamy.tab.api.Scoreboard announcement;

	public ScoreboardManager(TAB tab) {
		this.tab = tab;
		toggleCommand = tab.getConfiguration().premiumconfig.getString("scoreboard.toggle-command", "/sb");
		useNumbers = tab.getConfiguration().premiumconfig.getBoolean("scoreboard.use-numbers", false);
		permToToggle = tab.getConfiguration().premiumconfig.getBoolean("scoreboard.permission-required-to-toggle", false);
		disabledWorlds = tab.getConfiguration().premiumconfig.getStringList("scoreboard.disable-in-worlds", Arrays.asList("disabledworld"));
		defaultScoreboard = tab.getConfiguration().premiumconfig.getString("scoreboard.default-scoreboard", "MyDefaultScoreboard");
		perWorld = tab.getConfiguration().premiumconfig.getConfigurationSection("scoreboard.per-world");
		remember_toggle_choice = tab.getConfiguration().premiumconfig.getBoolean("scoreboard.remember-toggle-choice", false);
		hiddenByDefault = tab.getConfiguration().premiumconfig.getBoolean("scoreboard.hidden-by-default", false);
		scoreboard_on = tab.getConfiguration().premiumconfig.getString("scoreboard-on", "&2Scorebord enabled");
		scoreboard_off = tab.getConfiguration().premiumconfig.getString("scoreboard-off", "&7Scoreboard disabled");
		if (remember_toggle_choice) {
			sb_off_players = tab.getConfiguration().getPlayerData("scoreboard-off");
		}
		staticNumber = tab.getConfiguration().premiumconfig.getInt("scoreboard.static-number", 0);

		for (Object scoreboard : tab.getConfiguration().premiumconfig.getConfigurationSection("scoreboards").keySet()) {
			String condition = tab.getConfiguration().premiumconfig.getString("scoreboards." + scoreboard + ".display-condition");
			String childBoard = tab.getConfiguration().premiumconfig.getString("scoreboards." + scoreboard + ".if-condition-not-met");
			String title = tab.getConfiguration().premiumconfig.getString("scoreboards." + scoreboard + ".title");
			if (title == null) {
				title = "<Title not defined>";
				tab.getErrorManager().missingAttribute("Scoreboard", scoreboard, "title");
			}
			List<String> lines = tab.getConfiguration().premiumconfig.getStringList("scoreboards." + scoreboard + ".lines");
			if (lines == null) {
				lines = Arrays.asList("scoreboard \"" + scoreboard +"\" is missing \"lines\" keyword!", "did you forget to configure it or just your spacing is wrong?");
				tab.getErrorManager().missingAttribute("Scoreboard", scoreboard, "lines");
			}
			Scoreboard sb = new Scoreboard(this, scoreboard+"", title, lines, condition, childBoard);
			scoreboards.put(scoreboard+"", sb);
			tab.getFeatureManager().registerFeature("scoreboard-" + scoreboard, sb);
		}
		checkForMisconfiguration();
	}

	private void checkForMisconfiguration() {
		if (!defaultScoreboard.equalsIgnoreCase("NONE") && !scoreboards.containsKey(defaultScoreboard)) {
			tab.getErrorManager().startupWarn("Unknown scoreboard &e\"" + defaultScoreboard + "\"&c set as default scoreboard.");
			defaultScoreboard = "NONE";
		}
		for (Entry<String, String> entry : perWorld.entrySet()) {
			if (!scoreboards.containsKey(entry.getValue())) {
				tab.getErrorManager().startupWarn("Unknown scoreboard &e\"" + entry.getValue() + "\"&c set as per-world scoreboard in world &e\"" + entry.getKey() + "\"&c.");
			}
		}
		for (Scoreboard scoreboard : scoreboards.values()) {
			if (scoreboard.getChildScoreboard() != null && !scoreboards.containsKey(scoreboard.getChildScoreboard())) {
				tab.getErrorManager().startupWarn("Unknown scoreboard &e\"" + scoreboard.getChildScoreboard() + "\"&c set as if-condition-not-met of scoreboard &e\"" + scoreboard.getName() + "\"&c.");
			}
		}
	}

	@Override
	public void load() {
		for (TabPlayer p : tab.getPlayers()) {
			onJoin(p);
		}
		tab.getCPUManager().startRepeatingMeasuredTask(1000, "refreshing scoreboard conditions", TabFeature.SCOREBOARD, UsageType.REPEATING_TASK, new Runnable() {
			public void run() {
				for (TabPlayer p : tab.getPlayers()) {
					if (!p.isLoaded() || p.hasForcedScoreboard() || !p.isScoreboardVisible() || announcement != null || p.getOtherPluginScoreboard() != null) continue;
					me.neznamy.tab.api.Scoreboard board = p.getActiveScoreboard();
					String current = board == null ? "null" : board.getName();
					String highest = detectHighestScoreboard(p);
					if (!current.equals(highest)) {
						if (p.getActiveScoreboard() != null) p.getActiveScoreboard().unregister(p);
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
		for (TabPlayer p : tab.getPlayers()) {
			p.setActiveScoreboard(null);
		}
		scoreboards.clear();
	}

	@Override
	public void onJoin(TabPlayer p) {
		p.setScoreboardVisible(!sb_off_players.contains(p.getName()) && !hiddenByDefault, false);
	}

	public void sendHighestScoreboard(TabPlayer p) {
		if (isDisabledWorld(disabledWorlds, p.getWorldName()) || !p.isScoreboardVisible()) return;
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
		if (isDisabledWorld(disabledWorlds, sender.getWorldName())) return false;
		if (message.equals(toggleCommand) || message.startsWith(toggleCommand+" ")) {
			tab.command.execute(sender, message.replace(toggleCommand,"scoreboard").split(" "));
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

	@Override
	public boolean onPacketSend(TabPlayer receiver, PacketPlayOutScoreboardDisplayObjective packet) {
		if (packet.slot == DisplaySlot && !packet.objectiveName.equals(ObjectiveName)) {
			receiver.setOtherPluginScoreboard(packet.objectiveName);
			if (receiver.getActiveScoreboard() != null) {
				tab.getCPUManager().runMeasuredTask("send packets", TabFeature.SCOREBOARD, UsageType.ANTI_OVERRIDE, () -> receiver.getActiveScoreboard().unregister(receiver));
			}
		}
		return false;
	}

	@Override
	public void onPacketSend(TabPlayer receiver, PacketPlayOutScoreboardObjective packet) {
		if (packet.method == 1 && receiver.getOtherPluginScoreboard() != null && receiver.getOtherPluginScoreboard().equals(packet.objectiveName)) {
			receiver.setOtherPluginScoreboard(null);
			tab.getCPUManager().runMeasuredTask("send packets", TabFeature.SCOREBOARD, UsageType.ANTI_OVERRIDE, () -> sendHighestScoreboard(receiver));
		}
	}
}