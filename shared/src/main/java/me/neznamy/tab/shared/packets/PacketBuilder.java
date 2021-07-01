package me.neznamy.tab.shared.packets;

import java.lang.reflect.InvocationTargetException;

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
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public Object build(PacketPlayOutBoss packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException;
	
	/**
	 * Constructs platform-specific PacketPlayOutChat class based on custom packet class
	 * @param packet - custom packet to be built
	 * @param clientVersion - version of client to receive the packet
	 * @return platform-specific packet
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InstantiationException 
	 */
	public Object build(PacketPlayOutChat packet, ProtocolVersion clientVersion) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, SecurityException;
	
	/**
	 * Constructs platform-specific PacketPlayOutPlayerInfo class based on custom packet class
	 * @param packet - custom packet to be built
	 * @param clientVersion - version of client to receive the packet
	 * @return platform-specific packet
	 * @throws NegativeArraySizeException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public Object build(PacketPlayOutPlayerInfo packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException, NegativeArraySizeException;
	
	/**
	 * Constructs platform-specific PacketPlayOutPlayerListHeaderFooter class based on custom packet class
	 * @param packet - custom packet to be built
	 * @param clientVersion - version of client to receive the packet
	 * @return platform-specific packet
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public Object build(PacketPlayOutPlayerListHeaderFooter packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException;
	
	/**
	 * Constructs platform-specific PacketPlayOutScoreboardDisplayObjective class based on custom packet class
	 * @param packet - custom packet to be built
	 * @param clientVersion - version of client to receive the packet
	 * @return platform-specific packet
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public Object build(PacketPlayOutScoreboardDisplayObjective packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException;
	
	/**
	 * Constructs platform-specific PacketPlayOutScoreboardObjective class based on custom packet class
	 * @param packet - custom packet to be built
	 * @param clientVersion - version of client to receive the packet
	 * @return platform-specific packet
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public Object build(PacketPlayOutScoreboardObjective packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException;
	
	/**
	 * Constructs platform-specific PacketPlayOutScoreboardScore class based on custom packet class
	 * @param packet - custom packet to be built
	 * @param clientVersion - version of client to receive the packet
	 * @return platform-specific packet
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public Object build(PacketPlayOutScoreboardScore packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, SecurityException;
	
	/**
	 * Constructs platform-specific PacketPlayOutScoreboardTeam class based on custom packet class
	 * @param packet - custom packet to be built
	 * @param clientVersion - version of client to receive the packet
	 * @return platform-specific packet
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 */
	public Object build(PacketPlayOutScoreboardTeam packet, ProtocolVersion clientVersion) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, SecurityException;

	/**
	 * Cuts given string to specified character length (or length-1 if last character is a color character) and translates RGB to legacy colors
	 * @param string - string to cut
	 * @param length - length to cut to
	 * @return the cut text or original if cut was not needed
	 */
	public default String cutTo(String string, int length) {
		if (string == null) return "";
		String legacyText = IChatBaseComponent.optimizedComponent(string).toLegacyText();
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
		if (text == null) return null;
		if (clientVersion.getMinorVersion() >= 13) {
			return IChatBaseComponent.optimizedComponent(text).toString(clientVersion);
		} else {
			return cutTo(text, length);
		}
	}
	
	/**
	 * Converts platform-specific instance of player info packet into custom class object
	 * @param packet - platform-specific info packet
	 * @param clientVersion - client version
	 * @return The converted packet
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	public PacketPlayOutPlayerInfo readPlayerInfo(Object packet, ProtocolVersion clientVersion) throws IllegalAccessException, InvocationTargetException;
	
	/**
	 * Converts platform-specific instance of objective packet into custom class object
	 * @param packet - platform-specific objective packet
	 * @param clientVersion - client version
	 * @return The converted packet
	 * @throws IllegalAccessException 
	 */
	public PacketPlayOutScoreboardObjective readObjective(Object packet, ProtocolVersion clientVersion) throws IllegalAccessException;
	
	/**
	 * Converts platform-specific instance of display objective packet into custom class object
	 * @param packet - platform-specific display objective packet
	 * @param clientVersion - client version
	 * @return The converted packet
	 * @throws IllegalAccessException 
	 */
	public PacketPlayOutScoreboardDisplayObjective readDisplayObjective(Object packet, ProtocolVersion clientVersion) throws IllegalAccessException;
}