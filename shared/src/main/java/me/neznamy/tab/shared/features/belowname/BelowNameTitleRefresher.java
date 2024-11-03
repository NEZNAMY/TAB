package me.neznamy.tab.shared.features.belowname;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Feature for updating belowname title.
 */
@RequiredArgsConstructor
public class BelowNameTitleRefresher extends RefreshableFeature implements CustomThreaded {

    @NotNull
    private final BelowName feature;

    @NotNull
    private final ThreadExecutor customThread;

    @NotNull
    @Override
    public String getFeatureName() {
        return "BelowName";
    }

    @NotNull
    @Override
    public String getRefreshDisplayName() {
        return "Updating BelowName title";
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        if (refreshed.belowNameData.disabled.get()) return;
        refreshed.getScoreboard().updateObjective(
                BelowName.OBJECTIVE_NAME,
                feature.getCache().get(refreshed.belowNameData.text.updateAndGet()),
                Scoreboard.HealthDisplay.INTEGER,
                feature.getCache().get(refreshed.belowNameData.defaultNumberFormat.updateAndGet())
        );
    }

    @Override
    @NotNull
    public ThreadExecutor getCustomThread() {
        return customThread;
    }
}
