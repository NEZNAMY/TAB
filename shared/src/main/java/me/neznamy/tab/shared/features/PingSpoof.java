package me.neznamy.tab.shared.features;

import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.features.layout.LayoutManagerImpl;
import me.neznamy.tab.shared.features.layout.ParentGroup;
import me.neznamy.tab.shared.features.layout.PlayerSlot;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * This feature hides real ping of players in connection bar and
 * replaces it with a custom fake value.
 */
public class PingSpoof extends TabFeature implements JoinListener, LatencyListener, Loadable, UnLoadable {

    /** Value to display as ping instead of real ping */
    private final int value = config().getInt("ping-spoof.value", 0);

    private LayoutManagerImpl layoutManager;

    @Override
    public int onLatencyChange(@NotNull TabPlayer packetReceiver, @NotNull UUID id, int latency) {
        if (layoutManager != null) {
            if (packetReceiver.layoutData.view != null) {
                for (ParentGroup group : packetReceiver.layoutData.view.getGroups()) {
                    PlayerSlot slot = group.getPlayerSlots().get((int) id.getLeastSignificantBits());
                    if (slot != null && slot.getPlayer() != null) return value;
                }
            }
        }
        if (TAB.getInstance().getPlayer(id) != null) return value;
        return latency;
    }

    @Override
    public void load() {
        layoutManager = TAB.getInstance().getFeatureManager().getFeature(TabConstants.Feature.LAYOUT);
        updateAll(false);
    }

    @Override
    public void unload() {
        updateAll(true);
    }

    @Override
    public void onJoin(@NotNull TabPlayer connectedPlayer) {
        for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
            connectedPlayer.getTabList().updateLatency(all.getTablistId(), value);
            all.getTabList().updateLatency(connectedPlayer.getTablistId(), value);
        }
    }

    private void updateAll(boolean realPing) {
        for (TabPlayer viewer : TAB.getInstance().getOnlinePlayers()) {
            for (TabPlayer target : TAB.getInstance().getOnlinePlayers()) {
                viewer.getTabList().updateLatency(target.getTablistId(), realPing ? target.getPing() : value);
            }
        }
    }

    @Override
    @NotNull
    public String getFeatureName() {
        return "Ping spoof";
    }
}
