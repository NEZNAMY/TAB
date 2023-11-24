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

@RequiredArgsConstructor
public class ScoreRefresher extends TabFeature implements Refreshable {

    private final String NUMBER_FORMAT_PROPERTY = Property.randomName();
    @Getter private final String featureName = "Scoreboard";
    @Getter private final String refreshDisplayName = "Updating scores";

    private final ScoreboardLine line;
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

    public void registerProperties(@NotNull TabPlayer player) {
        player.setProperty(this, NUMBER_FORMAT_PROPERTY, numberFormat);
    }

    @Nullable
    public IChatBaseComponent getNumberFormat(@NotNull TabPlayer player) {
        return IChatBaseComponent.emptyToNullOptimizedComponent(player.getProperty(NUMBER_FORMAT_PROPERTY).updateAndGet());
    }
}
