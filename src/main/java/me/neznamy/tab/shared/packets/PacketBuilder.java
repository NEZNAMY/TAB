package me.neznamy.tab.shared.packets;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * An interface represending a platform-specific packet builder
 */
public interface PacketBuilder {

	/**
	 * Constructs platform-specific PacketPlayOutBoss class based on custom packet class
	 * @param packet - custom packet to be built
	 * @param clientVersion - version of client to receive the packet
	 * @return platform-specific packet
	 * @throws Exception - when something fails
	 */
	public Object build(PacketPlayOutBoss packet, ProtocolVersion clientVersion) throws Exception;
	
	/**
	 * Constructs platform-specific PacketPlayOutChat class based on custom packet class
	 * @param packet - custom packet to be built
	 * @param clientVersion - version of client to receive the packet
	 * @return platform-specific packet
	 * @throws Exception - when something fails
	 */
	public Object build(PacketPlayOutChat packet, ProtocolVersion clientVersion) throws Exception;
	
	/**
	 * Constructs platform-specific PacketPlayOutPlayerInfo class based on custom packet class
	 * @param packet - custom packet to be built
	 * @param clientVersion - version of client to receive the packet
	 * @return platform-specific packet
	 * @throws Exception - when something fails
	 */
	public Object build(PacketPlayOutPlayerInfo packet, ProtocolVersion clientVersion) throws Exception;
	
	/**
	 * Constructs platform-specific PacketPlayOutPlayerListHeaderFooter class based on custom packet class
	 * @param packet - custom packet to be built
	 * @param clientVersion - version of client to receive the packet
	 * @return platform-specific packet
	 * @throws Exception - when something fails
	 */
	public Object build(PacketPlayOutPlayerListHeaderFooter packet, ProtocolVersion clientVersion) throws Exception;
	
	/**
	 * Constructs platform-specific PacketPlayOutScoreboardDisplayObjective class based on custom packet class
	 * @param packet - custom packet to be built
	 * @param clientVersion - version of client to receive the packet
	 * @return platform-specific packet
	 * @throws Exception - when something fails
	 */
	public Object build(PacketPlayOutScoreboardDisplayObjective packet, ProtocolVersion clientVersion) throws Exception;
	
	/**
	 * Constructs platform-specific PacketPlayOutScoreboardObjective class based on custom packet class
	 * @param packet - custom packet to be built
	 * @param clientVersion - version of client to receive the packet
	 * @return platform-specific packet
	 * @throws Exception - when something fails
	 */
	public Object build(PacketPlayOutScoreboardObjective packet, ProtocolVersion clientVersion) throws Exception;
	
	/**
	 * Constructs platform-specific PacketPlayOutScoreboardScore class based on custom packet class
	 * @param packet - custom packet to be built
	 * @param clientVersion - version of client to receive the packet
	 * @return platform-specific packet
	 * @throws Exception - when something fails
	 */
	public Object build(PacketPlayOutScoreboardScore packet, ProtocolVersion clientVersion) throws Exception;
	
	/**
	 * Constructs platform-specific PacketPlayOutScoreboardTeam class based on custom packet class
	 * @param packet - custom packet to be built
	 * @param clientVersion - version of client to receive the packet
	 * @return platform-specific packet
	 * @throws Exception - when something fails
	 */
	public Object build(PacketPlayOutScoreboardTeam packet, ProtocolVersion clientVersion) throws Exception;

	/**
	 * Cuts given string to specified character length (or length-1 if last character is a color character)
	 * @param string - string to cut
	 * @param length - length to cut to
	 * @return the cut text or original if cut was not needed
	 */
	public default String cutTo(String string, int length) {
		if (string == null || string.length() <= length) return string;
		if (string.charAt(length-1) == Placeholders.colorChar) {
			return string.substring(0, length-1); //cutting one extra character to prevent prefix ending with "&"
		} else {
			return string.substring(0, length);
		}
	}
}