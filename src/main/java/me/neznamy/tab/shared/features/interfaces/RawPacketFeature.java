package me.neznamy.tab.shared.features.interfaces;

import me.neznamy.tab.shared.ITabPlayer;

/**
 * Classes implementing this interface will receive raw packets
 */
public interface RawPacketFeature extends Feature {

	public Object onPacketReceive(ITabPlayer sender, Object packet) throws Throwable;
	public Object onPacketSend(ITabPlayer receiver, Object packet) throws Throwable;
}