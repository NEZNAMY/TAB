package me.neznamy.tab.shared.features.playerlistobjective;

import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.chat.component.TabComponent;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.types.CustomThreaded;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Feature for updating playerlist objective title.
 */
@RequiredArgsConstructor
public class PlayerListObjectiveTitleRefresher extends RefreshableFeature implements CustomThreaded {

    @NotNull
    private final YellowNumber feature;

    @NotNull
    @Override
    public String getFeatureName() {
        return feature.getFeatureName();
    }

    @NotNull
    @Override
    public String getRefreshDisplayName() {
        return "Updating Playerlist Objective title";
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        if (refreshed.playerlistObjectiveData.disabled.get()) return;
        refreshed.getScoreboard().updateObjective(
                YellowNumber.OBJECTIVE_NAME,
                feature.getCache().get(refreshed.playerlistObjectiveData.title.updateAndGet()),
                feature.getConfiguration().getHealthDisplay(),
                TabComponent.empty()
        );
    }

    @Override
    @NotNull
    public ThreadExecutor getCustomThread() {
        return feature.getCustomThread();
    }
}
