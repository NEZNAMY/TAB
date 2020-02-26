package me.neznamy.tab.shared.features;

import me.neznamy.tab.shared.ITabPlayer;

public interface RawPacketFeature {

	public Object onPacketReceive(ITabPlayer sender, Object packet) throws Throwable;
	public Object onPacketSend(ITabPlayer receiver, Object packet) throws Throwable;
	public String getCPUName();
}
