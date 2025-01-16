package me.neznamy.tab.shared.features.scoreboard;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.cpu.TimedCaughtTask;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardConfiguration.ScoreboardDefinition;
import me.neznamy.tab.shared.features.scoreboard.lines.ScoreboardLine;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.SafeScoreboard;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;

/**
 * Feature handler for scoreboard feature.
 */
public class ScoreboardManagerImpl extends RefreshableFeature implements ScoreboardManager, JoinListener,
        CommandListener, DisplayObjectiveListener, ObjectiveListener, Loadable,
        QuitListener, CustomThreaded, ServerSwitchListener {

    /** Objective name used by this feature */
    public static final String OBJECTIVE_NAME = "TAB-Scoreboard";

    @Getter
    private final StringToComponentCache cache = new StringToComponentCache("Scoreboard", 1000);

    @Getter
    private final ThreadExecutor customThread = new ThreadExecutor("TAB Scoreboard Thread");

    @Getter
    private final ScoreboardConfiguration configuration;

    //defined scoreboards
    @Getter private final Map<String, me.neznamy.tab.api.scoreboard.Scoreboard> registeredScoreboards = new LinkedHashMap<>();
    private me.neznamy.tab.api.scoreboard.Scoreboard[] definedScoreboards;

    //list of players with disabled scoreboard
    private final List<String> sbOffPlayers;

    //active scoreboard announcement
    @Nullable
    private me.neznamy.tab.api.scoreboard.Scoreboard announcement;

    /**
     * Constructs new instance and loads config options.
     *
     * @param   configuration
     *          Feature configuration
     */
    public ScoreboardManagerImpl(@NotNull ScoreboardConfiguration configuration) {
        this.configuration = configuration;
        sbOffPlayers = configuration.isRememberToggleChoice() ? TAB.getInstance().getConfiguration().getPlayerDataFile()
                .getStringList("scoreboard-off", new ArrayList<>()) : Collections.emptyList();
    }

    @Override
    public void load() {
        for (Entry<String, ScoreboardDefinition> entry : configuration.getScoreboards().entrySet()) {
            String scoreboardName = entry.getKey();
            ScoreboardImpl sb = new ScoreboardImpl(this, scoreboardName, entry.getValue());
            registeredScoreboards.put(scoreboardName, sb);
            TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.scoreboardLine(scoreboardName), sb);
        }
        definedScoreboards = registeredScoreboards.values().stream().filter(s -> !((ScoreboardImpl)s).isApi()).toArray(me.neznamy.tab.api.scoreboard.Scoreboard[]::new);
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            onJoin(p);
        }
    }

    @NotNull
    @Override
    public String getRefreshDisplayName() {
        return "Switching scoreboards";
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        if (p.scoreboardData.forcedScoreboard != null || !hasScoreboardVisible(p) ||
                announcement != null || p.scoreboardData.joinDelayed) return;
        sendHighestScoreboard(p);
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        ((SafeScoreboard<?>)connectedPlayer.getScoreboard()).setAntiOverrideScoreboard(true);
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setScoreboardName(connectedPlayer, "");
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setScoreboardVisible(connectedPlayer, false);
        if (configuration.getJoinDelay() > 0) {
            connectedPlayer.scoreboardData.joinDelayed = true;
            customThread.executeLater(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
                setScoreboardVisible(connectedPlayer, configuration.isHiddenByDefault() == sbOffPlayers.contains(connectedPlayer.getName()), false);
                connectedPlayer.scoreboardData.joinDelayed = false;
            }, getFeatureName(), TabConstants.CpuUsageCategory.PLAYER_JOIN), configuration.getJoinDelay());
        } else {
            setScoreboardVisible(connectedPlayer, configuration.isHiddenByDefault() == sbOffPlayers.contains(connectedPlayer.getName()), false);
        }
    }

    /**
     * Sends the player scoreboard he should see according to conditions and worlds
     *
     * @param   p
     *          player to send scoreboard to
     */
    public void sendHighestScoreboard(@NonNull TabPlayer p) {
        if (p.scoreboardData.otherPluginScoreboard != null) return;
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
        if (message.equals(configuration.getToggleCommand())) {
            TAB.getInstance().getCommand().execute(sender, new String[] {"scoreboard"});
            return true;
        }
        return false;
    }

    @Override
    @NotNull
    public String getCommand() {
        return configuration.getToggleCommand();
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
        ScoreboardImpl sb = new ScoreboardImpl(this, name, new ScoreboardDefinition(null, title, lines), true, true);
        registeredScoreboards.put(name, sb);
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
            if (player.scoreboardData.otherPluginScoreboard == null) {
                sendHighestScoreboard(player);
            }
            if (sendToggleMessage) {
                player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getScoreboardOn(), true);
            }
            if (configuration.isRememberToggleChoice()) {
                if (configuration.isHiddenByDefault()) {
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
            if (player.scoreboardData.otherPluginScoreboard == null) {
                unregisterScoreboard(player);
            }
            if (sendToggleMessage) {
                player.sendMessage(TAB.getInstance().getConfiguration().getMessages().getScoreboardOff(), true);
            }
            if (configuration.isRememberToggleChoice()) {
                if (configuration.isHiddenByDefault()) {
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
        customThread.execute(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            announcement = sb;
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (!hasScoreboardVisible(all)) continue;
                previous.put(all, all.scoreboardData.activeScoreboard);
                if (all.scoreboardData.activeScoreboard != null) all.scoreboardData.activeScoreboard.removePlayer(all);
                sb.addPlayer(all);
            }
        }, getFeatureName(), "Adding announced Scoreboard"));
        customThread.executeLater(new TimedCaughtTask(TAB.getInstance().getCpu(), () -> {
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (!hasScoreboardVisible(all)) continue;
                sb.removePlayer(all);
                if (previous.get(all) != null) previous.get(all).addPlayer(all);
            }
            announcement = null;
        }, getFeatureName(), "Removing announced Scoreboard"), duration*1000);
    }

    @Override
    @Nullable
    public ScoreboardImpl getActiveScoreboard(@NonNull me.neznamy.tab.api.TabPlayer player) {
        ensureActive();
        return ((TabPlayer)player).scoreboardData.activeScoreboard;
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return "Scoreboard";
    }

    @Override
    public void onServerChange(@NotNull TabPlayer changed, @NotNull String from, @NotNull String to) {
        if (changed.scoreboardData.otherPluginScoreboard != null) {
            changed.scoreboardData.otherPluginScoreboard = null;
            sendHighestScoreboard(changed);
        }
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

        /** Property of scoreboard title of scoreboard the player can currently see */
        @Nullable
        public Property titleProperty;

        /** Map of line text properties */
        @NotNull
        public final Map<ScoreboardLine, Property> lineProperties = new IdentityHashMap<>();

        /** Map of line player name properties (used in long lines) */
        @NotNull
        public final Map<ScoreboardLine, Property> lineNameProperties = new IdentityHashMap<>();

        /** Map of line NumberFormat properties */
        @NotNull
        public final Map<ScoreboardLine, Property> numberFormatProperties = new IdentityHashMap<>();
    }
}