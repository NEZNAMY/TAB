package me.neznamy.tab.shared.features.scoreboard;

import lombok.AllArgsConstructor;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.features.scoreboard.lines.ScoreboardLineHolder;
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

    /** Properties of scoreboard lines of scoreboard the player can currently see */
    @NotNull
    public final Map<ScoreboardLineHolder, LineProperties> lineProperties = new IdentityHashMap<>();

    /**
     * Data class for storing properties of a scoreboard line for a player.
     */
    @AllArgsConstructor
    public static class LineProperties {

        /** Property of line text */
        @NotNull
        public final Property textProperty;

        /** Name of the score (used in long lines) */
        @NotNull
        public String scoreName;

        /** Property of line NumberFormat */
        @NotNull
        public final Property numberFormatProperty;

        /** Property of line score */
        @NotNull
        public final Property scoreProperty;
    }
}