package me.neznamy.tab.shared.features.types.packet;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.types.Feature;

/**
 * Classes implementing this interface will receive raw packets
 */
public interface RawPacketListener extends Feature {

	public Object onPacketReceive(TabPlayer sender, Object packet) throws Throwable;
	public void onPacketSend(TabPlayer receiver, Object packet) throws Throwable;
}