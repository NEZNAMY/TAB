package me.neznamy.tab.shared.features.scoreboard;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.types.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Feature handler for scoreboard feature.
 */
public class ScoreboardManagerImpl extends RefreshableFeature implements ScoreboardManager, JoinListener,
        CommandListener, DisplayObjectiveListener, ObjectiveListener, Loadable, UnLoadable,
        QuitListener, CustomThreaded {

    /** Objective name used by this feature */
    public static final String OBJECTIVE_NAME = "TAB-Scoreboard";

    @Getter
    private final ScheduledExecutorService customThread = TAB.getInstance().getCpu().newExecutor("TAB Scoreboard Thread");

    //config options
    @Getter private final String command = config().getString("scoreboard.toggle-command", "/sb");
    @Getter private final boolean usingNumbers = config().getBoolean("scoreboard.use-numbers", false);
    private final boolean rememberToggleChoice = config().getBoolean("scoreboard.remember-toggle-choice", false);
    private final boolean hiddenByDefault = config().getBoolean("scoreboard.hidden-by-default", false);
    @Getter private final int staticNumber = config().getInt("scoreboard.static-number", 0);
    private final int joinDelay = config().getInt("scoreboard.delay-on-join-milliseconds", 0);

    //defined scoreboards
    @Getter private final Map<String, me.neznamy.tab.api.scoreboard.Scoreboard> registeredScoreboards = new LinkedHashMap<>();
    private me.neznamy.tab.api.scoreboard.Scoreboard[] definedScoreboards;

    //list of players with disabled scoreboard
    private final List<String> sbOffPlayers = rememberToggleChoice ? TAB.getInstance().getConfiguration().getPlayerDataFile()
            .getStringList("scoreboard-off", new ArrayList<>()) : Collections.emptyList();

    //active scoreboard announcement
    @Nullable
    private me.neznamy.tab.api.scoreboard.Scoreboard announcement;

    /**
     * Constructs new instance and loads config options.
     */
    public ScoreboardManagerImpl() {
        super("Scoreboard", "Switching scoreboards");
    }

    @Override
    public void load() {
        Map<String, Map<String, Object>> map = config().getConfigurationSection("scoreboard.scoreboards");
        boolean noConditionScoreboardFound = false;
        String noConditionScoreboard = null;
        for (Entry<String, Map<String, Object>> entry : map.entrySet()) {
            if (entry.getValue() == null) {
                TAB.getInstance().getConfigHelper().startup().invalidScoreboardSection(entry.getKey());
                continue;
            }
            String condition = (String) entry.getValue().get("display-condition");
            if (condition == null || condition.isEmpty()) {
                noConditionScoreboardFound = true;
                noConditionScoreboard = entry.getKey();
            } else if (noConditionScoreboardFound) {
                TAB.getInstance().getConfigHelper().startup().nonLastNoConditionScoreboard(noConditionScoreboard, entry.getKey());
            }
            String title = TAB.getInstance().getConfigHelper().startup().fromMapOrElse(entry.getValue(), "title", "<Title not defined>",
                    "Scoreboard \"" + entry.getKey() + "\" is missing title!");
            List<String> lines = TAB.getInstance().getConfigHelper().startup().fromMapOrElse(entry.getValue(), "lines",
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
        if (p.scoreboardData.forcedScoreboard != null || !hasScoreboardVisible(p) ||
                announcement != null || p.scoreboardData.otherPluginScoreboard != null || p.scoreboardData.joinDelayed) return;
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
        ((SafeScoreboard<?>)connectedPlayer.getScoreboard()).setAntiOverrideScoreboard(true);
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setScoreboardName(connectedPlayer, "");
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setScoreboardVisible(connectedPlayer, false);
        if (joinDelay > 0) {
            connectedPlayer.scoreboardData.joinDelayed = true;
            TAB.getInstance().getCpu().executeLater(customThread, () -> {
                if (connectedPlayer.scoreboardData.otherPluginScoreboard == null)
                    setScoreboardVisible(connectedPlayer, hiddenByDefault == sbOffPlayers.contains(connectedPlayer.getName()), false);
                connectedPlayer.scoreboardData.joinDelayed = false;
            }, getFeatureName(), TabConstants.CpuUsageCategory.PLAYER_JOIN, joinDelay);
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
        ScoreboardImpl current = p.scoreboardData.activeScoreboard;
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
        if (p.scoreboardData.activeScoreboard != null) {
            p.scoreboardData.activeScoreboard.removePlayer(p);
            p.scoreboardData.activeScoreboard = null;
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
        if (p.scoreboardData.forcedScoreboard != null) return p.scoreboardData.forcedScoreboard;
        for (me.neznamy.tab.api.scoreboard.Scoreboard sb : definedScoreboards) {
            if (((ScoreboardImpl)sb).isConditionMet(p)) return sb;
        }
        return null;
    }

    @Override
    public boolean onCommand(@NotNull TabPlayer sender, @NotNull String message) {
        if (message.equals(command)) {
            TAB.getInstance().getCommand().execute(sender, new String[] {"scoreboard"});
            return true;
        }
        return false;
    }

    @Override
    public void onDisplayObjective(@NotNull TabPlayer receiver, int slot, @NotNull String objective) {
        if (slot == Scoreboard.DisplaySlot.SIDEBAR.ordinal() && !objective.equals(OBJECTIVE_NAME)) {
            TAB.getInstance().debug("Player " + receiver.getName() + " received scoreboard called " + objective + ", hiding TAB one.");
            receiver.scoreboardData.otherPluginScoreboard = objective;
            ScoreboardImpl sb = receiver.scoreboardData.activeScoreboard;
            if (sb != null) {
                sb.removePlayer(receiver);
            }
        }
    }

    @Override
    public void onObjective(@NotNull TabPlayer receiver, int action, @NotNull String objective) {
        if (action == Scoreboard.ObjectiveAction.UNREGISTER && objective.equals(receiver.scoreboardData.otherPluginScoreboard)) {
            TAB.getInstance().debug("Player " + receiver.getName() + " no longer has another scoreboard, sending TAB one.");
            receiver.scoreboardData.otherPluginScoreboard = null;
            sendHighestScoreboard(receiver);
        }
    }

    private void savePlayers() {
        synchronized (sbOffPlayers) {
            TAB.getInstance().getConfiguration().getPlayerDataFile().set("scoreboard-off", new ArrayList<>(sbOffPlayers));
        }
    }

    @Override
    public void onQuit(@NotNull TabPlayer disconnectedPlayer) {
        ScoreboardImpl sb = disconnectedPlayer.scoreboardData.activeScoreboard;
        if (sb != null) {
            sb.removePlayerFromSet(disconnectedPlayer);
        }
    }

    // ------------------
    // API Implementation
    // ------------------

    @Override
    @NotNull
    public me.neznamy.tab.api.scoreboard.Scoreboard createScoreboard(@NonNull String name, @NonNull String title, @NonNull List<String> lines) {
        ensureActive();
        me.neznamy.tab.api.scoreboard.Scoreboard sb = new ScoreboardImpl(this, name, title, lines, true);
        registeredScoreboards.put(name, sb);
        definedScoreboards = registeredScoreboards.values().toArray(new me.neznamy.tab.api.scoreboard.Scoreboard[0]);
        return sb;
    }

    @Override
    public void showScoreboard(@NonNull me.neznamy.tab.api.TabPlayer player, @NonNull me.neznamy.tab.api.scoreboard.Scoreboard scoreboard) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();

        if (p.scoreboardData.forcedScoreboard != null) {
            p.scoreboardData.forcedScoreboard.removePlayer(p);
        }
        p.scoreboardData.forcedScoreboard = (ScoreboardImpl) scoreboard;

        if (p.scoreboardData.activeScoreboard != null) {
            p.scoreboardData.activeScoreboard.removePlayer(p);
            p.scoreboardData.activeScoreboard = null;
        }

        if (hasScoreboardVisible(player)) ((ScoreboardImpl) scoreboard).addPlayer(p);
    }

    @Override
    public boolean hasCustomScoreboard(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        return ((TabPlayer)player).scoreboardData.forcedScoreboard != null;
    }
    
    @Override
    public void resetScoreboard(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        TabPlayer p = (TabPlayer) player;
        p.ensureLoaded();

        if (p.scoreboardData.forcedScoreboard != null) {
            p.scoreboardData.forcedScoreboard.removePlayer(p);
            p.scoreboardData.forcedScoreboard = null;
            me.neznamy.tab.api.scoreboard.Scoreboard sb = detectHighestScoreboard(p);
            if (sb == null) return; //no scoreboard available
            p.scoreboardData.activeScoreboard = (ScoreboardImpl) sb;
            ((ScoreboardImpl) sb).addPlayer(p);
        }
    }

    @Override
    public boolean hasScoreboardVisible(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        return ((TabPlayer)player).scoreboardData.visible;
    }

    @Override
    public void setScoreboardVisible(@NonNull me.neznamy.tab.api.TabPlayer p, boolean visible, boolean sendToggleMessage) {
        ensureActive();
        TabPlayer player = (TabPlayer) p;
        if (player.scoreboardData.visible == visible) return;
        if (visible) {
            player.scoreboardData.visible = true;
            sendHighestScoreboard(player);
            if (sendToggleMessage) {
                player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getScoreboardOn(), true);
            }
            if (rememberToggleChoice) {
                if (hiddenByDefault) {
                    if (!sbOffPlayers.contains(player.getName())) {
                        sbOffPlayers.add(player.getName());
                        savePlayers();
                    }
                } else {
                    if (sbOffPlayers.remove(player.getName())) {
                        savePlayers();
                    }
                }
            }
        } else {
            player.scoreboardData.visible = false;
            unregisterScoreboard(player);
            if (sendToggleMessage) {
                player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getScoreboardOff(), true);
            }
            if (rememberToggleChoice) {
                if (hiddenByDefault) {
                    if (sbOffPlayers.remove(player.getName())) {
                        savePlayers();
                    }
                } else {
                    if (!sbOffPlayers.contains(player.getName())) {
                        sbOffPlayers.add(player.getName());
                        savePlayers();
                    }
                }
            }
        }
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setScoreboardVisible(player, visible);
    }

    @Override
    public void toggleScoreboard(@NonNull me.neznamy.tab.api.TabPlayer player, boolean sendToggleMessage) {
        ensureActive();
        setScoreboardVisible(player, !((TabPlayer)player).scoreboardData.visible, sendToggleMessage);
    }

    @Override
    public void announceScoreboard(@NonNull String scoreboard, int duration) {
        ensureActive();
        if (duration < 0) throw new IllegalArgumentException("Duration cannot be negative");
        ScoreboardImpl sb = (ScoreboardImpl) registeredScoreboards.get(scoreboard);
        if (sb == null) throw new IllegalArgumentException("No registered scoreboard found with name " + scoreboard);
        Map<TabPlayer, ScoreboardImpl> previous = new HashMap<>();
        TAB.getInstance().getCpu().execute(customThread, () -> {
            announcement = sb;
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (!hasScoreboardVisible(all)) continue;
                previous.put(all, all.scoreboardData.activeScoreboard);
                if (all.scoreboardData.activeScoreboard != null) all.scoreboardData.activeScoreboard.removePlayer(all);
                sb.addPlayer(all);
            }
        }, getFeatureName(), "Adding announced Scoreboard");
        TAB.getInstance().getCpu().executeLater(customThread, () -> {
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (!hasScoreboardVisible(all)) continue;
                sb.removePlayer(all);
                if (previous.get(all) != null) previous.get(all).addPlayer(all);
            }
            announcement = null;
        }, getFeatureName(), "Removing announced Scoreboard", duration*1000);
    }

    @Override
    @Nullable
    public ScoreboardImpl getActiveScoreboard(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        return ((TabPlayer)player).scoreboardData.activeScoreboard;
    }

    /**
     * Class storing scoreboard data of players.
     */
    public static class PlayerData {

        /** Flag tracking whether this player is under join delay or not */
        public boolean joinDelayed;

        /** Flag tracking whether player wishes to have scoreboard visible or not */
        public boolean visible;

        /** Scoreboard currently displayed to player */
        @Nullable
        public ScoreboardImpl activeScoreboard;

        /** Forced scoreboard using API */
        @Nullable
        public ScoreboardImpl forcedScoreboard;

        /** Scoreboard sent by another plugin (objective name) */
        @Nullable
        public String otherPluginScoreboard;
    }
}