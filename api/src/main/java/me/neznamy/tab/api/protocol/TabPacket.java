package me.neznamy.tab.api.protocol;

/**
 * Interface representing a packet that can be sent to client independently of platform.
 * It is technically empty, existence is only present to restrict type access
 * when sending packets to player, so {@link me.neznamy.tab.api.TabPlayer#sendCustomPacket(TabPacket)} cannot
 * be called with an actual packet.
 */
public interface TabPacket {

	/**
	 * Returns user-friendly string representation of the packet
	 * @return user-friendly string representation of the packet
	 */
	String toString();
}