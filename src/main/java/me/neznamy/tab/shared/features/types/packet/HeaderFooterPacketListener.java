package me.neznamy.tab.shared.features.types.packet;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.types.Feature;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerListHeaderFooter;

/**
 * Classes implementing this interface will receive header/footer packet
 */
public interface HeaderFooterPacketListener extends Feature {

	/**
	 * Processes packet send
	 * @param packetReciver - player receiving packet
	 * @param packet - the packet
	 * @return true if event should be cancelled, false if not
	 */
	public boolean onPacketSend(TabPlayer packetReciver, PacketPlayOutPlayerListHeaderFooter packet);
}