package me.neznamy.tab.shared.features.types.packet;

import me.neznamy.tab.api.TabPlayer;

public interface LoginPacketListener {

	public void onLoginPacket(TabPlayer packetReceiver);
}