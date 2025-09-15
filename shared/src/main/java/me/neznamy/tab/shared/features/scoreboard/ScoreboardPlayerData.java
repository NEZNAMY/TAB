package me.neznamy.tab.shared.features.scoreboard;

import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.features.scoreboard.lines.ScoreboardLine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Class storing scoreboard data of players.
 */
public class ScoreboardPlayerData {

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