package me.neznamy.tab.shared.features.globalplayerlist;

import lombok.Getter;
import me.neznamy.tab.api.feature.Refreshable;
import me.neznamy.tab.api.feature.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.shared.TAB;

public class LatencyRefresher extends TabFeature implements Refreshable {

    @Getter private final String featureName = "Global PlayerList";
    @Getter private final String refreshDisplayName = "Updating latency";

    {
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
