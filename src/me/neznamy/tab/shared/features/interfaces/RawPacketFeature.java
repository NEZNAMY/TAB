package me.neznamy.tab.shared.features.interfaces;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.cpu.CPUFeature;

public interface RawPacketFeature {

	public Object onPacketReceive(ITabPlayer sender, Object packet) throws Throwable;
	public Object onPacketSend(ITabPlayer receiver, Object packet) throws Throwable;
	public CPUFeature getCPUName();
}
