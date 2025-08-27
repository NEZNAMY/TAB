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
 * Feature refreshing NumberFormat in scoreboard for players.
 */
@RequiredArgsConstructor
public class ScoreRefresher extends RefreshableFeature implements CustomThreaded {

    private static final StringToComponentCache cache = new StringToComponentCache("Scoreboard NumberFormat", 1000);

    /** Line this score belongs to */
    @NonNull private final ScoreboardLine line;

    /** Configured number format */
    @NonNull private final String numberFormat;

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
        player.scoreboardData.numberFormatProperties.put(line, new Property(this, player, numberFormat));
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
