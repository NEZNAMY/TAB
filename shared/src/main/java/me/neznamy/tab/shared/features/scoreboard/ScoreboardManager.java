package me.neznamy.tab.shared.features.scoreboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.ITabPlayer;
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

	public static final String OBJECTIVE_NAME = "TAB-Scoreboard";
	public static final int DISPLAY_SLOT = 1;
	
	//tab instance
	private TAB tab;
	
	//toggle command
	private String toggleCommand;
	
	//list of disabled worlds/servers
	private List<String> disabledWorlds;
	
	//default scoreboard
	private String defaultScoreboard;
	
	//per-world / per-server scoreboards
	private Map<String, String> perWorld;
	
	//defined scoreboards
	private Map<String, ScoreboardImpl> scoreboards = new HashMap<>();
	
	//using 1-15
	private boolean useNumbers;
	
	//saving toggle choice into file
	private boolean rememberToggleChoice;
	
	//list of players with disabled scoreboard
	private Set<String> sbOffPlayers = new HashSet<>();
	
	//scoreboards registered via API
	private List<me.neznamy.tab.api.Scoreboard> apiScoreboards = new ArrayList<>();
	
	//permission required to toggle
	private boolean permToToggle;
	
	//if use-numbers is false, displaying this number in all lines
	private int staticNumber;
	
	//hidden by default, toggle command must be ran to show it
	private boolean hiddenByDefault;

	//scoreboard toggle on message
	private String scoreboardOn;
	
	//scoreboard toggle off message
	private String scoreboardOff;
	
	//currently active scoreboard announcement
	private me.neznamy.tab.api.Scoreboard announcement;
	
	//hiding TAB's scoreboard when another plugin sends one
	private boolean respectOtherPlugins;
	
	//config option someone requested
	private int joinDelay;
	private List<TabPlayer> joinDelayed = new ArrayList<>();
	
	private Set<TabPlayer> playersInDisabledWorlds = new HashSet<>();

	/**
	 * Constructs new instance and loads configuration
	 * @param tab - tab instance
	 */
	public ScoreboardManager(TAB tab) {
		this.tab = tab;
		toggleCommand = tab.getConfiguration().getPremiumConfig().getString("scoreboard.toggle-command", "/sb");
		useNumbers = tab.getConfiguration().getPremiumConfig().getBoolean("scoreboard.use-numbers", false);
		permToToggle = tab.getConfiguration().getPremiumConfig().getBoolean("scoreboard.permission-required-to-toggle", false);
		disabledWorlds = tab.getConfiguration().getPremiumConfig().getStringList("scoreboard.disable-in-worlds", Arrays.asList("disabledworld"));
		defaultScoreboard = tab.getConfiguration().getPremiumConfig().getString("scoreboard.default-scoreboard", "MyDefaultScoreboard");
		perWorld = tab.getConfiguration().getPremiumConfig().getConfigurationSection("scoreboard.per-world");
		rememberToggleChoice = tab.getConfiguration().getPremiumConfig().getBoolean("scoreboard.remember-toggle-choice", false);
		hiddenByDefault = tab.getConfiguration().getPremiumConfig().getBoolean("scoreboard.hidden-by-default", false);
		scoreboardOn = tab.getConfiguration().getPremiumConfig().getString("scoreboard-on", "&2Scorebord enabled");
		scoreboardOff = tab.getConfiguration().getPremiumConfig().getString("scoreboard-off", "&7Scoreboard disabled");
		respectOtherPlugins = TAB.getInstance().getConfiguration().getConfig().getBoolean("scoreboard.respect-other-plugins", true);
		if (isRememberToggleChoice()) {
			sbOffPlayers = Collections.synchronizedSet(new HashSet<>(tab.getConfiguration().getPlayerData("scoreboard-off")));
		}
		staticNumber = tab.getConfiguration().getPremiumConfig().getInt("scoreboard.static-number", 0);
		joinDelay = tab.getConfiguration().getPremiumConfig().getInt("scoreboard.delay-on-join-milliseconds", 0);

		for (Object scoreboard : tab.getConfiguration().getPremiumConfig().getConfigurationSection("scoreboards").keySet()) {
			String condition = tab.getConfiguration().getPremiumConfig().getString("scoreboards." + scoreboard + ".display-condition");
			String childBoard = tab.getConfiguration().getPremiumConfig().getString("scoreboards." + scoreboard + ".if-condition-not-met");
			String title = tab.getConfiguration().getPremiumConfig().getString("scoreboards." + scoreboard + ".title");
			if (title == null) {
				title = "<Title not defined>";
				tab.getErrorManager().missingAttribute("Scoreboard", scoreboard, "title");
			}
			List<String> lines = tab.getConfiguration().getPremiumConfig().getStringList("scoreboards." + scoreboard + ".lines");
			if (lines == null) {
				lines = Arrays.asList("scoreboard \"" + scoreboard +"\" is missing \"lines\" keyword!", "did you forget to configure it or just your spacing is wrong?");
				tab.getErrorManager().missingAttribute("Scoreboard", scoreboard, "lines");
			}
			ScoreboardImpl sb = new ScoreboardImpl(this, scoreboard.toString(), title, lines, condition, childBoard);
			scoreboards.put(scoreboard.toString(), sb);
			tab.getFeatureManager().registerFeature("scoreboard-" + scoreboard, sb);
		}
		checkForMisconfiguration();
		tab.debug(String.format("Loaded Scoreboard feature with parameters toggleCommand=%s, useNumbers=%s, permToToggle=%s, disabledWorlds=%s"
				+ ", defaultScoreboard=%s, perWorld=%s, rememberToggleChoice=%s, hiddenByDefault=%s, scoreboard_on=%s, scoreboard_off=%s, staticNumber=%s, joinDelay=%s",
				toggleCommand, isUseNumbers(), isPermToToggle(), disabledWorlds, defaultScoreboard, perWorld, isRememberToggleChoice(), isHiddenByDefault(), getScoreboardOn(), getScoreboardOff(), getStaticNumber(), joinDelay));
	}

	/**
	 * Checks for misconfiguration and sends console warns if anything was found
	 */
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
		for (ScoreboardImpl scoreboard : scoreboards.values()) {
			if (scoreboard.getChildScoreboard() != null && !scoreboards.containsKey(scoreboard.getChildScoreboard())) {
				tab.getErrorManager().startupWarn("Unknown scoreboard &e\"" + scoreboard.getChildScoreboard() + "\"&c set as if-condition-not-met of scoreboard &e\"" + scoreboard.getName() + "\"&c.");
			}
		}
	}

	@Override
	public void load() {
		for (TabPlayer p : tab.getPlayers()) {
			if (isDisabledWorld(disabledWorlds, p.getWorldName())) {
				playersInDisabledWorlds.add(p);
				return;
			}
			p.setScoreboardVisible(isHiddenByDefault() == getSbOffPlayers().contains(p.getName()), false);
		}
		tab.getCPUManager().startRepeatingMeasuredTask(1000, "refreshing scoreboard conditions", TabFeature.SCOREBOARD, UsageType.REPEATING_TASK, () -> {

			for (TabPlayer p : tab.getPlayers()) {
				if (!p.isLoaded() || p.hasForcedScoreboard() || !p.isScoreboardVisible() || 
					getAnnouncement() != null || ((ITabPlayer)p).getOtherPluginScoreboard() != null || joinDelayed.contains(p)) continue;
				me.neznamy.tab.api.Scoreboard board = p.getActiveScoreboard();
				String current = board == null ? "null" : board.getName();
				String highest = detectHighestScoreboard(p);
				if (!current.equals(highest)) {
					if (p.getActiveScoreboard() != null) p.getActiveScoreboard().unregister(p);
					sendHighestScoreboard(p);
				}
			}
		});
	}

	@Override
	public void unload() {
		for (ScoreboardImpl board : scoreboards.values()) {
			board.unregister();
		}
		for (TabPlayer p : tab.getPlayers()) {
			((ITabPlayer)p).setActiveScoreboard(null);
		}
		scoreboards.clear();
	}

	@Override
	public void onJoin(TabPlayer connectedPlayer) {
		if (isDisabledWorld(disabledWorlds, connectedPlayer.getWorldName())) {
			playersInDisabledWorlds.add(connectedPlayer);
			return;
		}
		if (joinDelay > 0) {
			joinDelayed.add(connectedPlayer);
			tab.getCPUManager().runTaskLater(joinDelay, "processing player join", getFeatureType(), UsageType.PLAYER_JOIN_EVENT, () -> {
				
				if (((ITabPlayer)connectedPlayer).getOtherPluginScoreboard() == null) connectedPlayer.setScoreboardVisible(isHiddenByDefault() == getSbOffPlayers().contains(connectedPlayer.getName()), false);
				joinDelayed.remove(connectedPlayer);
			});
		} else {
			connectedPlayer.setScoreboardVisible(isHiddenByDefault() == getSbOffPlayers().contains(connectedPlayer.getName()), false);
		}
	}

	/**
	 * Sends the player scoreboard he should see according to conditions and worlds
	 * @param p - player to send scoreboard to
	 */
	public void sendHighestScoreboard(TabPlayer p) {
		if (playersInDisabledWorlds.contains(p) || !p.isScoreboardVisible()) return;
		String scoreboard = detectHighestScoreboard(p);
		if (scoreboard != null) {
			ScoreboardImpl board = scoreboards.get(scoreboard);
			if (board != null) {
				((ITabPlayer)p).setActiveScoreboard(board);
				board.register(p);
			}
		}
	}

	@Override
	public void onQuit(TabPlayer p) {
		unregisterScoreboard(p, false);
		playersInDisabledWorlds.remove(p);
	}

	/**
	 * Removes this player from registered users in scoreboard and sends unregister packets if set
	 * @param p - player to unregister scoreboard to
	 * @param sendUnregisterPacket - if unregister packets should be sent or not
	 */
	public void unregisterScoreboard(TabPlayer p, boolean sendUnregisterPacket) {
		if (p.getActiveScoreboard() != null) {
			if (sendUnregisterPacket) {
				p.getActiveScoreboard().unregister(p);
			} else {
				p.getActiveScoreboard().getRegisteredUsers().remove(p);
			}
			((ITabPlayer)p).setActiveScoreboard(null);
		}
	}

	@Override
	public void onWorldChange(TabPlayer p, String from, String to) {
		if (isDisabledWorld(disabledWorlds, p.getWorldName())) {
			playersInDisabledWorlds.add(p);
		} else {
			playersInDisabledWorlds.remove(p);
		}
		unregisterScoreboard(p, true);
		sendHighestScoreboard(p);
	}

	/**
	 * Returns currently highest scoreboard in chain for specified player
	 * @param p - player to check
	 * @return highest scoreboard player should see
	 */
	public String detectHighestScoreboard(TabPlayer p) {
		String scoreboard = perWorld.get(tab.getConfiguration().getWorldGroupOf(perWorld.keySet(), p.getWorldName()));
		if (scoreboard == null) {
			if (defaultScoreboard.equalsIgnoreCase("NONE")) {
				return "null";
			} else {
				scoreboard = defaultScoreboard;
			}
		}
		ScoreboardImpl board = scoreboards.get(scoreboard);
		while (board != null && !board.isConditionMet(p)) {
			board = scoreboards.get(board.getChildScoreboard());
			if (board == null) return "null";
			scoreboard = board.getName();
		}
		return scoreboard;
	}

	@Override
	public boolean onCommand(TabPlayer sender, String message) {
		if (playersInDisabledWorlds.contains(sender)) return false;
		if (message.equals(toggleCommand) || message.startsWith(toggleCommand+" ")) {
			tab.getCommand().execute(sender, message.replace(toggleCommand,"scoreboard").split(" "));
			return true;
		}
		return false;
	}

	/**
	 * Returns map of currently defined scoreboards
	 * @return map of currently defined scoreboards
	 */
	public Map<String, ScoreboardImpl> getScoreboards(){
		return scoreboards;
	}

	@Override
	public TabFeature getFeatureType() {
		return TabFeature.SCOREBOARD;
	}

	@Override
	public boolean onPacketSend(TabPlayer receiver, PacketPlayOutScoreboardDisplayObjective packet) {
		if (respectOtherPlugins && packet.getSlot() == DISPLAY_SLOT && !packet.getObjectiveName().equals(OBJECTIVE_NAME)) {
			tab.debug("Player " + receiver.getName() + " received scoreboard called " + packet.getObjectiveName() + ", hiding TAB one.");
			((ITabPlayer)receiver).setOtherPluginScoreboard(packet.getObjectiveName());
			if (receiver.getActiveScoreboard() != null) {
				tab.getCPUManager().runMeasuredTask("sending packets", TabFeature.SCOREBOARD, UsageType.ANTI_OVERRIDE, () -> receiver.getActiveScoreboard().unregister(receiver));
			}
		}
		return false;
	}

	@Override
	public void onPacketSend(TabPlayer receiver, PacketPlayOutScoreboardObjective packet) {
		if (respectOtherPlugins && packet.getMethod() == 1 && ((ITabPlayer)receiver).getOtherPluginScoreboard() != null && ((ITabPlayer)receiver).getOtherPluginScoreboard().equals(packet.getObjectiveName())) {
			tab.debug("Player " + receiver.getName() + " no longer has another scoreboard, sending TAB one.");
			((ITabPlayer)receiver).setOtherPluginScoreboard(null);
			tab.getCPUManager().runMeasuredTask("sending packets", TabFeature.SCOREBOARD, UsageType.ANTI_OVERRIDE, () -> sendHighestScoreboard(receiver));
		}
	}

	public boolean isRememberToggleChoice() {
		return rememberToggleChoice;
	}

	public boolean isHiddenByDefault() {
		return hiddenByDefault;
	}

	public Set<String> getSbOffPlayers() {
		return sbOffPlayers;
	}

	public String getScoreboardOn() {
		return scoreboardOn;
	}

	public String getScoreboardOff() {
		return scoreboardOff;
	}

	public boolean isUseNumbers() {
		return useNumbers;
	}

	public int getStaticNumber() {
		return staticNumber;
	}

	public boolean isPermToToggle() {
		return permToToggle;
	}

	public List<me.neznamy.tab.api.Scoreboard> getApiScoreboards() {
		return apiScoreboards;
	}

	public me.neznamy.tab.api.Scoreboard getAnnouncement() {
		return announcement;
	}

	public void setAnnouncement(me.neznamy.tab.api.Scoreboard announcement) {
		this.announcement = announcement;
	}
}