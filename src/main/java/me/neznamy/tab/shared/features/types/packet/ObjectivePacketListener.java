package me.neznamy.tab.shared.features.types.packet;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.types.Feature;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective;

/**
 * Classes implementing this interface will receive objective packet
 */
public interface ObjectivePacketListener extends Feature {

	/**
	 * Processes the packet send
	 * @param receiver - player receiving packet
	 * @param packet - received packet
	 */
	public void onPacketSend(TabPlayer receiver, PacketPlayOutScoreboardObjective packet);
}