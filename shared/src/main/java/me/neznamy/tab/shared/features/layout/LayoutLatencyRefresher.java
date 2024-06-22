package me.neznamy.tab.shared.features.layout;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

public class LayoutLatencyRefresher extends TabFeature implements Refreshable {

    @NotNull
    private final LayoutManagerImpl manager;

    public LayoutLatencyRefresher(@NotNull LayoutManagerImpl manager) {
        this.manager = manager;
        addUsedPlaceholder(TabConstants.Placeholder.PING);
    }

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        for (TabPlayer all : TAB.getInstance().onlinePlayers()) {
            if (all.getVersion().getMinorVersion() < 8) continue;
            if (all.layoutData.view == null) continue;
            PlayerSlot slot = all.layoutData.view.getSlot(p);
            if (slot == null) continue;
            all.getTabList().updateLatency(slot.getUniqueId(), p.getPing());
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
        return manager.getFeatureName();
    }
}
