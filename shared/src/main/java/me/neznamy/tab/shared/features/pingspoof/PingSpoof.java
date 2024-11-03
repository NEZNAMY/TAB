package me.neznamy.tab.shared.features.pingspoof;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This feature hides real ping of players in connection bar and
 * replaces it with a custom fake value.
 */
@Getter
@RequiredArgsConstructor
public class PingSpoof extends TabFeature implements JoinListener, LatencyListener, Loadable, UnLoadable, CustomThreaded {

    @Getter
    private final ThreadExecutor customThread = new ThreadExecutor("TAB Ping Spoof Thread");

    /** Value to display as ping instead of real ping */
    private final PingSpoofConfiguration configuration;

    @Override
    public int onLatencyChange(@NotNull TabPlayer packetReceiver, @NotNull UUID id, int latency) {
        if (TAB.getInstance().getPlayerByTabListUUID(id) != null) return configuration.getValue();
        return latency;
    }

    @Override
    public void load() {
        updateAll(false);
    }

    @Override
    public void unload() {
        updateAll(true);
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            connectedPlayer.getTabList().updateLatency(all.getTablistId(), configuration.getValue());
            all.getTabList().updateLatency(connectedPlayer.getTablistId(), configuration.getValue());
        }
    }

    private void updateAll(boolean realPing) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
                viewer.getTabList().updateLatency(target.getTablistId(), realPing ? target.getPing() : configuration.getValue());
            }
        }
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return "Ping spoof";
    }
}
