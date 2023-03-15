package me.neznamy.tab.shared.features.layout;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.api.TabConstants;

@RequiredArgsConstructor
public class LayoutLatencyRefresher extends TabFeature {

    @Getter private final String featureName = "Layout";
    @Getter private final String refreshDisplayName = "Updating latency";
    private final LayoutManager manager;

    {
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
            all.sendCustomPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_LATENCY,
                    new PacketPlayOutPlayerInfo.PlayerInfoData(slot.getUniqueId(), p.getPing())));
        }
    }
}
