package me.neznamy.tab.shared.features.globalplayerlist;

import lombok.Getter;
import me.neznamy.tab.shared.player.TabPlayer;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;

public class LatencyRefresher extends TabFeature implements Refreshable {

    @Getter private final String featureName = "Global PlayerList";
    @Getter private final String refreshDisplayName = "Updating latency";

    public LatencyRefresher() {
        TAB.getInstance().getPlaceholderManager().addUsedPlaceholder(TabConstants.Placeholder.PING, this);
    }

    @Override
    public void refresh(TabPlayer p, boolean force) {
        //player ping changed, must manually update latency for players on other servers
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (!p.getServer().equals(all.getServer())) all.getTabList().updateLatency(p.getTablistId(), p.getPing());
        }
    }
}
