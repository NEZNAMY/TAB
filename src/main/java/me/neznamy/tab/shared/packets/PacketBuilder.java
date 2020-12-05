package me.neznamy.tab.shared.packets;

import me.neznamy.tab.shared.ProtocolVersion;

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
	 * Constructs platform-specific PacketPlayOutTitle class based on custom packet class
	 * @param packet - custom packet to be built
	 * @param clientVersion - version of client to receive the packet
	 * @return platform-specific packet
	 * @throws Exception - when something fails
	 */
	public Object build(PacketPlayOutTitle packet, ProtocolVersion clientVersion) throws Exception;

	/**
	 * Cuts given string to specified character length (or length-1 if last character is a color character) and translates RGB to legacy colors
	 * @param string - string to cut
	 * @param length - length to cut to
	 * @return the cut text or original if cut was not needed
	 */
	public default String cutTo(String string, int length) {
		if (string == null) return null;
		String legacyText = IChatBaseComponent.fromColoredText(string).toLegacyText();
		if (legacyText.length() <= length) return legacyText;
		if (legacyText.charAt(length-1) == '\u00a7') {
			return legacyText.substring(0, length-1); //cutting one extra character to prevent prefix ending with "&"
		} else {
			return legacyText.substring(0, length);
		}
	}
	
	/**
	 * Returns the text as json component for 1.13+ clients or cut text for 1.12-
	 * @param text - text to convert
	 * @param clientVersion - client version
	 * @return component for 1.13+ clients, cut string for 1.12-
	 */
	public default String jsonOrCut(String text, ProtocolVersion clientVersion, int length) {
		if (clientVersion.getMinorVersion() >= 13) {
			return IChatBaseComponent.optimizedComponent(text).toString(clientVersion);
		} else {
			return cutTo(text, length);
		}
	}
	
	public PacketPlayOutPlayerInfo readPlayerInfo(Object packet, ProtocolVersion clientVersion) throws Exception;
	
	public PacketPlayOutScoreboardObjective readObjective(Object packet, ProtocolVersion clientVersion) throws Exception;
	
	public PacketPlayOutScoreboardDisplayObjective readDisplayObjective(Object packet, ProtocolVersion clientVersion) throws Exception;
}