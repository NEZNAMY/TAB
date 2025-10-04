package me.neznamy.tab.shared.features.scoreboard;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.api.scoreboard.Line;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardConfiguration.ScoreboardDefinition;
import me.neznamy.tab.shared.features.scoreboard.lines.LongLine;
import me.neznamy.tab.shared.features.scoreboard.lines.ScoreboardLine;
import me.neznamy.tab.shared.features.scoreboard.lines.StableDynamicLine;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class representing a scoreboard configured in config
 */
@Getter
public class ScoreboardImpl extends RefreshableFeature implements me.neznamy.tab.api.scoreboard.Scoreboard, CustomThreaded {

    //scoreboard manager
    private final ScoreboardManagerImpl manager;

    //name of this scoreboard
    private final String name;

    //scoreboard title
    private String title;

    //display condition
    private Condition displayCondition;

    /** Flag tracking whether this scoreboard was made using API or not */
    private final boolean api;

    //lines of scoreboard
    private final List<Line> lines = new ArrayList<>();

    private boolean containsNumberFormat;

    //players currently seeing this scoreboard
    private final Set<TabPlayer> players = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * Constructs new instance with given parameters and registers lines to feature manager
     *
     * @param   manager
     *          scoreboard manager
     * @param   name
     *          name of this scoreboard
     * @param   definition
     *          Scoreboard properties
     */
    public ScoreboardImpl(@NonNull ScoreboardManagerImpl manager, @NonNull String name, @NonNull ScoreboardDefinition definition) {
        this(manager, name, definition, false, false);
        displayCondition = TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(definition.getDisplayCondition());
        if (displayCondition != null) {
            manager.addUsedPlaceholder(TabConstants.Placeholder.condition(displayCondition.getName()));
        }
    }

    /**
     * Constructs new instance with given parameters and registers lines to feature manager
     *
     * @param   manager
     *          scoreboard manager
     * @param   name
     *          name of this scoreboard
     * @param   definition
     *          Scoreboard properties
     * @param   dynamicLinesOnly
     *          Whether this scoreboard should only use dynamic lines or not
     * @param   api
     *          Whether this scoreboard was created using API or not
     */
    public ScoreboardImpl(@NonNull ScoreboardManagerImpl manager, @NonNull String name, @NonNull ScoreboardDefinition definition, boolean dynamicLinesOnly, boolean api) {
        this.manager = manager;
        this.name = name;
        this.api = api;
        title = definition.getTitle();
        for (int i = 0; i< definition.getLines().size(); i++) {
            String line = definition.getLines().get(i);
            if (line == null) line = "";
            if (line.contains("||")) containsNumberFormat = true;
            ScoreboardLine score;
            if (dynamicLinesOnly) {
                score = new StableDynamicLine(this, i+1, line);
            } else {
                score = registerLine(i+1, line);
            }
            lines.add(score);
            TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.scoreboardLine(name, i), score);
        }
    }

    /**
     * Registers line with given text and line number
     *
     * @param   lineNumber
     *          ID of line
     * @param   text
     *          text to display
     * @return  most optimal line from provided text
     */
    private @NotNull ScoreboardLine registerLine(int lineNumber, @Nullable String text) {
        if (text == null) return new LongLine(this, lineNumber, "");
        if (text.startsWith("Long|")) {
            return new LongLine(this, lineNumber, text.substring(5));
        }
        if (text.contains("%")) {
            return new StableDynamicLine(this, lineNumber, text);
        }
        return new LongLine(this, lineNumber, text);
    }

    /**
     * Returns true if condition is null or is met, false otherwise
     *
     * @param   p
     *          player to check
     * @return  true if condition is null or is met, false otherwise
     */
    public boolean isConditionMet(@NonNull TabPlayer p) {
        return displayCondition == null || displayCondition.isMet(p);
    }

    /**
     * Adds the player into scoreboard. This includes registering properties,
     * as well as scoreboard and all lines.
     *
     * @param   p
     *          Player to send this scoreboard to
     */
    public void addPlayer(@NonNull TabPlayer p) {
        if (p.scoreboardData.activeScoreboard == this) return; // already registered
        p.scoreboardData.titleProperty = new Property(this, p, title);
        p.getScoreboard().registerObjective(
                ScoreboardManagerImpl.OBJECTIVE_NAME,
                manager.getCache().get(p.scoreboardData.titleProperty.get()),
                Scoreboard.HealthDisplay.INTEGER,
                TabComponent.empty()
        );
        if (p.scoreboardData.otherPluginScoreboard == null) {
            p.getScoreboard().setDisplaySlot(ScoreboardManagerImpl.OBJECTIVE_NAME, Scoreboard.DisplaySlot.SIDEBAR);
        }
        for (Line s : lines) {
            ((ScoreboardLine)s).register(p);
        }
        players.add(p);
        p.scoreboardData.activeScoreboard = this;
        recalculateScores(p);
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setScoreboardName(p, name);
        if (containsNumberFormat && p.getVersionId() < ProtocolVersion.V1_20_3.getNetworkId()) {
            TAB.getInstance().getConfigHelper().runtime().error("Scoreboard \"" + name + "\" contains right-side text alignment (using ||), however, this feature " +
                    "was added in 1.20.3, but player \"" + p.getName() + "\" is using version " + p.getVersion().getFriendlyName() + ". Right-side text " +
                    "will not be visible for them.");
        }
    }

    /**
     * Unregisters player from this scoreboard.
     *
     * @param   p
     *          Player to unregister
     */
    public void removePlayer(@NonNull TabPlayer p) {
        if (p.scoreboardData.activeScoreboard != this) return; // not registered
        p.getScoreboard().unregisterObjective(ScoreboardManagerImpl.OBJECTIVE_NAME);
        for (Line l : lines) {
            ScoreboardLine line = (ScoreboardLine) l;
            if (line.isShownTo(p)) {
                p.getScoreboard().unregisterTeam(line.getTeamName());
                line.removePlayerSilently(p);
            }
        }
        players.remove(p);
        p.scoreboardData.activeScoreboard = null;
        p.scoreboardData.titleProperty = null;
        p.scoreboardData.lineProperties.clear();
        p.scoreboardData.lineNameProperties.clear();
        p.scoreboardData.numberFormatProperties.clear();
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setScoreboardName(p, "");
    }

    @NotNull
    @Override
    public String getRefreshDisplayName() {
        return "Updating Scoreboard title";
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        if (refreshed.scoreboardData.activeScoreboard != this) return; //player has different scoreboard displayed
        refreshed.getScoreboard().updateObjective(
                ScoreboardManagerImpl.OBJECTIVE_NAME,
                manager.getCache().get(refreshed.scoreboardData.titleProperty.updateAndGet()),
                Scoreboard.HealthDisplay.INTEGER,
                TabComponent.empty()
        );
    }

    /**
     * Recalculate scores for each line if using numbers. This takes into
     * consideration lines that are not visible.
     *
     * @param   p
     *          Player to recalculate scores for
     */
    public void recalculateScores(@NonNull TabPlayer p) {
        if (!manager.getConfiguration().isUseNumbers()) return;
        List<Line> linesReversed = new ArrayList<>(lines);
        Collections.reverse(linesReversed);
        int score = manager.getConfiguration().getStaticNumber();
        for (Line line : linesReversed) {
            Property pr = p.scoreboardData.lineProperties.get((ScoreboardLine) line);
            if (pr.getCurrentRawValue().isEmpty() || (!pr.getCurrentRawValue().isEmpty() && !pr.get().isEmpty())) {
                p.getScoreboard().setScore(
                        ScoreboardManagerImpl.OBJECTIVE_NAME,
                        ((ScoreboardLine)line).getPlayerName(p),
                        score++,
                        null, // Makes no sense for TAB
                        ((ScoreboardLine) line).getScoreRefresher().getNumberFormat(p)
                );
            }
        }
    }

    /**
     * Removes this player from list of players who can see it.
     *
     * @param   player
     *          Player to remove from set
     */
    public void removePlayerFromSet(@NonNull TabPlayer player) {
        players.remove(player);
        for (Line line : lines) {
            ((ScoreboardLine)line).removePlayerSilently(player);
        }
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return manager.getFeatureName();
    }

    // ------------------
    // API Implementation
    // ------------------

    @Override
    public void setTitle(@NonNull String title) {
        ensureActive();
        this.title = title;
        for (TabPlayer p : players) {
            p.scoreboardData.titleProperty.changeRawValue(title);
            p.getScoreboard().updateObjective(
                    ScoreboardManagerImpl.OBJECTIVE_NAME,
                    manager.getCache().get(p.scoreboardData.titleProperty.get()),
                    Scoreboard.HealthDisplay.INTEGER,
                    TabComponent.empty()
            );
        }
    }

    @Override
    public void addLine(@NonNull String text) {
        ensureActive();
        StableDynamicLine line = new StableDynamicLine(this, lines.size()+1, text);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.scoreboardLine(name, lines.size()), line);
        lines.add(line);
        for (TabPlayer p : players) {
            line.register(p);
            recalculateScores(p);
        }
    }

    @Override
    public void removeLine(int index) {
        ensureActive();
        if (index < 0 || index >= lines.size()) throw new IndexOutOfBoundsException("Index " + index + " is out of range (0 - " + (lines.size()-1) + ")");
        ScoreboardLine line = (ScoreboardLine) lines.get(index);
        lines.remove(line);
        for (TabPlayer p : players) {
            line.unregister(p);
            recalculateScores(p);
        }
        TAB.getInstance().getFeatureManager().unregisterFeature(TabConstants.Feature.scoreboardLine(name, index));
    }

    @Override
    public void unregister() {
        ensureActive();
        for (TabPlayer all : players.toArray(new TabPlayer[0])) {
            removePlayer(all);
        }
        players.clear();
    }

    @Override
    @NotNull
    public ThreadExecutor getCustomThread() {
        return manager.getCustomThread();
    }
}