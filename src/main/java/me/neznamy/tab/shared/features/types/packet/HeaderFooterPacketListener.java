package me.neznamy.tab.shared.features.types.packet;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.types.Feature;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerListHeaderFooter;

public interface HeaderFooterPacketListener extends Feature {

	public boolean onPacketSend(TabPlayer packetReciver, PacketPlayOutPlayerListHeaderFooter packet);
}