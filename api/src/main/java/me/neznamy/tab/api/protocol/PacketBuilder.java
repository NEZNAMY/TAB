package me.neznamy.tab.api.protocol;

import java.util.HashMap;
import java.util.Map;

import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.chat.EnumChatFormat;
import me.neznamy.tab.api.chat.IChatBaseComponent;
import me.neznamy.tab.api.chat.rgb.RGBUtils;
import me.neznamy.tab.api.util.BiFunctionWithException;

/**
 * An interface representing a platform-specific packet builder
 */
public abstract class PacketBuilder {

	/** Function map turning custom packet class into platform-specific packets */
	protected final Map<Class<? extends TabPacket>, BiFunctionWithException<TabPacket, ProtocolVersion, Object>> buildMap = new HashMap<>();

	/**
	 * Constructs new instance and fills {@link #buildMap} with abstract build methods
	 * of packets present on all platforms.
	 */
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

	/**
	 * Converts custom packet into platform-specific packet by calling a function from
	 * {@link #buildMap}.
	 * 
	 * @param	packet
	 * 			Packet to build
	 * @param	clientVersion
	 * 			Protocol version of player to build the packet for
	 * @return	Platform-specific packet
	 * @throws	ReflectiveOperationException
	 * 			if reflection fails
	 */
	public Object build(TabPacket packet, ProtocolVersion clientVersion) throws ReflectiveOperationException {
		return buildMap.get(packet.getClass()).apply(packet, clientVersion);
	}

	/**
	 * Constructs platform-specific PacketPlayOutBoss class based on custom packet class
	 * 
	 * @param	packet
	 * 			Custom packet to be built
	 * @param	clientVersion
	 * 			Protocol version of player to build the packet for
	 * @return	Platform-specific packet
	 * @throws	ReflectiveOperationException 
	 * 			if thrown by reflective operation
	 */
	public abstract Object build(PacketPlayOutBoss packet, ProtocolVersion clientVersion) throws ReflectiveOperationException;

	/**
	 * Constructs platform-specific PacketPlayOutChat class based on custom packet class
	 * 
	 * @param	packet
	 * 			Custom packet to be built
	 * @param	clientVersion
	 * 			Protocol version of player to build the packet for
	 * @return	Platform-specific packet
	 * @throws	ReflectiveOperationException 
	 * 			if thrown by reflective operation
	 */
	public abstract Object build(PacketPlayOutChat packet, ProtocolVersion clientVersion) throws ReflectiveOperationException;

	/**
	 * Constructs platform-specific PacketPlayOutPlayerInfo class based on custom packet class
	 * 
	 * @param	packet
	 * 			Custom packet to be built
	 * @param	clientVersion
	 * 			Protocol version of player to build the packet for
	 * @return	Platform-specific packet
	 * @throws	ReflectiveOperationException 
	 * 			if thrown by reflective operation
	 */
	public abstract Object build(PacketPlayOutPlayerInfo packet, ProtocolVersion clientVersion) throws ReflectiveOperationException;

	/**
	 * Constructs platform-specific PacketPlayOutPlayerListHeaderFooter class based on custom packet class
	 * 
	 * @param	packet
	 * 			Custom packet to be built
	 * @param	clientVersion
	 * 			Protocol version of player to build the packet for
	 * @return	Platform-specific packet
	 * @throws	ReflectiveOperationException 
	 * 			if thrown by reflective operation
	 */
	public abstract Object build(PacketPlayOutPlayerListHeaderFooter packet, ProtocolVersion clientVersion) throws ReflectiveOperationException;

	/**
	 * Constructs platform-specific PacketPlayOutScoreboardDisplayObjective class based on custom packet class
	 * 
	 * @param	packet
	 * 			Custom packet to be built
	 * @param	clientVersion
	 * 			Protocol version of player to build the packet for
	 * @return	Platform-specific packet
	 * @throws	ReflectiveOperationException 
	 * 			if thrown by reflective operation
	 */
	public abstract Object build(PacketPlayOutScoreboardDisplayObjective packet, ProtocolVersion clientVersion) throws ReflectiveOperationException;

	/**
	 * Constructs platform-specific PacketPlayOutScoreboardObjective class based on custom packet class
	 * 
	 * @param	packet
	 * 			Custom packet to be built
	 * @param	clientVersion
	 * 			Protocol version of player to build the packet for
	 * @return	Platform-specific packet
	 * @throws	ReflectiveOperationException 
	 * 			if thrown by reflective operation
	 */
	public abstract Object build(PacketPlayOutScoreboardObjective packet, ProtocolVersion clientVersion) throws ReflectiveOperationException;

	/**
	 * Constructs platform-specific PacketPlayOutScoreboardScore class based on custom packet class
	 * 
	 * @param	packet
	 * 			Custom packet to be built
	 * @param	clientVersion
	 * 			Protocol version of player to build the packet for
	 * @return	Platform-specific packet
	 * @throws	ReflectiveOperationException 
	 * 			if thrown by reflective operation
	 */
	public abstract Object build(PacketPlayOutScoreboardScore packet, ProtocolVersion clientVersion) throws ReflectiveOperationException;

	/**
	 * Constructs platform-specific PacketPlayOutScoreboardTeam class based on custom packet class
	 * 
	 * @param	packet
	 * 			Custom packet to be built
	 * @param	clientVersion
	 * 			Protocol version of player to build the packet for
	 * @return	Platform-specific packet
	 * @throws	ReflectiveOperationException 
	 * 			if thrown by reflective operation
	 */
	public abstract Object build(PacketPlayOutScoreboardTeam packet, ProtocolVersion clientVersion) throws ReflectiveOperationException;

	/**
	 * Cuts given string to specified character length (or length-1 if last character is a color character)
	 * and translates RGB to legacy colors. If string is not that long, the original string is returned.
	 * RGB codes are converted into legacy, since cutting is only needed for <1.13.
	 * If {@code string} is {@code null}, empty string is returned.
	 * @param	string
	 * 			String to cut
	 * @param	length
	 * 			Length to cut to
	 * @return	string cut to {@code length} characters
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
	 * If {@code clientVersion} is >= 1.13, creates a component from given text and returns
	 * it as a serialized component, which BungeeCord uses.
	 * <p>
	 * If {@code clientVersion} is < 1.12, the text is cut to {@code length} characters if
	 * needed and returned.
	 * 
	 * @param	text
	 * 			Text to convert
	 * @param	clientVersion
	 * 			Version of player to convert text for
	 * @return	serialized component for 1.13+ clients, cut string for 1.12-
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
	 * Converts platform-specific instance of player info packet into 
	 * {@link PacketPlayOutPlayerInfo} object.
	 * 
	 * @param	packet
	 * 			platform-specific info packet
	 * @param	clientVersion
	 * 			Version of client receiving the packet
	 * @return	The packet converted into {@link PacketPlayOutPlayerInfo}
	 * @throws	ReflectiveOperationException 
	 * 			if thrown by reflective operation
	 */
	public abstract PacketPlayOutPlayerInfo readPlayerInfo(Object packet, ProtocolVersion clientVersion) throws ReflectiveOperationException;

	/**
	 * Converts platform-specific instance of objective packet into 
	 * {@link PacketPlayOutScoreboardObjective} object.
	 * 
	 * @param	packet
	 * 			platform-specific objective packet
	 * @return	The packet converted into {@link PacketPlayOutScoreboardObjective}
	 * @throws	ReflectiveOperationException 
	 * 			if thrown by reflective operation
	 */
	public abstract PacketPlayOutScoreboardObjective readObjective(Object packet) throws ReflectiveOperationException;

	/**
	 * Converts platform-specific instance of display objective packet into 
	 * {@link PacketPlayOutScoreboardDisplayObjective} object.
	 * 
	 * @param	packet
	 * 			platform-specific display objective packet
	 * @return	The packet converted into {@link PacketPlayOutScoreboardDisplayObjective}
	 * @throws	ReflectiveOperationException 
	 * 			if thrown by reflective operation
	 */
	public abstract PacketPlayOutScoreboardDisplayObjective readDisplayObjective(Object packet) throws ReflectiveOperationException;
}