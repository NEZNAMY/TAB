package me.neznamy.tab.shared.features.types.packet;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.types.Feature;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardDisplayObjective;

/**
 * Classes implementing this interface will receive display objective packet
 */
public interface DisplayObjectivePacketListener extends Feature {

	/**
	 * Processes the packet send and returns true if packet should be cancelled
	 * @param receiver - player receiving packet
	 * @param packet - received packet
	 * @return true if packet should be cancelled, false if not
	 */
	public boolean onPacketSend(TabPlayer receiver, PacketPlayOutScoreboardDisplayObjective packet);
}