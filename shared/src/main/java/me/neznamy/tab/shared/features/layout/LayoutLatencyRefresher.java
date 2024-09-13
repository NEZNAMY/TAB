package me.neznamy.tab.shared.features.layout;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.types.RefreshableFeature;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Layout sub-feature updating latency of layout entries to match player latencies.
 */
public class LayoutLatencyRefresher extends RefreshableFeature {

    /**
     * Constructs new instance.
     */
    public LayoutLatencyRefresher() {
        addUsedPlaceholder(TabConstants.Placeholder.PING);
    }

    @NotNull
    @Override
    public String getFeatureName() {
        return "Layout";
    }

    @NotNull
    @Override
    public String getRefreshDisplayName() {
        return "Updating latency";
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all.getVersion().getMinorVersion() < 8) continue;
            if (all.layoutData.currentLayout == null) continue;
            PlayerSlot slot = all.layoutData.currentLayout.view.getSlot(p);
            if (slot == null) continue;
            all.getTabList().updateLatency(slot.getUniqueId(), p.getPing());
        }
    }
}
