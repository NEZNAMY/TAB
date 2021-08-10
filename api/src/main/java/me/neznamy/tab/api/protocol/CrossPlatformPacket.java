package me.neznamy.tab.api.protocol;

/**
 * Interface representing a packet that can be sent to client independently of platform
 */
public interface CrossPlatformPacket {

	/**
	 * Returns user-friendly string representation of the packet
	 * @return user-friendly string representation of the packet
	 */
	public String toString();
}