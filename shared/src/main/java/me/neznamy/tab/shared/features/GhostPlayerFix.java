package me.neznamy.tab.shared.features;

import lombok.Getter;
import me.neznamy.tab.api.feature.QuitListener;
import me.neznamy.tab.api.feature.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.TabConstants;
import me.neznamy.tab.shared.TAB;

/**
 * A small class fixing bugs in other plugins
 */
public class GhostPlayerFix extends TabFeature implements QuitListener {

    @Getter private final String featureName = "Ghost player fix";

    @Override
    public void onQuit(TabPlayer disconnectedPlayer) {
        TAB.getInstance().getCPUManager().runTaskLater(500, this, TabConstants.CpuUsageCategory.PLAYER_QUIT, () -> {

            if (TAB.getInstance().getPlayer(disconnectedPlayer.getName()) != null) return; //player reconnected meanwhile, not removing then
            for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
                if (all == disconnectedPlayer) continue;
                all.getTabList().removeEntry(disconnectedPlayer.getTablistId());
            }
        });
    }
}