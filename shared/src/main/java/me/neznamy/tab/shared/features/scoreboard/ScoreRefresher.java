package me.neznamy.tab.shared.features.scoreboard;

import lombok.NonNull;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.chat.TabComponent;
import me.neznamy.tab.shared.features.scoreboard.lines.ScoreboardLine;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Feature refreshing NumberFormat in scoreboard for players.
 */
public class ScoreRefresher extends RefreshableFeature implements CustomThreaded {

    private final String NUMBER_FORMAT_PROPERTY = Property.randomName();

    /** Line this score belongs to */
    private final ScoreboardLine line;

    /** Configured number format */
    private final String numberFormat;

    /**
     * Constructs new instance with given parameters.
     *
     * @param   line
     *          Line this NumberFormat belongs to
     * @param   numberFormat
     *          Configured number format
     */
    public ScoreRefresher(@NonNull ScoreboardLine line, @NonNull String numberFormat) {
        super(line.getFeatureName(), "Updating NumberFormat");
        this.line = line;
        this.numberFormat = numberFormat;
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        if (!line.getParent().getPlayers().contains(refreshed)) return;
        if (refreshed.getProperty(NUMBER_FORMAT_PROPERTY) == null) return; // Shrug
        refreshed.getScoreboard().setScore(
                ScoreboardManagerImpl.OBJECTIVE_NAME,
                line.getPlayerName(refreshed),
                line.getNumber(refreshed),
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
        player.setProperty(this, NUMBER_FORMAT_PROPERTY, numberFormat);
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
        return TabComponent.optimized(player.getProperty(NUMBER_FORMAT_PROPERTY).updateAndGet());
    }

    @Override
    @NotNull
    public ScheduledExecutorService getCustomThread() {
        return line.getCustomThread();
    }
}
