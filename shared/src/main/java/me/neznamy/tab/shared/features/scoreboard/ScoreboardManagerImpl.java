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
import me.neznamy.tab.api.scoreboard.Scoreboard;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.UsageType;
import me.neznamy.tab.shared.features.TabFeature;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective;

/**
 * Feature handler for scoreboard feature
 */
public class ScoreboardManagerImpl extends TabFeature implements ScoreboardManager {

	public static final String OBJECTIVE_NAME = "TAB-Scoreboard";
	public static final int DISPLAY_SLOT = 1;

	//toggle command
	private String toggleCommand;
	
	//list of disabled worlds/servers
	private List<String> disabledWorlds;
	
	//default scoreboard
	private String defaultScoreboard;
	
	//per-world / per-server scoreboards
	private Map<String, String> perWorld;
	
	//defined scoreboards
	private Map<String, Scoreboard> scoreboards = new HashMap<>();
	
	//using 1-15
	private boolean useNumbers;
	
	//saving toggle choice into file
	private boolean rememberToggleChoice;
	
	//list of players with disabled scoreboard
	private Set<String> sbOffPlayers = new HashSet<>();
	
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
	private Scoreboard announcement;
	
	//config option someone requested
	private int joinDelay;
	private List<TabPlayer> joinDelayed = new ArrayList<>();
	
	private Set<TabPlayer> playersInDisabledWorlds = new HashSet<>();
	
	private Map<TabPlayer, Scoreboard> forcedScoreboard = new HashMap<>();
	
	private Map<TabPlayer, Scoreboard> activeScoreboard = new HashMap<>();
	
	private Set<TabPlayer> visiblePlayers = new HashSet<>();
	
	private Map<TabPlayer, String> otherPluginScoreboard = new HashMap<>();

	/**
	 * Constructs new instance and loads configuration
	 * @param tab - tab instance
	 */
	public ScoreboardManagerImpl() {
		toggleCommand = TAB.getInstance().getConfiguration().getPremiumConfig().getString("scoreboard.toggle-command", "/sb");
		useNumbers = TAB.getInstance().getConfiguration().getPremiumConfig().getBoolean("scoreboard.use-numbers", false);
		permToToggle = TAB.getInstance().getConfiguration().getPremiumConfig().getBoolean("scoreboard.permission-required-to-toggle", false);
		disabledWorlds = TAB.getInstance().getConfiguration().getPremiumConfig().getStringList("scoreboard.disable-in-worlds", Arrays.asList("disabledworld"));
		defaultScoreboard = TAB.getInstance().getConfiguration().getPremiumConfig().getString("scoreboard.default-scoreboard", "MyDefaultScoreboard");
		perWorld = TAB.getInstance().getConfiguration().getPremiumConfig().getConfigurationSection("scoreboard.per-world");
		rememberToggleChoice = TAB.getInstance().getConfiguration().getPremiumConfig().getBoolean("scoreboard.remember-toggle-choice", false);
		hiddenByDefault = TAB.getInstance().getConfiguration().getPremiumConfig().getBoolean("scoreboard.hidden-by-default", false);
		scoreboardOn = TAB.getInstance().getConfiguration().getPremiumConfig().getString("scoreboard-on", "&2Scorebord enabled");
		scoreboardOff = TAB.getInstance().getConfiguration().getPremiumConfig().getString("scoreboard-off", "&7Scoreboard disabled");
		if (rememberToggleChoice) {
			sbOffPlayers = Collections.synchronizedSet(new HashSet<>(TAB.getInstance().getConfiguration().getPlayerData("scoreboard-off")));
		}
		staticNumber = TAB.getInstance().getConfiguration().getPremiumConfig().getInt("scoreboard.static-number", 0);
		joinDelay = TAB.getInstance().getConfiguration().getPremiumConfig().getInt("scoreboard.delay-on-join-milliseconds", 0);

		for (Object scoreboard : TAB.getInstance().getConfiguration().getPremiumConfig().getConfigurationSection("scoreboards").keySet()) {
			String condition = TAB.getInstance().getConfiguration().getPremiumConfig().getString("scoreboards." + scoreboard + ".display-condition");
			String childBoard = TAB.getInstance().getConfiguration().getPremiumConfig().getString("scoreboards." + scoreboard + ".if-condition-not-met");
			String title = TAB.getInstance().getConfiguration().getPremiumConfig().getString("scoreboards." + scoreboard + ".title");
			if (title == null) {
				title = "<Title not defined>";
				TAB.getInstance().getErrorManager().missingAttribute("Scoreboard", scoreboard, "title");
			}
			List<String> lines = TAB.getInstance().getConfiguration().getPremiumConfig().getStringList("scoreboards." + scoreboard + ".lines");
			if (lines == null) {
				lines = Arrays.asList("scoreboard \"" + scoreboard +"\" is missing \"lines\" keyword!", "did you forget to configure it or just your spacing is wrong?");
				TAB.getInstance().getErrorManager().missingAttribute("Scoreboard", scoreboard, "lines");
			}
			ScoreboardImpl sb = new ScoreboardImpl(this, scoreboard.toString(), title, lines, condition, childBoard);
			scoreboards.put(scoreboard.toString(), sb);
			TAB.getInstance().getFeatureManager().registerFeature("scoreboard-" + scoreboard, sb);
		}
		checkForMisconfiguration();
		TAB.getInstance().debug(String.format("Loaded Scoreboard feature with parameters toggleCommand=%s, useNumbers=%s, permToToggle=%s, disabledWorlds=%s"
				+ ", defaultScoreboard=%s, perWorld=%s, rememberToggleChoice=%s, hiddenByDefault=%s, scoreboard_on=%s, scoreboard_off=%s, staticNumber=%s, joinDelay=%s",
				toggleCommand, useNumbers, permToToggle, disabledWorlds, defaultScoreboard, perWorld, rememberToggleChoice, hiddenByDefault, scoreboardOn, scoreboardOff, staticNumber, joinDelay));
	}

	/**
	 * Checks for misconfiguration and sends console warns if anything was found
	 */
	private void checkForMisconfiguration() {
		if (!defaultScoreboard.equalsIgnoreCase("NONE") && !scoreboards.containsKey(defaultScoreboard)) {
			TAB.getInstance().getErrorManager().startupWarn("Unknown scoreboard &e\"" + defaultScoreboard + "\"&c set as default scoreboard.");
			defaultScoreboard = "NONE";
		}
		for (Entry<String, String> entry : perWorld.entrySet()) {
			if (!scoreboards.containsKey(entry.getValue())) {
				TAB.getInstance().getErrorManager().startupWarn("Unknown scoreboard &e\"" + entry.getValue() + "\"&c set as per-world scoreboard in world &e\"" + entry.getKey() + "\"&c.");
			}
		}
		for (Scoreboard scoreboard : scoreboards.values()) {
			if (((ScoreboardImpl) scoreboard).getChildScoreboard() != null && !scoreboards.containsKey(((ScoreboardImpl) scoreboard).getChildScoreboard())) {
				TAB.getInstance().getErrorManager().startupWarn("Unknown scoreboard &e\"" + ((ScoreboardImpl) scoreboard).getChildScoreboard() + "\"&c set as if-condition-not-met of scoreboard &e\"" + scoreboard.getName() + "\"&c.");
			}
		}
	}

	@Override
	public void load() {
		for (TabPlayer p : TAB.getInstance().getPlayers()) {
			if (isDisabledWorld(disabledWorlds, p.getWorldName())) {
				playersInDisabledWorlds.add(p);
				return;
			}
			setScoreboardVisible(p, hiddenByDefault == sbOffPlayers.contains(p.getName()), false);
		}
		TAB.getInstance().getCPUManager().startRepeatingMeasuredTask(1000, "refreshing scoreboard conditions", getFeatureType(), UsageType.REPEATING_TASK, () -> {

			for (TabPlayer p : TAB.getInstance().getPlayers()) {
				if (!p.isLoaded() || forcedScoreboard.containsKey(p) || !hasScoreboardVisible(p) || 
					announcement != null || otherPluginScoreboard.containsKey(p) || joinDelayed.contains(p)) continue;
				Scoreboard board = activeScoreboard.get(p);
				String current = board == null ? "null" : board.getName();
				String highest = detectHighestScoreboard(p);
				if (!current.equals(highest)) {
					if (activeScoreboard.containsKey(p)) activeScoreboard.get(p).removePlayer(p);
					sendHighestScoreboard(p);
				}
			}
		});
	}

	@Override
	public void unload() {
		for (Scoreboard board : scoreboards.values()) {
			((ScoreboardImpl)board).unregister();
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
			TAB.getInstance().getCPUManager().runTaskLater(joinDelay, "processing player join", getFeatureType(), UsageType.PLAYER_JOIN_EVENT, () -> {
				
				if (!otherPluginScoreboard.containsKey(connectedPlayer)) setScoreboardVisible(connectedPlayer, hiddenByDefault == sbOffPlayers.contains(connectedPlayer.getName()), false);
				joinDelayed.remove(connectedPlayer);
			});
		} else {
			setScoreboardVisible(connectedPlayer, hiddenByDefault == sbOffPlayers.contains(connectedPlayer.getName()), false);
		}
	}

	/**
	 * Sends the player scoreboard he should see according to conditions and worlds
	 * @param p - player to send scoreboard to
	 */
	public void sendHighestScoreboard(TabPlayer p) {
		if (playersInDisabledWorlds.contains(p) || !hasScoreboardVisible(p)) return;
		String scoreboard = detectHighestScoreboard(p);
		if (scoreboard != null) {
			Scoreboard board = scoreboards.get(scoreboard);
			if (board != null) {
				activeScoreboard.put(p, board);
				board.addPlayer(p);
			}
		}
	}

	@Override
	public void onQuit(TabPlayer p) {
		unregisterScoreboard(p, false);
		playersInDisabledWorlds.remove(p);
		forcedScoreboard.remove(p);
		activeScoreboard.remove(p);
		visiblePlayers.remove(p);
		otherPluginScoreboard.remove(p);
	}

	/**
	 * Removes this player from registered users in scoreboard and sends unregister packets if set
	 * @param p - player to unregister scoreboard to
	 * @param sendUnregisterPacket - if unregister packets should be sent or not
	 */
	public void unregisterScoreboard(TabPlayer p, boolean sendUnregisterPacket) {
		if (activeScoreboard.containsKey(p)) {
			if (sendUnregisterPacket) {
				activeScoreboard.get(p).removePlayer(p);
			} else {
				activeScoreboard.get(p).getPlayers().remove(p);
			}
			activeScoreboard.remove(p);
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

	public boolean isUsingNumbers() {
		return useNumbers;
	}
	
	public int getStaticNumber() {
		return staticNumber;
	}
	
	/**
	 * Returns currently highest scoreboard in chain for specified player
	 * @param p - player to check
	 * @return highest scoreboard player should see
	 */
	public String detectHighestScoreboard(TabPlayer p) {
		String scoreboard = perWorld.get(TAB.getInstance().getConfiguration().getWorldGroupOf(perWorld.keySet(), p.getWorldName()));
		if (scoreboard == null) {
			if (defaultScoreboard.equalsIgnoreCase("NONE")) {
				return "null";
			} else {
				scoreboard = defaultScoreboard;
			}
		}
		ScoreboardImpl board = (ScoreboardImpl) scoreboards.get(scoreboard);
		while (board != null && !board.isConditionMet(p)) {
			board = (ScoreboardImpl) scoreboards.get(board.getChildScoreboard());
			if (board == null) return "null";
			scoreboard = board.getName();
		}
		return scoreboard;
	}

	@Override
	public boolean onCommand(TabPlayer sender, String message) {
		if (playersInDisabledWorlds.contains(sender)) return false;
		if (message.equals(toggleCommand) || message.startsWith(toggleCommand+" ")) {
			TAB.getInstance().getCommand().execute(sender, message.replace(toggleCommand,"scoreboard").split(" "));
			return true;
		}
		return false;
	}

	@Override
	public String getFeatureType() {
		return "Scoreboard";
	}

	@Override
	public boolean onPacketSend(TabPlayer receiver, PacketPlayOutScoreboardDisplayObjective packet) {
		if (packet.getSlot() == DISPLAY_SLOT && !packet.getObjectiveName().equals(OBJECTIVE_NAME)) {
			TAB.getInstance().debug("Player " + receiver.getName() + " received scoreboard called " + packet.getObjectiveName() + ", hiding TAB one.");
			otherPluginScoreboard.put(receiver, packet.getObjectiveName());
			if (activeScoreboard.containsKey(receiver)) {
				TAB.getInstance().getCPUManager().runMeasuredTask("sending packets", getFeatureType(), UsageType.ANTI_OVERRIDE, () -> activeScoreboard.get(receiver).removePlayer(receiver));
			}
		}
		return false;
	}

	@Override
	public void onPacketSend(TabPlayer receiver, PacketPlayOutScoreboardObjective packet) {
		if (packet.getMethod() == 1 && otherPluginScoreboard.containsKey(receiver) && otherPluginScoreboard.get(receiver).equals(packet.getObjectiveName())) {
			TAB.getInstance().debug("Player " + receiver.getName() + " no longer has another scoreboard, sending TAB one.");
			otherPluginScoreboard.remove(receiver);
			TAB.getInstance().getCPUManager().runMeasuredTask("sending packets", getFeatureType(), UsageType.ANTI_OVERRIDE, () -> sendHighestScoreboard(receiver));
		}
	}

	@Override
	public Scoreboard createScoreboard(String name, String title, List<String> lines) {
		Scoreboard sb = new ScoreboardImpl(this, name, title, lines);
		scoreboards.put(name, sb);
		return sb;
	}

	@Override
	public Map<String, Scoreboard> getRegisteredScoreboards() {
		return scoreboards;
	}

	@Override
	public void showScoreboard(TabPlayer player, String scoreboard) {
		Scoreboard sb = scoreboards.get(scoreboard);
		if (scoreboard == null) throw new IllegalArgumentException("No scoreboard found with name " + scoreboard);
		showScoreboard(player, sb);
	}

	@Override
	public void showScoreboard(TabPlayer player, Scoreboard scoreboard) {
		if (forcedScoreboard.containsKey(player)) {
			forcedScoreboard.get(player).removePlayer(player);
		}
		if (activeScoreboard.containsKey(player)) {
			activeScoreboard.get(player).removePlayer(player);
			activeScoreboard.remove(player);
		}
		forcedScoreboard.put(player, scoreboard);
		scoreboard.addPlayer(player);
	}

	@Override
	public void hideCustomScoreboard(TabPlayer player) {
		if (!forcedScoreboard.containsKey(player)) return;
		Scoreboard sb = scoreboards.get(detectHighestScoreboard(player));
		if (sb == null) return; //no scoreboard available
		activeScoreboard.put(player, sb);
		sb.addPlayer(player);
		forcedScoreboard.remove(player);
	}

	@Override
	public boolean hasScoreboardVisible(TabPlayer player) {
		return visiblePlayers.contains(player);
	}

	@Override
	public boolean hasCustomScoreboard(TabPlayer player) {
		return forcedScoreboard.containsKey(player);
	}

	@Override
	public void setScoreboardVisible(TabPlayer player, boolean visible, boolean sendToggleMessage) {
		if (visiblePlayers.contains(player) == visible) return;
		if (visible) {
			visiblePlayers.add(player);
			sendHighestScoreboard(player);
			if (sendToggleMessage) {
				player.sendMessage(scoreboardOn, true);
			}
			if (rememberToggleChoice) {
				if (hiddenByDefault) {
					sbOffPlayers.add(player.getName());
				} else {
					sbOffPlayers.remove(player.getName());
				}
				synchronized (sbOffPlayers){
					TAB.getInstance().getConfiguration().getPlayerDataFile().set("scoreboard-off", new ArrayList<>(sbOffPlayers));
				}
			}
		} else {
			visiblePlayers.remove(player);
			unregisterScoreboard(player, true);
			if (sendToggleMessage) {
				player.sendMessage(scoreboardOff, true);
			}
			if (rememberToggleChoice) {
				if (hiddenByDefault) {
					sbOffPlayers.remove(player.getName());
				} else {
					sbOffPlayers.add(player.getName());
				}
				synchronized (sbOffPlayers){
					TAB.getInstance().getConfiguration().getPlayerDataFile().set("scoreboard-off", new ArrayList<>(sbOffPlayers));
				}
			}
		}
	}

	@Override
	public void toggleScoreboard(TabPlayer player, boolean sendToggleMessage) {
		setScoreboardVisible(player, !visiblePlayers.contains(player), sendToggleMessage);
	}
	
	public Map<TabPlayer, Scoreboard> getActiveScoreboards(){
		return activeScoreboard;
	}
	
	public Map<TabPlayer, String> getOtherPluginScoreboards(){
		return otherPluginScoreboard;
	}
	
	public boolean requiresPermissionToToggle() {
		return permToToggle;
	}

	@Override
	public void announceScoreboard(String scoreboard, int duration) {
		Scoreboard sb = scoreboards.get(scoreboard);
		if (sb == null) throw new IllegalArgumentException("No registered scoreboard found with name " + scoreboard);
		new Thread(() -> {
			try {
				announcement = sb;
				Map<TabPlayer, Scoreboard> previous = new HashMap<>();
				for (TabPlayer all : TAB.getInstance().getPlayers()) {
					if (!hasScoreboardVisible(all)) continue;
					previous.put(all, activeScoreboard.get(all));
					if (activeScoreboard.containsKey(all)) activeScoreboard.get(all).removePlayer(all);
					sb.addPlayer(all);
				}
				Thread.sleep(duration*1000L);
				for (TabPlayer all : TAB.getInstance().getPlayers()) {
					if (!hasScoreboardVisible(all)) continue;
					sb.removePlayer(all);
					if (previous.get(all) != null) previous.get(all).addPlayer(all);
				}
				announcement = null;
			} catch (InterruptedException pluginDisabled) {
				Thread.currentThread().interrupt();
			}
		}).start();
	}
}