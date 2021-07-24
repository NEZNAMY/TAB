package me.neznamy.tab.api.protocol;

import java.lang.reflect.InvocationTargetException;

import me.neznamy.tab.api.ProtocolVersion;

/**
 * Interface representing a packet that can be sent to client independently of platform
 */
public interface CrossPlatformPacket {
	
	/**
	 * Converts the class into raw packet
	 * @param clientVersion - version of player to create packet for
	 * @return the raw packet
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public Object build(ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException;
}