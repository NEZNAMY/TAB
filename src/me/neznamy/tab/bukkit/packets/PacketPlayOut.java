package me.neznamy.tab.bukkit.packets;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.Shared;

public abstract class PacketPlayOut extends NMSClass{

	public abstract Object toNMS() throws Exception;
	
	public void send(ITabPlayer to) {
		try {
			to.sendPacket(toNMS());
		} catch (Exception e) {
			Shared.error("An error occured when creating " + getClass().getSimpleName(), e);
		}
	}
}