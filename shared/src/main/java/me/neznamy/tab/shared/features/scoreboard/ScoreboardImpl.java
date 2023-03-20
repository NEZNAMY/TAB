package me.neznamy.tab.shared.features.scoreboard;

import lombok.Getter;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.api.feature.Refreshable;
import me.neznamy.tab.api.feature.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.scoreboard.Line;
import me.neznamy.tab.api.scoreboard.Scoreboard;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.scoreboard.lines.*;
import me.neznamy.tab.shared.placeholders.conditions.Condition;

import java.util.*;

/**
 * A class representing a scoreboard configured in config
 */
public class ScoreboardImpl extends TabFeature implements Scoreboard, Refreshable {

    @Getter private final String featureName = "Scoreboard";
    @Getter private final String refreshDisplayName = "Updating Scoreboard title";

    //scoreboard manager
    @Getter private final ScoreboardManagerImpl manager;

    //name of this scoreboard
    @Getter private final String name;

    //scoreboard title
    @Getter private String title;

    //display condition
    private Condition displayCondition;

    //lines of scoreboard
    @Getter private final List<Line> lines = new ArrayList<>();

    //players currently seeing this scoreboard
    @Getter private final Set<TabPlayer> players = Collections.newSetFromMap(new WeakHashMap<>());

    private final String titleProperty;

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
    public ScoreboardImpl(ScoreboardManagerImpl manager, String name, String title, List<String> lines, String displayCondition) {
        this(manager, name, title, lines, false);
        this.displayCondition = Condition.getCondition(displayCondition);
        if (this.displayCondition != null) {
            manager.addUsedPlaceholders(Collections.singletonList(TabConstants.Placeholder.condition(this.displayCondition.getName())));
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
     */
    public ScoreboardImpl(ScoreboardManagerImpl manager, String name, String title, List<String> lines, boolean dynamicLinesOnly) {
        this.manager = manager;
        this.name = name;
        this.title = title;
        this.titleProperty = getName() + "-" + TabConstants.Property.SCOREBOARD_TITLE;
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
    private ScoreboardLine registerLine(int lineNumber, String text) {
        if (text == null) return new StaticLine(this, lineNumber, "");
        if (text.startsWith("Custom|")) {
            String[] elements = text.split("\\|");
            return new CustomLine(this, lineNumber, elements[1], elements[2], elements[3], Integer.parseInt(elements[4]));
        }
        if (text.startsWith("Long|")) {
            return new LongLine(this, lineNumber, text.substring(5));
        }
        if (text.contains("%")) {
            return new StableDynamicLine(this, lineNumber, text);
        }
        return new StaticLine(this, lineNumber, text);
    }

    /**
     * Returns true if condition is null or is met, false otherwise
     *
     * @param   p
     *          player to check
     * @return  true if condition is null or is met, false otherwise
     */
    public boolean isConditionMet(TabPlayer p) {
        return displayCondition == null || displayCondition.isMet(p);
    }

    public void addPlayer(TabPlayer p) {
        if (players.contains(p)) return; //already registered
        players.add(p);
        p.setProperty(this, titleProperty, title);
        p.getScoreboard().registerObjective(ScoreboardManagerImpl.OBJECTIVE_NAME, p.getProperty(titleProperty).get(), false);
        p.getScoreboard().setDisplaySlot(me.neznamy.tab.api.Scoreboard.DisplaySlot.SIDEBAR, ScoreboardManagerImpl.OBJECTIVE_NAME);
        for (Line s : lines) {
            ((ScoreboardLine)s).register(p);
        }
        manager.getActiveScoreboards().put(p, this);
        recalculateScores(p);
        TAB.getInstance().getPlaceholderManager().getTabExpansion().setScoreboardName(p, name);
    }

    @Override
    public void unregister() {
        for (TabPlayer all : getPlayers().toArray(new TabPlayer[0])) {
            removePlayer(all);
        }
        players.clear();
    }

    public void removePlayer(TabPlayer p) {
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
    public void refresh(TabPlayer refreshed, boolean force) {
        if (!players.contains(refreshed)) return;
        refreshed.getScoreboard().updateObjective(ScoreboardManagerImpl.OBJECTIVE_NAME, refreshed.getProperty(titleProperty).updateAndGet(), false);
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
        for (TabPlayer p : players) {
            p.setProperty(this, titleProperty, title);
            refresh(p, false);
        }
    }

    @Override
    public void addLine(String text) {
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

    public void recalculateScores(TabPlayer p) {
        if (!manager.isUsingNumbers()) return;
        List<Line> linesReversed = new ArrayList<>(lines);
        Collections.reverse(linesReversed);
        int score = 1;
        for (Line line : linesReversed) {
            if (line instanceof CustomLine) {
                score++;
                continue;
            }
            if (line instanceof StaticLine || p.getProperty(getName() + "-" + ((ScoreboardLine)line).getTeamName()).get().length() > 0) {
                p.getScoreboard().setScore(ScoreboardManagerImpl.OBJECTIVE_NAME, ((ScoreboardLine)line).getPlayerName(p), score++);
            }
        }
    }

    public void removePlayerFromSet(TabPlayer player) {
        players.remove(player);
    }
}