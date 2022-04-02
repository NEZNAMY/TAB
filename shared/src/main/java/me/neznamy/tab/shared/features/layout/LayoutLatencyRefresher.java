package me.neznamy.tab.shared.features.layout;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;

public class LayoutLatencyRefresher extends TabFeature {

    private final LayoutManager manager;

    public LayoutLatencyRefresher(LayoutManager manager) {
        super(manager.getFeatureName(), "Updating latency");
        this.manager = manager;
        TAB.getInstance().getPlaceholderManager().addUsedPlaceholder("%ping%", this);
    }

    @Override
    public void refresh(TabPlayer p, boolean force) {
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            PlayerSlot slot = manager.getPlayerViews().get(all).getSlot(p);
            if (slot != null) {
                all.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_LATENCY,
                        new PacketPlayOutPlayerInfo.PlayerInfoData(slot.getUUID(), p.getPing())), TabConstants.PacketCategory.LAYOUT_LATENCY);
            }
        }
    }
}
