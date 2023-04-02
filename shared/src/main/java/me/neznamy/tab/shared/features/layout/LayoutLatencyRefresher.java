package me.neznamy.tab.shared.features.layout;

import lombok.Getter;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.player.TabPlayer;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.TAB;

public class LayoutLatencyRefresher extends TabFeature implements Refreshable {

    @Getter private final String featureName = "Layout";
    @Getter private final String refreshDisplayName = "Updating latency";
    private final LayoutManager manager;

    public LayoutLatencyRefresher(LayoutManager manager) {
        this.manager = manager;
        TAB.getInstance().getPlaceholderManager().addUsedPlaceholder(TabConstants.Placeholder.PING, this);
    }

    @Override
    public void refresh(TabPlayer p, boolean force) {
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            if (all.getVersion().getMinorVersion() < 8) continue;
            Layout layout = manager.getPlayerViews().get(all);
            if (layout == null) continue;
            PlayerSlot slot = layout.getSlot(p);
            if (slot == null) continue;
            all.getTabList().updateLatency(slot.getUniqueId(), p.getPing());
        }
    }
}
