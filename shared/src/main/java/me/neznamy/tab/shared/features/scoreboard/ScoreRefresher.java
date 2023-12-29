package me.neznamy.tab.shared.features.scoreboard;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.shared.features.scoreboard.lines.ScoreboardLine;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Feature refreshing NumberFormat in scoreboard for players.
 */
@RequiredArgsConstructor
public class ScoreRefresher extends TabFeature implements Refreshable {

    private final String NUMBER_FORMAT_PROPERTY = Property.randomName();
    @Getter private final String featureName = "Scoreboard";
    @Getter private final String refreshDisplayName = "Updating scores";

    /** Line this score belongs to */
    private final ScoreboardLine line;

    /** Configured number format */
    private final String numberFormat;

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        if (!line.getParent().getPlayers().contains(refreshed)) return;
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
    public IChatBaseComponent getNumberFormat(@NotNull TabPlayer player) {
        return IChatBaseComponent.emptyToNullOptimizedComponent(player.getProperty(NUMBER_FORMAT_PROPERTY).updateAndGet());
    }
}
