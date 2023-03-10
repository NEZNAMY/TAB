package me.neznamy.tab.shared.features;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.layout.Layout;
import me.neznamy.tab.shared.features.layout.LayoutManager;
import me.neznamy.tab.shared.features.layout.ParentGroup;
import me.neznamy.tab.shared.features.layout.PlayerSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Sets ping of all players in the packet to configured value to prevent hacked clients from seeing exact ping value of each player
 */
public class PingSpoof extends TabFeature {

    //fake ping value
    private final int value = TAB.getInstance().getConfiguration().getConfig().getInt("ping-spoof.value", 0);

    private final LayoutManager layoutManager;

    /**
     * Constructs new instance and loads config options
     */
    public PingSpoof(LayoutManager layoutManager) {
        super("Ping spoof", null);
        this.layoutManager = layoutManager;
    }

    @Override
    public void onPlayerInfo(TabPlayer receiver, PacketPlayOutPlayerInfo info) {
        if (!info.getActions().contains(EnumPlayerInfoAction.UPDATE_LATENCY)) return;
        for (PlayerInfoData playerInfoData : info.getEntries()) {
            if (TAB.getInstance().getPlayerByTabListUUID(playerInfoData.getUniqueId()) != null) playerInfoData.setLatency(value);
            if (layoutManager != null) {
                Layout layout = layoutManager.getPlayerViews().get(receiver);
                if (layout != null) {
                    for (ParentGroup group : layout.getGroups()) {
                        for (Map.Entry<Integer, PlayerSlot> entry : group.getPlayerSlots().entrySet()) {
                            if (layoutManager.getUUID(entry.getKey()) == playerInfoData.getUniqueId() && entry.getValue().getPlayer() != null) {
                                playerInfoData.setLatency(value);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void load() {
        updateAll(false);
    }

    @Override
    public void unload() {
        updateAll(true);
    }

    private void updateAll(boolean realPing) {
        List<PlayerInfoData> list = new ArrayList<>();
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            list.add(new PlayerInfoData(p.getUniqueId(), realPing ? p.getPing() : value));
        }
        for (TabPlayer p : TAB.getInstance().getOnlinePlayers()) {
            p.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.UPDATE_LATENCY, list), this);
        }
    }
}