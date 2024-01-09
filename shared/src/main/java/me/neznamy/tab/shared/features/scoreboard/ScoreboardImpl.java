package me.neznamy.tab.shared.features.scoreboard;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.api.scoreboard.Line;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.scoreboard.lines.*;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A class representing a scoreboard configured in config
 */
@Getter
public class ScoreboardImpl extends TabFeature implements me.neznamy.tab.api.scoreboard.Scoreboard, Refreshable {

    private final String featureName = "Scoreboard";
    private final String refreshDisplayName = "Updating Scoreboard title";
    private final String titleProperty = Property.randomName();

    //scoreboard manager
    private final ScoreboardManagerImpl manager;

    //name of this scoreboard
    private final String name;

    //scoreboard title
    private String title;

    //display condition
    private Condition displayCondition;

    //lines of scoreboard
    private final List<Line> lines = new ArrayList<>();

    //players currently seeing this scoreboard
    private final Set<TabPlayer> players = Collections.newSetFromMap(new WeakHashMap<>());

    /**
     * Constructs new instance with given parameters and registers lines to feature manager
     *
     * @param   manager
     *          scoreboard manager
     * @param   name
     *          name of this scoreboard
     * @param   title
     *          scoreboard title
     * @param   lines
     *          lines of scoreboard
     * @param   displayCondition
     *          display condition
     */
    public ScoreboardImpl(@NonNull ScoreboardManagerImpl manager, @NonNull String name, @NonNull String title,
                          @NonNull List<String> lines, @Nullable String displayCondition) {
        this(manager, name, title, lines, false);
        this.displayCondition = Condition.getCondition(displayCondition);
        if (this.displayCondition != null) {
            manager.addUsedPlaceholder(TabConstants.Placeholder.condition(this.displayCondition.getName()));
        }
    }

    /**
     * Constructs new instance with given parameters and registers lines to feature manager
     *
     * @param   manager
     *          scoreboard manager
     * @param   name
     *          name of this scoreboard
     * @param   title
     *          scoreboard title
     * @param   lines
     *          lines of scoreboard
     * @param   dynamicLinesOnly
     *          Whether this scoreboard should only use dynamic lines or not
     */
    public ScoreboardImpl(@NonNull ScoreboardManagerImpl manager, @NonNull String name, @NonNull String title,
                          @NonNull List<String> lines, boolean dynamicLinesOnly) {
        this.manager = manager;
        this.name = name;
        this.title = title;
        for (int i=0; i<lines.size(); i++) {
            ScoreboardLine score;
            if (dynamicLinesOnly) {
                score = new StableDynamicLine(this, i+1, lines.get(i));
            } else {
                score = registerLine(i+1, lines.get(i));
            }
            this.lines.add(score);
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
        if (players.contains(p)) return; //already registered
        players.add(p);
        p.setProperty(this, titleProperty, title);
        p.getScoreboard().registerObjective(
                ScoreboardManagerImpl.OBJECTIVE_NAME,
                p.getProperty(titleProperty).updateAndGet(),
                Scoreboard.HealthDisplay.INTEGER,
                new IChatBaseComponent("")
        );
        p.getScoreboard().setDisplaySlot(Scoreboard.DisplaySlot.SIDEBAR, ScoreboardManagerImpl.OBJECTIVE_NAME);
        for (Line s : lines) {
            ((ScoreboardLine)s).register(p);
        }
        manager.getActiveScoreboards().put(p, this);
        recalculateScores(p);
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setScoreboardName(p, name);
    }

    @Override
    public void unregister() {
        for (TabPlayer all : players.toArray(new TabPlayer[0])) {
            removePlayer(all);
        }
        players.clear();
    }

    /**
     * Unregisters player from this scoreboard.
     *
     * @param   p
     *          Player to unregister
     */
    public void removePlayer(@NonNull TabPlayer p) {
        if (!players.contains(p)) return; //not registered
        p.getScoreboard().unregisterObjective(ScoreboardManagerImpl.OBJECTIVE_NAME);
        for (Line line : lines) {
            if (((ScoreboardLine)line).isShownTo(p))
                p.getScoreboard().unregisterTeam(((ScoreboardLine)line).getTeamName());
        }
        players.remove(p);
        manager.getActiveScoreboards().remove(p);
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setScoreboardName(p, "");
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        if (!players.contains(refreshed)) return;
        refreshed.getScoreboard().updateObjective(
                ScoreboardManagerImpl.OBJECTIVE_NAME,
                refreshed.getProperty(titleProperty).updateAndGet(),
                Scoreboard.HealthDisplay.INTEGER,
                new IChatBaseComponent("")
        );
    }

    @Override
    public void setTitle(@NonNull String title) {
        this.title = title;
        for (TabPlayer p : players) {
            p.setProperty(this, titleProperty, title);
            refresh(p, false);
        }
    }

    @Override
    public void addLine(@NonNull String text) {
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
        if (index < 0 || index >= lines.size()) throw new IndexOutOfBoundsException("Index " + index + " is out of range (0 - " + (lines.size()-1) + ")");
        ScoreboardLine line = (ScoreboardLine) lines.get(index);
        lines.remove(line);
        for (TabPlayer p : players) {
            line.unregister(p);
            recalculateScores(p);
        }
        TAB.getInstance().getFeatureManager().unregisterFeature(TabConstants.Feature.scoreboardLine(name, index));
    }

    /**
     * Recalculate scores for each line if using numbers. This takes into
     * consideration lines that are not visible.
     *
     * @param   p
     *          Player to recalculate scores for
     */
    public void recalculateScores(@NonNull TabPlayer p) {
        if (!manager.isUsingNumbers()) return;
        List<Line> linesReversed = new ArrayList<>(lines);
        Collections.reverse(linesReversed);
        int score = 1;
        for (Line line : linesReversed) {
            if (!p.getProperty(name + "-" + ((ScoreboardLine) line).getTeamName()).get().isEmpty()) {
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
     * Used on Login packet which clears all scoreboards.
     *
     * @param   player
     *          Player to remove from set
     */
    public void removePlayerFromSet(@NonNull TabPlayer player) {
        players.remove(player);
    }
}