package me.neznamy.tab.shared.features;

import lombok.Getter;
import me.neznamy.tab.api.feature.*;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Sets ping of all players in the packet to configured value to prevent hacked clients from seeing exact ping value of each player
 */
public class PingSpoof extends TabFeature implements JoinListener, LatencyListener, Loadable, UnLoadable {

    //fake ping value
    private final int value = TAB.getInstance().getConfiguration().getConfig().getInt("ping-spoof.value", 0);

    @Getter private final String featureName = "Ping spoof";

    @Override
    public int onLatencyChange(TabPlayer packetReceiver, UUID id, int latency) {
        if (TAB.getInstance().getPlayerByTabListUUID(id) != null) return value;
        return latency;
    }

    @Override
    public void load() {
        updateAll(false);
    }

    @Override
    public void onJoin(TabPlayer p) {
        Map<UUID, Integer> map = new HashMap<>();
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            map.put(all.getUniqueId(), value);
            all.getTabList().updateLatency(p.getTablistId(), value);
        }
        p.getTabList().updateLatencies(map);
    }

    @Override
    public void unload() {
        updateAll(true);
    }

    private void updateAll(boolean realPing) {
        Map<UUID, Integer> map = new HashMap<>();
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            map.put(p.getUniqueId(), realPing ? p.getPing() : value);
        }
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            p.getTabList().updateLatencies(map);
        }
    }
}