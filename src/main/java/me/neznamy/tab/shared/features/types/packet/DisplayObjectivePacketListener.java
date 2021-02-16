package me.neznamy.tab.shared.features.types.packet;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.types.Feature;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardDisplayObjective;

public interface DisplayObjectivePacketListener extends Feature {

	public boolean onPacketSend(TabPlayer receiver, PacketPlayOutScoreboardDisplayObjective packet);
}