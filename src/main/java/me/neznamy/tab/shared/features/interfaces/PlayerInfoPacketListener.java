package me.neznamy.tab.shared.features.interfaces;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;

/**
 * Classes implementing this interface will receive PacketPlayOutPlayerInfo
 */
public interface PlayerInfoPacketListener extends Feature {

	public PacketPlayOutPlayerInfo onPacketSend(TabPlayer receiver, PacketPlayOutPlayerInfo info);
}