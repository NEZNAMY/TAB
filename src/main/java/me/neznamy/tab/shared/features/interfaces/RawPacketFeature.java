package me.neznamy.tab.shared.features.interfaces;

import me.neznamy.tab.api.TabPlayer;

/**
 * Classes implementing this interface will receive raw packets
 */
public interface RawPacketFeature extends Feature {

	public Object onPacketReceive(TabPlayer sender, Object packet) throws Throwable;
	public Object onPacketSend(TabPlayer receiver, Object packet) throws Throwable;
}