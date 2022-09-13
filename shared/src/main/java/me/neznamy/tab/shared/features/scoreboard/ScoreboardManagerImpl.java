package me.neznamy.tab.shared.features.scoreboard;

import java.util.*;
import java.util.Map.Entry;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.api.protocol.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.api.scoreboard.Scoreboard;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PipelineInjector;
import me.neznamy.tab.shared.features.TabExpansion;

/**
 * Feature handler for scoreboard feature
 */
public class ScoreboardManagerImpl extends TabFeature implements ScoreboardManager {

    public static final String OBJECTIVE_NAME = "TAB-Scoreboard";
    public static final int DISPLAY_SLOT = 1;

    //config options
    private final String toggleCommand = TAB.getInstance().getConfiguration().getConfig().getString("scoreboard.toggle-command", "/sb");
    private final boolean useNumbers = TAB.getInstance().getConfiguration().getConfig().getBoolean("scoreboard.use-numbers", false);
    private final boolean rememberToggleChoice = TAB.getInstance().getConfiguration().getConfig().getBoolean("scoreboard.remember-toggle-choice", false);
    private final boolean hiddenByDefault = TAB.getInstance().getConfiguration().getConfig().getBoolean("scoreboard.hidden-by-default", false);
    private final boolean respectOtherPlugins = TAB.getInstance().getConfiguration().getConfig().getBoolean("scoreboard.respect-other-plugins", true);
    private final int staticNumber = TAB.getInstance().getConfiguration().getConfig().getInt("scoreboard.static-number", 0);
    private final int joinDelay = TAB.getInstance().getConfiguration().getConfig().getInt("scoreboard.delay-on-join-milliseconds", 0);

    //defined scoreboards
    private final Map<String, Scoreboard> scoreboards = new LinkedHashMap<>();
    private Scoreboard[] definedScoreboards;

    //toggle messages
    private final String scoreboardOn = TAB.getInstance().getConfiguration().getMessages().getScoreboardOn();
    private final String scoreboardOff = TAB.getInstance().getConfiguration().getMessages().getScoreboardOff();

    //list of players with disabled scoreboard
    private final List<String> sbOffPlayers;

    //active scoreboard announcement
    private Scoreboard announcement;

    private final Set<TabPlayer> joinDelayed = Collections.newSetFromMap(new WeakHashMap<>());
    private final WeakHashMap<TabPlayer, ScoreboardImpl> forcedScoreboard = new WeakHashMap<>();
    private final WeakHashMap<TabPlayer, ScoreboardImpl> activeScoreboard = new WeakHashMap<>();
    private final Set<TabPlayer> visiblePlayers = Collections.newSetFromMap(new WeakHashMap<>());
    private final WeakHashMap<TabPlayer, String> otherPluginScoreboard = new WeakHashMap<>();

    /**
     * Constructs new instance and loads configuration
     */
    @SuppressWarnings("unchecked")
    public ScoreboardManagerImpl() {
        super("Scoreboard", "Switching displayed scoreboard", "scoreboard");
        if (rememberToggleChoice) {
            sbOffPlayers = Collections.synchronizedList(new ArrayList<>(TAB.getInstance().getConfiguration().getPlayerDataFile().getStringList("scoreboard-off", new ArrayList<>())));
        } else {
            sbOffPlayers = Collections.emptyList();
        }
        Map<String, Map<String, Object>> map = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("scoreboard.scoreboards");
        for (Entry<String, Map<String, Object>> entry : map.entrySet()) {
            String condition = (String) entry.getValue().get("display-condition");
            String title = (String) entry.getValue().get("title");
            if (title == null) {
                title = "<Title not defined>";
                TAB.getInstance().getErrorManager().missingAttribute(getFeatureName(), entry.getKey(), "title");
            }
            List<String> lines = (List<String>) entry.getValue().get("lines");
            if (lines == null) {
                lines = Arrays.asList("scoreboard \"" + entry.getKey() +"\" is missing \"lines\" keyword!", "did you forget to configure it or just your spacing is wrong?");
                TAB.getInstance().getErrorManager().missingAttribute(getFeatureName(), entry.getKey(), "lines");
            }
            ScoreboardImpl sb = new ScoreboardImpl(this, entry.getKey(), title, lines, condition);
            scoreboards.put(entry.getKey(), sb);
            TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.scoreboardLine(entry.getKey()), sb);
        }
        definedScoreboards = scoreboards.values().toArray(new Scoreboard[0]);
        if (respectOtherPlugins) {
            PipelineInjector inj = (PipelineInjector) TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.PIPELINE_INJECTION);
            //null check if injection was disabled in config or velocity
            if (inj != null) inj.setByteBufDeserialization(true);
        }
        TAB.getInstance().debug(String.format("Loaded Scoreboard feature with parameters toggleCommand=%s, useNumbers=%s, disabledWorlds=%s"
                + ", disabledServers=%s, rememberToggleChoice=%s, hiddenByDefault=%s, scoreboard_on=%s, scoreboard_off=%s, staticNumber=%s, joinDelay=%s",
                toggleCommand, useNumbers, Arrays.toString(disabledWorlds), Arrays.toString(disabledServers), rememberToggleChoice, hiddenByDefault, scoreboardOn, scoreboardOff, staticNumber, joinDelay));
    }

    @Override
    public void load() {
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            onJoin(p);
        }
    }

    @Override
    public void refresh(TabPlayer p, boolean force) {
        if (forcedScoreboard.containsKey(p) || !hasScoreboardVisible(p) ||
                announcement != null || otherPluginScoreboard.containsKey(p) || joinDelayed.contains(p)) return;
        sendHighestScoreboard(p);
    }

    @Override
    public void unload() {
        for (Scoreboard board : definedScoreboards) {
            board.unregister();
        }
    }

    @Override
    public void onJoin(TabPlayer connectedPlayer) {
        if (isDisabled(connectedPlayer.getServer(), connectedPlayer.getWorld())) {
            if (hiddenByDefault == sbOffPlayers.contains(connectedPlayer.getName())) visiblePlayers.add(connectedPlayer);
            addDisabledPlayer(connectedPlayer);
            return;
        }
        if (joinDelay > 0) {
            joinDelayed.add(connectedPlayer);
            TAB.getInstance().getCPUManager().runTaskLater(joinDelay, this, TabConstants.CpuUsageCategory.PLAYER_JOIN, () -> {

                if (!otherPluginScoreboard.containsKey(connectedPlayer)) setScoreboardVisible(connectedPlayer, hiddenByDefault == sbOffPlayers.contains(connectedPlayer.getName()), false);
                joinDelayed.remove(connectedPlayer);
            });
        } else {
            setScoreboardVisible(connectedPlayer, hiddenByDefault == sbOffPlayers.contains(connectedPlayer.getName()), false);
        }
    }

    /**
     * Sends the player scoreboard he should see according to conditions and worlds
     *
     * @param   p
     *          player to send scoreboard to
     */
    public void sendHighestScoreboard(TabPlayer p) {
        if (isDisabledPlayer(p) || !hasScoreboardVisible(p)) return;
        ScoreboardImpl scoreboard = (ScoreboardImpl) detectHighestScoreboard(p);
        ScoreboardImpl current = activeScoreboard.get(p);
        if (scoreboard != current) {
            if (current != null) {
                current.removePlayer(p);
            }
            if (scoreboard != null) {
                scoreboard.addPlayer(p);
            }
        }
    }

    /**
     * Removes this player from registered users in scoreboard and sends unregister packets if set
     *
     * @param   p
     *          player to unregister scoreboard to
     */
    public void unregisterScoreboard(TabPlayer p) {
        if (activeScoreboard.containsKey(p)) {
            activeScoreboard.get(p).removePlayer(p);
            activeScoreboard.remove(p);
        }
    }

    @Override
    public void onServerChange(TabPlayer p, String from, String to) {
        onWorldChange(p, null, null);
    }

    @Override
    public void onWorldChange(TabPlayer p, String from, String to) {
        boolean disabledBefore = isDisabledPlayer(p);
        if (isDisabled(p.getServer(), p.getWorld())) {
            addDisabledPlayer(p);
            if (!disabledBefore) {
                unregisterScoreboard(p);
            }
        } else {
            removeDisabledPlayer(p);
            if (disabledBefore) {
                sendHighestScoreboard(p);
            }
        }
    }

    public boolean isUsingNumbers() {
        return useNumbers;
    }

    public int getStaticNumber() {
        return staticNumber;
    }

    /**
     * Returns currently the highest scoreboard in chain for specified player
     *
     * @param   p
     *          player to check
     * @return  highest scoreboard player should see
     */
    public Scoreboard detectHighestScoreboard(TabPlayer p) {
        if (forcedScoreboard.containsKey(p)) return forcedScoreboard.get(p);
        for (Scoreboard sb : definedScoreboards) {
            if (((ScoreboardImpl)sb).isConditionMet(p)) return sb;
        }
        return null;
    }

    @Override
    public boolean onCommand(TabPlayer sender, String message) {
        if (isDisabledPlayer(sender)) return false;
        if (message.equals(toggleCommand) || message.startsWith(toggleCommand+" ")) {
            TAB.getInstance().getCommand().execute(sender, message.replace(toggleCommand, "scoreboard").split(" "));
            return true;
        }
        return false;
    }

    @Override
    public void onDisplayObjective(TabPlayer receiver, PacketPlayOutScoreboardDisplayObjective packet) {
        if (respectOtherPlugins && packet.getSlot() == DISPLAY_SLOT && !packet.getObjectiveName().equals(OBJECTIVE_NAME)) {
            TAB.getInstance().debug("Player " + receiver.getName() + " received scoreboard called " + packet.getObjectiveName() + ", hiding TAB one.");
            otherPluginScoreboard.put(receiver, packet.getObjectiveName());
            ScoreboardImpl sb = activeScoreboard.get(receiver);
            if (sb != null) {
                TAB.getInstance().getCPUManager().runMeasuredTask(this, TabConstants.CpuUsageCategory.SCOREBOARD_PACKET_CHECK, () -> sb.removePlayer(receiver));
            }
        }
    }

    @Override
    public void onObjective(TabPlayer receiver, PacketPlayOutScoreboardObjective packet) {
        if (respectOtherPlugins && packet.getAction() == 1 && otherPluginScoreboard.containsKey(receiver) && otherPluginScoreboard.get(receiver).equals(packet.getObjectiveName())) {
            TAB.getInstance().debug("Player " + receiver.getName() + " no longer has another scoreboard, sending TAB one.");
            otherPluginScoreboard.remove(receiver);
            TAB.getInstance().getCPUManager().runMeasuredTask(this, TabConstants.CpuUsageCategory.SCOREBOARD_PACKET_CHECK, () -> sendHighestScoreboard(receiver));
        }
    }

    @Override
    public Scoreboard createScoreboard(String name, String title, List<String> lines) {
        if (name == null) throw new IllegalArgumentException("name cannot be null");
        if (title == null) throw new IllegalArgumentException("title cannot be null");
        if (lines == null) throw new IllegalArgumentException("lines cannot be null");
        Scoreboard sb = new ScoreboardImpl(this, name, title, lines, true);
        scoreboards.put(name, sb);
        definedScoreboards = scoreboards.values().toArray(new Scoreboard[0]);
        return sb;
    }

    @Override
    public Map<String, Scoreboard> getRegisteredScoreboards() {
        return scoreboards;
    }

    @Override
    public void showScoreboard(TabPlayer player, Scoreboard scoreboard) {
        if (player == null) throw new IllegalArgumentException("player cannot be null");
        if (scoreboard == null) throw new IllegalArgumentException("scoreboard cannot be null");
        if (forcedScoreboard.containsKey(player)) {
            forcedScoreboard.get(player).removePlayer(player);
        }
        if (activeScoreboard.containsKey(player)) {
            activeScoreboard.get(player).removePlayer(player);
            activeScoreboard.remove(player);
        }
        forcedScoreboard.put(player, (ScoreboardImpl) scoreboard);
        if (hasScoreboardVisible(player)) ((ScoreboardImpl) scoreboard).addPlayer(player);
    }

    @Override
    public void resetScoreboard(TabPlayer player) {
        if (!forcedScoreboard.containsKey(player)) return;
        forcedScoreboard.get(player).removePlayer(player);
        forcedScoreboard.remove(player);
        Scoreboard sb = detectHighestScoreboard(player);
        if (sb == null) return; //no scoreboard available
        activeScoreboard.put(player, (ScoreboardImpl) sb);
        ((ScoreboardImpl) sb).addPlayer(player);
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
                    if (!sbOffPlayers.contains(player.getName())) sbOffPlayers.add(player.getName());
                } else {
                    sbOffPlayers.remove(player.getName());
                }
                synchronized (sbOffPlayers){
                    TAB.getInstance().getConfiguration().getPlayerDataFile().set("scoreboard-off", new ArrayList<>(sbOffPlayers));
                }
            }
        } else {
            visiblePlayers.remove(player);
            unregisterScoreboard(player);
            if (sendToggleMessage) {
                player.sendMessage(scoreboardOff, true);
            }
            if (rememberToggleChoice) {
                if (hiddenByDefault) {
                    sbOffPlayers.remove(player.getName());
                } else {
                    if (!sbOffPlayers.contains(player.getName())) sbOffPlayers.add(player.getName());
                }
                synchronized (sbOffPlayers){
                    TAB.getInstance().getConfiguration().getPlayerDataFile().set("scoreboard-off", new ArrayList<>(sbOffPlayers));
                }
            }
        }
        TabExpansion expansion = TAB.getInstance().getPlaceholderManager().getTabExpansion();
        if (expansion != null) expansion.setScoreboardVisible(player, visible);
    }

    @Override
    public void toggleScoreboard(TabPlayer player, boolean sendToggleMessage) {
        setScoreboardVisible(player, !visiblePlayers.contains(player), sendToggleMessage);
    }

    public Map<TabPlayer, ScoreboardImpl> getActiveScoreboards(){
        return activeScoreboard;
    }

    public Map<TabPlayer, String> getOtherPluginScoreboards(){
        return otherPluginScoreboard;
    }

    @Override
    public void announceScoreboard(String scoreboard, int duration) {
        if (duration < 0) throw new IllegalArgumentException("Duration cannot be negative");
        ScoreboardImpl sb = (ScoreboardImpl) scoreboards.get(scoreboard);
        if (sb == null) throw new IllegalArgumentException("No registered scoreboard found with name " + scoreboard);
        Map<TabPlayer, ScoreboardImpl> previous = new HashMap<>();
        TAB.getInstance().getCPUManager().runTask(() -> {
            announcement = sb;
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (!hasScoreboardVisible(all)) continue;
                previous.put(all, activeScoreboard.get(all));
                if (activeScoreboard.containsKey(all)) activeScoreboard.get(all).removePlayer(all);
                sb.addPlayer(all);
            }
        });
        TAB.getInstance().getCPUManager().runTaskLater(duration*1000,
                this, "Removing announced Scoreboard", () -> {
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (!hasScoreboardVisible(all)) continue;
                sb.removePlayer(all);
                if (previous.get(all) != null) previous.get(all).addPlayer(all);
            }
            announcement = null;
        });
    }

    @Override
    public Scoreboard getActiveScoreboard(TabPlayer player) {
        return activeScoreboard.get(player);
    }

    @Override
    public void onLoginPacket(TabPlayer packetReceiver) {
        otherPluginScoreboard.remove(packetReceiver);
        ScoreboardImpl scoreboard = activeScoreboard.get(packetReceiver);
        if (scoreboard != null) {
            scoreboard.removePlayer(packetReceiver);
            scoreboard.addPlayer(packetReceiver);
        }
    }
}