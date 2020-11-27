package me.neznamy.tab.shared.features.interfaces;

import me.neznamy.tab.api.TabPlayer;

public interface LoginPacketListener {

	public void onLoginPacket(TabPlayer packetReceiver);
}