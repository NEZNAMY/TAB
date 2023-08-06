package me.neznamy.tab.shared.features;

import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This feature hides real ping of players in connection bar and
 * replaces it with a custom fake value.
 */
public class PingSpoof extends TabFeature implements JoinListener, LatencyListener, Loadable, UnLoadable {

    /** Feature name in CPU report */
    @Getter private final String featureName = "Ping spoof";

    /** Value to display as ping instead of real ping */
    private final int value = TAB.getInstance().getConfig().getInt("ping-spoof.value", 0);

    @Override
    public int onLatencyChange(TabPlayer packetReceiver, UUID id, int latency) {
        return value;
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
            connectedPlayer.getTabList().updateLatency(all.getTablistId(), value);
            all.getTabList().updateLatency(connectedPlayer.getTablistId(), value);
        }
    }

    private void updateAll(boolean realPing) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
                viewer.getTabList().updateLatency(target.getTablistId(), realPing ? target.getPing() : value);
            }
        }
    }
}
