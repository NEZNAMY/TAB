package me.neznamy.tab.shared.features.scoreboard;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.scoreboard.Line;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.scoreboard.ScoreboardConfiguration.ScoreboardDefinition;
import me.neznamy.tab.shared.features.scoreboard.lines.ScoreboardLineHolder;
import me.neznamy.tab.shared.features.types.Conditional;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.placeholders.conditions.Condition;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class representing a scoreboard configured in config
 */
@Getter
public class ScoreboardImpl extends RefreshableFeature implements me.neznamy.tab.api.scoreboard.Scoreboard,
        CustomThreaded, Conditional {

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
        this(manager, name, definition, false);
        displayCondition = TAB.getInstance().getPlaceholderManager().getConditionManager().getByNameOrExpression(definition.getDisplayCondition());
        if (displayCondition != null) {
            manager.addUsedPlaceholder(displayCondition.getPlaceholderIdentifier());
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
     * @param   api
     *          Whether this scoreboard was created using API or not
     */
    public ScoreboardImpl(@NonNull ScoreboardManagerImpl manager, @NonNull String name, @NonNull ScoreboardDefinition definition, boolean api) {
        this.manager = manager;
        this.name = name;
        this.api = api;
        title = definition.getTitle();
        for (int i = 0; i< definition.getLines().size(); i++) {
            String line = definition.getLines().get(i);
            if (line == null) line = "";
            ScoreboardLineHolder score = new ScoreboardLineHolder(this, line, i+1);
            lines.add(score);
            TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.scoreboardLine(name, i), score);
        }
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
            ((ScoreboardLineHolder)s).register(p);
        }
        players.add(p);
        p.scoreboardData.activeScoreboard = this;
        p.expansionData.setScoreboardName(name);
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
            p.getScoreboard().unregisterTeam(((ScoreboardLineHolder) l).getTeamName());
        }
        players.remove(p);
        p.scoreboardData.activeScoreboard = null;
        p.scoreboardData.titleProperty = null;
        p.scoreboardData.lineProperties.clear();
        p.expansionData.setScoreboardName("");
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
     * Removes this player from list of players who can see it.
     *
     * @param   player
     *          Player to remove from set
     */
    public void removePlayerFromSet(@NonNull TabPlayer player) {
        players.remove(player);
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
        ScoreboardLineHolder line = new ScoreboardLineHolder(this, text, lines.size()+1);
        TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.scoreboardLine(name, lines.size()), line);
        lines.add(line);
        for (TabPlayer p : players) {
            line.register(p);
        }
    }

    @Override
    public void removeLine(int index) {
        ensureActive();
        if (index < 0 || index >= lines.size()) throw new IndexOutOfBoundsException("Index " + index + " is out of range (0 - " + (lines.size()-1) + ")");
        ScoreboardLineHolder line = (ScoreboardLineHolder) lines.get(index);
        lines.remove(line);
        for (TabPlayer p : players) {
            line.unregister(p);
        }
        TAB.getInstance().getFeatureManager().unregisterFeature(TabConstants.Feature.scoreboardLine(name, index));
    }

    @Override
    public void setLines(@NonNull List<String> newLines) {
        ensureActive();
        int commonSize = Math.min(lines.size(), newLines.size());
        for (int i = 0; i < commonSize; i++) {
            String newText = newLines.get(i) == null ? "" : newLines.get(i);
            if (!lines.get(i).getText().equals(newText))
                lines.get(i).setText(newText);
        }
        for (int i = lines.size() - 1; i >= commonSize; i--)
            removeLine(i);
        for (int i = commonSize; i < newLines.size(); i++)
            addLine(newLines.get(i) == null ? "" : newLines.get(i));
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
