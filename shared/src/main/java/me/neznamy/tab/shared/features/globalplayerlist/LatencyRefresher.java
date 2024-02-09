package me.neznamy.tab.shared.features.globalplayerlist;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Sub-feature for Global Playerlist for refreshing player ping in tablist
 * of players on other servers.
 */
public class LatencyRefresher extends TabFeature implements Refreshable {

    /**
     * Constructs new instance and marks ping placeholder as used.
     */
    public LatencyRefresher() {
        addUsedPlaceholder(TabConstants.Placeholder.PING);
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        //player ping changed, must manually update latency for players on other servers
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (!p.getServer().equals(all.getServer())) all.getTabList().updateLatency(p.getTablistId(), p.getPing());
        }
    }

    @Override
    @NotNull
    public String getRefreshDisplayName() {
        return "Updating latency";
    }

    @Override
    @NotNull
    public String getFeatureName() {
        return "Global PlayerList";
    }
}
