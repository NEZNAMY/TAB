package me.neznamy.tab.shared.features;

import me.neznamy.tab.api.TabFeature;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import me.neznamy.tab.api.protocol.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.TAB;

/**
 * A small class fixing bugs in other plugins
 */
public class GhostPlayerFix extends TabFeature {

	public GhostPlayerFix() {
		super("Ghost player fix", null);
		TAB.getInstance().debug("Loaded GhostPlayerFix feature");
	}
	
	@Override
	public void onQuit(TabPlayer disconnectedPlayer) {
		TAB.getInstance().getCPUManager().runTaskLater(500, "removing players", this, TabConstants.CpuUsageCategory.PLAYER_QUIT, () -> {

			if (TAB.getInstance().getPlayer(disconnectedPlayer.getName()) != null) return; //player reconnected meanwhile, not removing then
			for (TabPlayer all : TAB.getInstance().getOnlinePlayers()) {
				if (all == disconnectedPlayer) continue;
				all.sendCustomPacket(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, new PlayerInfoData(disconnectedPlayer.getUniqueId())), this);
			}
		});
	}
}