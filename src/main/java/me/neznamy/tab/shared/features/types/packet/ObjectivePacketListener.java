package me.neznamy.tab.shared.features.types.packet;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.types.Feature;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective;

public interface ObjectivePacketListener extends Feature {

	public void onPacketSend(TabPlayer receiver, PacketPlayOutScoreboardObjective packet);
}