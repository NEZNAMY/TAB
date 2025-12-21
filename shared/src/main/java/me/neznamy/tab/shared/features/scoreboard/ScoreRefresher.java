package me.neznamy.tab.shared.features.scoreboard;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.scoreboard.lines.ScoreboardLine;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.util.cache.StringToComponentCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Feature refreshing score / NumberFormat in a scoreboard line for players.
 */
@RequiredArgsConstructor
public class ScoreRefresher extends RefreshableFeature implements CustomThreaded {

    private static final StringToComponentCache cache = new StringToComponentCache("Scoreboard NumberFormat", 1000);

    /** Line this score belongs to */
    @NonNull private final ScoreboardLine line;

    /** Line number to use if nothing is configured (1-15) */
    private final int lineNumber;

    /** Configured score for <1.20.3 (can be null if not configured) */
    @Nullable private final String score;

    /** Configured number format (can be null if not configured) */
    @Nullable private final String numberFormat;

    @NotNull
    @Override
    public String getRefreshDisplayName() {
        return "Updating NumberFormat";
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        if (refreshed.scoreboardData.activeScoreboard != line.getParent()) return; //player has different scoreboard displayed
        if (refreshed.scoreboardData.numberFormatProperties.get(line) == null) return; // Shrug
        refreshed.getScoreboard().setScore(
                ScoreboardManagerImpl.OBJECTIVE_NAME,
                line.getPlayerName(refreshed),
                getScore(refreshed),
                null,
                getNumberFormat(refreshed)
        );
    }

    /**
     * Registers properties for player.
     *
     * @param   player
     *          Player to register properties for
     */
    public void registerProperties(@NotNull TabPlayer player) {
        player.scoreboardData.scoreProperties.put(line, new Property(this, player, score == null ? "" : score));
        player.scoreboardData.numberFormatProperties.put(line, new Property(this, player, numberFormat == null ? "" : numberFormat));
    }

    /**
     * Returns new score value for specified player.
     *
     * @param   player
     *          Player to get score for
     * @return  New score based on current placeholder results
     */
    public int getScore(@NotNull TabPlayer player) {
        return getScore(player, line.getParent().getLines().size() + 1 - lineNumber);
    }

    /**
     * Returns new score value for specified player.
     * If score is not configured, returns provided fallback number.
     *
     * @param   player
     *          Player to get score for
     * @param   fallbackNumber
     *          Fallback number to return if score is not configured
     * @return  New score based on current placeholder results
     */
    public int getScore(@NotNull TabPlayer player, int fallbackNumber) {
        // Check if score is configured (for <1.20.3)
        if (score != null) {
            try {
                return Integer.parseInt(player.scoreboardData.scoreProperties.get(line).updateAndGet());
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return fallbackNumber;
    }

    /**
     * Returns new number format value for specified player.
     *
     * @param   player
     *          Player to get number format for
     * @return  New number format based on current placeholder results
     */
    @Nullable
    public TabComponent getNumberFormat(@NotNull TabPlayer player) {
        return cache.get(player.scoreboardData.numberFormatProperties.get(line).updateAndGet());
    }

    @Override
    @NotNull
    public ThreadExecutor getCustomThread() {
        return line.getCustomThread();
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return line.getFeatureName();
    }
}
