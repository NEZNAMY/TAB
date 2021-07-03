package me.neznamy.tab.shared.features.types.packet;

import java.lang.reflect.InvocationTargetException;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.features.types.Feature;

/**
 * Classes implementing this interface will receive raw packets
 */
public interface RawPacketListener extends Feature {

	/**
	 * Processes raw packet sent by client
	 * @param sender - packet sender
	 * @param packet - packet received
	 * @return modified packet or null if packet should be cancelled
	 * @throws IllegalAccessException 
	 * @throws ClassNotFoundException 
	 */
	public Object onPacketReceive(TabPlayer sender, Object packet) throws IllegalAccessException;
	
	/**
	 * Processes raw packet sent to client
	 * @param receiver - packet receiver
	 * @param packet - the packet
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws InstantiationException 
	 */
	public void onPacketSend(TabPlayer receiver, Object packet) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException, InstantiationException;
}