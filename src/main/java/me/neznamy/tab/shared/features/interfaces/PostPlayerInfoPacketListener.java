package me.neznamy.tab.shared.features.interfaces;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;

/**
 * Classes implementing this interface will receive PacketPlayOutPlayerInfo after the packet was successfully sent
 */
public interface PostPlayerInfoPacketListener extends Feature {

	public void postPacket(TabPlayer receiver, PacketPlayOutPlayerInfo info);
}