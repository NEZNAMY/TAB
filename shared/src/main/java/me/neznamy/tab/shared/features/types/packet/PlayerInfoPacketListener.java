package me.neznamy.tab.shared.features.types.packet;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.types.Feature;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;

/**
 * Classes implementing this interface will receive PacketPlayOutPlayerInfo
 */
public interface PlayerInfoPacketListener extends Feature {

	/**
	 * Processes the packet send and possibly modifies it
	 * @param receiver - player receiving packet
	 * @param packet - received packet
	 */
	public void onPacketSend(TabPlayer receiver, PacketPlayOutPlayerInfo info);
}