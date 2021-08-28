package me.neznamy.tab.api.protocol;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.ParseException;

import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.chat.rgb.RGBUtils;
import me.neznamy.tab.api.util.BiFunctionWithException;

/**
 * An interface represending a platform-specific packet builder
 */
public abstract class PacketBuilder {
	
	protected Map<Class<? extends TabPacket>, BiFunctionWithException<TabPacket, ProtocolVersion, Object>> buildMap = new HashMap<>();
	
	protected PacketBuilder() {
		buildMap.put(PacketPlayOutBoss.class, (packet, version) -> build((PacketPlayOutBoss)packet, version));
		buildMap.put(PacketPlayOutChat.class, (packet, version) -> build((PacketPlayOutChat)packet, version));
		buildMap.put(PacketPlayOutPlayerInfo.class, (packet, version) -> build((PacketPlayOutPlayerInfo)packet, version));
		buildMap.put(PacketPlayOutPlayerListHeaderFooter.class, (packet, version) -> build((PacketPlayOutPlayerListHeaderFooter)packet, version));
		buildMap.put(PacketPlayOutScoreboardDisplayObjective.class, (packet, version) -> build((PacketPlayOutScoreboardDisplayObjective)packet, version));
		buildMap.put(PacketPlayOutScoreboardObjective.class, (packet, version) -> build((PacketPlayOutScoreboardObjective)packet, version));
		buildMap.put(PacketPlayOutScoreboardScore.class, (packet, version) -> build((PacketPlayOutScoreboardScore)packet, version));
		buildMap.put(PacketPlayOutScoreboardTeam.class, (packet, version) -> build((PacketPlayOutScoreboardTeam)packet, version));
	}
	
	public Object build(TabPacket packet, ProtocolVersion clientVersion) throws Exception {
		return buildMap.get(packet.getClass()).apply(packet, clientVersion);
	}

	/**
	 * Constructs platform-specific PacketPlayOutBoss class based on custom packet class
	 * @param packet - custom packet to be built
	 * @param clientVersion - version of client to receive the packet
	 * @return platform-specific packet
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public abstract Object build(PacketPlayOutBoss packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException;
	
	/**
	 * Constructs platform-specific PacketPlayOutChat class based on custom packet class
	 * @param packet - custom packet to be built
	 * @param clientVersion - version of client to receive the packet
	 * @return platform-specific packet
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public abstract Object build(PacketPlayOutChat packet, ProtocolVersion clientVersion) throws IllegalAccessException, InvocationTargetException, InstantiationException;
	
	/**
	 * Constructs platform-specific PacketPlayOutPlayerInfo class based on custom packet class
	 * @param packet - custom packet to be built
	 * @param clientVersion - version of client to receive the packet
	 * @return platform-specific packet
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public abstract Object build(PacketPlayOutPlayerInfo packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException;
	
	/**
	 * Constructs platform-specific PacketPlayOutPlayerListHeaderFooter class based on custom packet class
	 * @param packet - custom packet to be built
	 * @param clientVersion - version of client to receive the packet
	 * @return platform-specific packet
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public abstract Object build(PacketPlayOutPlayerListHeaderFooter packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException;
	
	/**
	 * Constructs platform-specific PacketPlayOutScoreboardDisplayObjective class based on custom packet class
	 * @param packet - custom packet to be built
	 * @param clientVersion - version of client to receive the packet
	 * @return platform-specific packet
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public abstract Object build(PacketPlayOutScoreboardDisplayObjective packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException;
	
	/**
	 * Constructs platform-specific PacketPlayOutScoreboardObjective class based on custom packet class
	 * @param packet - custom packet to be built
	 * @param clientVersion - version of client to receive the packet
	 * @return platform-specific packet
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public abstract Object build(PacketPlayOutScoreboardObjective packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException;
	
	/**
	 * Constructs platform-specific PacketPlayOutScoreboardScore class based on custom packet class
	 * @param packet - custom packet to be built
	 * @param clientVersion - version of client to receive the packet
	 * @return platform-specific packet
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public abstract Object build(PacketPlayOutScoreboardScore packet, ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException;
	
	/**
	 * Constructs platform-specific PacketPlayOutScoreboardTeam class based on custom packet class
	 * @param packet - custom packet to be built
	 * @param clientVersion - version of client to receive the packet
	 * @return platform-specific packet
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public abstract Object build(PacketPlayOutScoreboardTeam packet, ProtocolVersion clientVersion) throws IllegalAccessException, InvocationTargetException, InstantiationException;

	/**
	 * Cuts given string to specified character length (or length-1 if last character is a color character) and translates RGB to legacy colors
	 * @param string - string to cut
	 * @param length - length to cut to
	 * @return the cut text or original if cut was not needed
	 */
	public String cutTo(String string, int length) {
		if (string == null) return "";
		String legacyText = string;
		if (string.contains("#")) {
			//converting RGB to legacy colors
			legacyText = RGBUtils.getInstance().convertRGBtoLegacy(string);
		}
		if (legacyText.length() <= length) return legacyText;
		if (legacyText.charAt(length-1) == EnumChatFormat.COLOR_CHAR) {
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
	public String jsonOrCut(String text, ProtocolVersion clientVersion, int length) {
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
	 * @throws ParseException 
	 */
	public abstract PacketPlayOutPlayerInfo readPlayerInfo(Object packet, ProtocolVersion clientVersion) throws IllegalAccessException, InvocationTargetException, ParseException;
	
	/**
	 * Converts platform-specific instance of objective packet into custom class object
	 * @param packet - platform-specific objective packet
	 * @param clientVersion - client version
	 * @return The converted packet
	 * @throws IllegalAccessException 
	 * @throws ParseException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 */
	public abstract PacketPlayOutScoreboardObjective readObjective(Object packet, ProtocolVersion clientVersion) throws IllegalAccessException, ParseException, IllegalArgumentException, InvocationTargetException;
	
	/**
	 * Converts platform-specific instance of display objective packet into custom class object
	 * @param packet - platform-specific display objective packet
	 * @param clientVersion - client version
	 * @return The converted packet
	 * @throws IllegalAccessException 
	 */
	public abstract PacketPlayOutScoreboardDisplayObjective readDisplayObjective(Object packet, ProtocolVersion clientVersion) throws IllegalAccessException;
}