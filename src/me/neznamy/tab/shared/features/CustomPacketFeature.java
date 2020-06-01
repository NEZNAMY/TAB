package me.neznamy.tab.shared.features;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.cpu.CPUFeature;
import me.neznamy.tab.shared.packets.UniversalPacketPlayOut;

public interface CustomPacketFeature {

	public UniversalPacketPlayOut onPacketSend(ITabPlayer receiver, UniversalPacketPlayOut packet);
	public CPUFeature getCPUName();
}
