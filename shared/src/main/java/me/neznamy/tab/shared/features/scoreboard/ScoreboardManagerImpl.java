package me.neznamy.tab.shared.features.scoreboard;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.types.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;

/**
 * Feature handler for scoreboard feature
 */
public class ScoreboardManagerImpl extends TabFeature implements ScoreboardManager, JoinListener,
        CommandListener, DisplayObjectiveListener, ObjectiveListener, Loadable, UnLoadable, Refreshable,
        ServerSwitchListener {

    public static final String OBJECTIVE_NAME = "TAB-Scoreboard";

    //config options
    @Getter private final String toggleCommand = TAB.getInstance().getConfiguration().getConfig().getString("scoreboard.toggle-command", "/sb");
    @Getter private final boolean usingNumbers = TAB.getInstance().getConfiguration().getConfig().getBoolean("scoreboard.use-numbers", false);
    private final boolean rememberToggleChoice = TAB.getInstance().getConfiguration().getConfig().getBoolean("scoreboard.remember-toggle-choice", false);
    private final boolean hiddenByDefault = TAB.getInstance().getConfiguration().getConfig().getBoolean("scoreboard.hidden-by-default", false);
    private final boolean respectOtherPlugins = TAB.getInstance().getConfiguration().getConfig().getBoolean("scoreboard.respect-other-plugins", true);
    @Getter private final int staticNumber = TAB.getInstance().getConfiguration().getConfig().getInt("scoreboard.static-number", 0);
    private final int joinDelay = TAB.getInstance().getConfiguration().getConfig().getInt("scoreboard.delay-on-join-milliseconds", 0);

    //defined scoreboards
    @Getter private final Map<String, me.neznamy.tab.api.scoreboard.Scoreboard> registeredScoreboards = new LinkedHashMap<>();
    private me.neznamy.tab.api.scoreboard.Scoreboard[] definedScoreboards;

    //list of players with disabled scoreboard
    private final List<String> sbOffPlayers = rememberToggleChoice ? TAB.getInstance().getConfiguration().getPlayerDataFile()
            .getStringList("scoreboard-off", new ArrayList<>()) : Collections.emptyList();

    //active scoreboard announcement
    private me.neznamy.tab.api.scoreboard.Scoreboard announcement;

    private final Set<TabPlayer> joinDelayed = Collections.newSetFromMap(new WeakHashMap<>());
    private final WeakHashMap<me.neznamy.tab.api.TabPlayer, ScoreboardImpl> forcedScoreboard = new WeakHashMap<>();
    @Getter private final WeakHashMap<me.neznamy.tab.api.TabPlayer, ScoreboardImpl> activeScoreboards = new WeakHashMap<>();
    private final Set<me.neznamy.tab.api.TabPlayer> visiblePlayers = Collections.newSetFromMap(new WeakHashMap<>());
    @Getter private final WeakHashMap<TabPlayer, String> otherPluginScoreboards = new WeakHashMap<>();
    @Getter private final String featureName = "Scoreboard";
    @Getter private final String refreshDisplayName = "Switching scoreboards";

    @Override
    public void load() {
        Map<String, Map<String, Object>> map = TAB.getInstance().getConfiguration().getConfig().getConfigurationSection("scoreboard.scoreboards");
        boolean noConditionScoreboardFound = false;
        String noConditionScoreboard = null;
        for (Entry<String, Map<String, Object>> entry : map.entrySet()) {
            String condition = (String) entry.getValue().get("display-condition");
            if (condition == null || condition.length() == 0) {
                noConditionScoreboardFound = true;
                noConditionScoreboard = entry.getKey();
            } else if (noConditionScoreboardFound) {
                TAB.getInstance().getMisconfigurationHelper().nonLastNoConditionScoreboard(noConditionScoreboard, entry.getKey());
            }
            String title = TAB.getInstance().getMisconfigurationHelper().fromMapOrElse(entry.getValue(), "title", "<Title not defined>",
                    "Scoreboard \"" + entry.getKey() + "\" is missing title!");
            List<String> lines = TAB.getInstance().getMisconfigurationHelper().fromMapOrElse(entry.getValue(), "lines",
                    Arrays.asList("scoreboard \"" + entry.getKey() +"\" is missing \"lines\" keyword!", "did you forget to configure it or just your spacing is wrong?"),
                    "Scoreboard \"" + entry.getKey() + "\" is missing lines!");
            ScoreboardImpl sb = new ScoreboardImpl(this, entry.getKey(), title, lines, condition);
            registeredScoreboards.put(entry.getKey(), sb);
            TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.scoreboardLine(entry.getKey()), sb);
        }
        definedScoreboards = registeredScoreboards.values().toArray(new me.neznamy.tab.api.scoreboard.Scoreboard[0]);
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            onJoin(p);
        }
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        if (forcedScoreboard.containsKey(p) || !hasScoreboardVisible(p) ||
                announcement != null || otherPluginScoreboards.containsKey(p) || joinDelayed.contains(p)) return;
        sendHighestScoreboard(p);
    }

    @Override
    public void unload() {
        for (me.neznamy.tab.api.scoreboard.Scoreboard board : definedScoreboards) {
            board.unregister();
        }
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        if (joinDelay > 0) {
            joinDelayed.add(connectedPlayer);
            TAB.getInstance().getCPUManager().runTaskLater(joinDelay, featureName, TabConstants.CpuUsageCategory.PLAYER_JOIN, () -> {
                if (!otherPluginScoreboards.containsKey(connectedPlayer)) setScoreboardVisible(connectedPlayer, hiddenByDefault == sbOffPlayers.contains(connectedPlayer.getName()), false);
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
    public void sendHighestScoreboard(@NonNull TabPlayer p) {
        if (!hasScoreboardVisible(p)) return;
        ScoreboardImpl scoreboard = (ScoreboardImpl) detectHighestScoreboard(p);
        ScoreboardImpl current = activeScoreboards.get(p);
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
    public void unregisterScoreboard(@NonNull TabPlayer p) {
        if (activeScoreboards.containsKey(p)) {
            activeScoreboards.get(p).removePlayer(p);
            activeScoreboards.remove(p);
        }
    }

    /**
     * Returns currently the highest scoreboard in chain for specified player
     *
     * @param   p
     *          player to check
     * @return  highest scoreboard player should see
     */
    public @Nullable me.neznamy.tab.api.scoreboard.Scoreboard detectHighestScoreboard(@NonNull TabPlayer p) {
        if (forcedScoreboard.containsKey(p)) return forcedScoreboard.get(p);
        for (me.neznamy.tab.api.scoreboard.Scoreboard sb : definedScoreboards) {
            if (((ScoreboardImpl)sb).isConditionMet(p)) return sb;
        }
        return null;
    }

    @Override
    public boolean onCommand(@NotNull TabPlayer sender, @NotNull String message) {
        if (message.equals(toggleCommand) || message.startsWith(toggleCommand+" ")) {
            TAB.getInstance().getCommand().execute(sender, message.replace(toggleCommand, "scoreboard").split(" "));
            return true;
        }
        return false;
    }

    @Override
    public void onDisplayObjective(@NotNull TabPlayer receiver, int slot, @NotNull String objective) {
        if (respectOtherPlugins && slot == Scoreboard.DisplaySlot.SIDEBAR.ordinal() && !objective.equals(OBJECTIVE_NAME)) {
            TAB.getInstance().debug("Player " + receiver.getName() + " received scoreboard called " + objective + ", hiding TAB one.");
            otherPluginScoreboards.put(receiver, objective);
            ScoreboardImpl sb = activeScoreboards.get(receiver);
            if (sb != null) {
                TAB.getInstance().getCPUManager().runMeasuredTask(featureName, TabConstants.CpuUsageCategory.SCOREBOARD_PACKET_CHECK, () -> sb.removePlayer(receiver));
            }
        }
    }

    @Override
    public void onObjective(@NotNull TabPlayer receiver, int action, @NotNull String objective) {
        if (respectOtherPlugins && action == 1 && otherPluginScoreboards.containsKey(receiver) && otherPluginScoreboards.get(receiver).equals(objective)) {
            TAB.getInstance().debug("Player " + receiver.getName() + " no longer has another scoreboard, sending TAB one.");
            otherPluginScoreboards.remove(receiver);
            TAB.getInstance().getCPUManager().runMeasuredTask(featureName, TabConstants.CpuUsageCategory.SCOREBOARD_PACKET_CHECK, () -> sendHighestScoreboard(receiver));
        }
    }

    @Override
    public @NotNull me.neznamy.tab.api.scoreboard.Scoreboard createScoreboard(@NonNull String name, @NonNull String title, @NonNull List<String> lines) {
        me.neznamy.tab.api.scoreboard.Scoreboard sb = new ScoreboardImpl(this, name, title, lines, true);
        registeredScoreboards.put(name, sb);
        definedScoreboards = registeredScoreboards.values().toArray(new me.neznamy.tab.api.scoreboard.Scoreboard[0]);
        return sb;
    }

    @Override
    public void showScoreboard(@NonNull me.neznamy.tab.api.TabPlayer player, @NonNull me.neznamy.tab.api.scoreboard.Scoreboard scoreboard) {
        if (forcedScoreboard.containsKey(player)) {
            forcedScoreboard.get(player).removePlayer((TabPlayer) player);
        }
        if (activeScoreboards.containsKey(player)) {
            activeScoreboards.get(player).removePlayer((TabPlayer) player);
            activeScoreboards.remove(player);
        }
        forcedScoreboard.put(player, (ScoreboardImpl) scoreboard);
        if (hasScoreboardVisible(player)) ((ScoreboardImpl) scoreboard).addPlayer((TabPlayer) player);
    }

    @Override
    public void resetScoreboard(me.neznamy.tab.api.@NonNull TabPlayer player) {
        if (!forcedScoreboard.containsKey(player)) return;
        forcedScoreboard.get(player).removePlayer((TabPlayer) player);
        forcedScoreboard.remove(player);
        me.neznamy.tab.api.scoreboard.Scoreboard sb = detectHighestScoreboard((TabPlayer) player);
        if (sb == null) return; //no scoreboard available
        activeScoreboards.put(player, (ScoreboardImpl) sb);
        ((ScoreboardImpl) sb).addPlayer((TabPlayer) player);
        forcedScoreboard.remove(player);
    }

    @Override
    public boolean hasScoreboardVisible(me.neznamy.tab.api.@NonNull TabPlayer player) {
        return visiblePlayers.contains(player);
    }

    @Override
    public boolean hasCustomScoreboard(me.neznamy.tab.api.@NonNull TabPlayer player) {
        return forcedScoreboard.containsKey(player);
    }

    @Override
    public void setScoreboardVisible(me.neznamy.tab.api.@NonNull TabPlayer p, boolean visible, boolean sendToggleMessage) {
        TabPlayer player = (TabPlayer) p;
        if (visiblePlayers.contains(player) == visible) return;
        if (visible) {
            visiblePlayers.add(player);
            sendHighestScoreboard(player);
            if (sendToggleMessage) {
                player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getScoreboardOn(), true);
            }
            if (rememberToggleChoice) {
                if (hiddenByDefault) {
                    if (!sbOffPlayers.contains(player.getName())) sbOffPlayers.add(player.getName());
                } else {
                    sbOffPlayers.remove(player.getName());
                }
                synchronized (sbOffPlayers) {
                    TAB.getInstance().getConfiguration().getPlayerDataFile().set("scoreboard-off", new ArrayList<>(sbOffPlayers));
                }
            }
        } else {
            visiblePlayers.remove(player);
            unregisterScoreboard(player);
            if (sendToggleMessage) {
                player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getScoreboardOff(), true);
            }
            if (rememberToggleChoice) {
                if (hiddenByDefault) {
                    sbOffPlayers.remove(player.getName());
                } else {
                    if (!sbOffPlayers.contains(player.getName())) sbOffPlayers.add(player.getName());
                }
                synchronized (sbOffPlayers) {
                    TAB.getInstance().getConfiguration().getPlayerDataFile().set("scoreboard-off", new ArrayList<>(sbOffPlayers));
                }
            }
        }
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setScoreboardVisible(player, visible);
    }

    @Override
    public void toggleScoreboard(me.neznamy.tab.api.@NonNull TabPlayer player, boolean sendToggleMessage) {
        setScoreboardVisible(player, !visiblePlayers.contains(player), sendToggleMessage);
    }

    @Override
    public void announceScoreboard(@NonNull String scoreboard, int duration) {
        if (duration < 0) throw new IllegalArgumentException("Duration cannot be negative");
        ScoreboardImpl sb = (ScoreboardImpl) registeredScoreboards.get(scoreboard);
        if (sb == null) throw new IllegalArgumentException("No registered scoreboard found with name " + scoreboard);
        Map<TabPlayer, ScoreboardImpl> previous = new HashMap<>();
        TAB.getInstance().getCPUManager().runTask(() -> {
            announcement = sb;
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (!hasScoreboardVisible(all)) continue;
                previous.put(all, activeScoreboards.get(all));
                if (activeScoreboards.containsKey(all)) activeScoreboards.get(all).removePlayer(all);
                sb.addPlayer(all);
            }
        });
        TAB.getInstance().getCPUManager().runTaskLater(duration*1000,
                featureName, "Removing announced Scoreboard", () -> {
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (!hasScoreboardVisible(all)) continue;
                sb.removePlayer(all);
                if (previous.get(all) != null) previous.get(all).addPlayer(all);
            }
            announcement = null;
        });
    }

    @Override
    public @Nullable me.neznamy.tab.api.scoreboard.Scoreboard getActiveScoreboard(me.neznamy.tab.api.@NonNull TabPlayer player) {
        return activeScoreboards.get(player);
    }

    @Override
    public void onServerChange(@NotNull TabPlayer changed, @NotNull String from, @NotNull String to) {
        otherPluginScoreboards.remove(changed);
        ScoreboardImpl scoreboard = activeScoreboards.get(changed);
        if (scoreboard != null) {
            scoreboard.removePlayerFromSet(changed);
            scoreboard.addPlayer(changed);
        }
    }
}