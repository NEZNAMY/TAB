package me.neznamy.tab.shared.features.interfaces;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective;

public interface ObjectivePacketListener {

	public void onPacketSend(TabPlayer receiver, PacketPlayOutScoreboardObjective packet);
}