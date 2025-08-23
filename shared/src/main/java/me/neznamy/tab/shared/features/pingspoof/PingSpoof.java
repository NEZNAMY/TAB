package me.neznamy.tab.shared.features.pingspoof;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.platform.decorators.TrackedTabList;
import org.jetbrains.annotations.NotNull;

/**
 * This feature hides real ping of players in connection bar and
 * replaces it with a custom fake value.
 */
@Getter
@RequiredArgsConstructor
public class PingSpoof extends TabFeature implements JoinListener, Loadable, UnLoadable, CustomThreaded {

    @Getter
    private final ThreadExecutor customThread = new ThreadExecutor("TAB Ping Spoof Thread");

    /** Value to display as ping instead of real ping */
    private final PingSpoofConfiguration configuration;

    @Override
    public void load() {
        TrackedTabList.setForcedLatency(configuration.getValue());
        updateAll(false);
    }

    @Override
    public void unload() {
        TrackedTabList.setForcedLatency(null);
        updateAll(true);
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            connectedPlayer.getTabList().updateLatency(all, configuration.getValue());
            all.getTabList().updateLatency(connectedPlayer, configuration.getValue());
        }
    }

    private void updateAll(boolean realPing) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
                viewer.getTabList().updateLatency(target, realPing ? target.getPing() : configuration.getValue());
            }
        }
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return "Ping spoof";
    }
}
