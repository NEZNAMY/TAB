package me.neznamy.tab.shared.features.globalplayerlist;

import lombok.Getter;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;
import org.jetbrains.annotations.NotNull;

@Getter
public class LatencyRefresher extends TabFeature implements Refreshable {

    private final String featureName = "Global PlayerList";
    private final String refreshDisplayName = "Updating latency";

    public LatencyRefresher() {
        TAB.getInstance().getPlaceholderManager().addUsedPlaceholder(TabConstants.Placeholder.PING, this);
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        //player ping changed, must manually update latency for players on other servers
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (!p.getServer().equals(all.getServer())) all.getTabList().updateLatency(p.getTablistId(), p.getPing());
        }
    }
}
