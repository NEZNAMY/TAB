package me.neznamy.tab.shared.features.types.packet;

import me.neznamy.tab.api.TabPlayer;

/**
 * Classes implementing this interface will receive login packet
 */
public interface LoginPacketListener {

	/**
	 * Processes login packet, only available on bungeecord
	 * @param packetReceiver - player receiving client reset packet
	 */
	public void onLoginPacket(TabPlayer packetReceiver);
}