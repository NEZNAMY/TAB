package me.neznamy.tab.shared.packets;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * Abstract class to be extended by packets which can be sent on all 3 supported platforms
 */
public abstract class UniversalPacketPlayOut {

	public static PacketBuilder builder;
	
	/**
	 * Converts the class into raw packet
	 * @param clientVersion - version of player to create packet for
	 * @return the raw packet
	 */
	protected abstract Object build(ProtocolVersion clientVersion) throws Exception;
	
	/**
	 * Cuts the string into specified length if needed
	 * @param string - text to cut
	 * @param length - length to cut to
	 * @return the cut string
	 */
	public String cutTo(String string, int length) {
		if (string == null || string.length() <= length) return string;
		if (string.charAt(length-1) == Placeholders.colorChar) {
			return string.substring(0, length-1); //cutting one extra character to prevent prefix ending with "&"
		} else {
			return string.substring(0, length);
		}
	}
	
	/**
	 * Calls build(...) and wraps it into a try/catch
	 * @param clientVersion - player version
	 * @return built packet depending on platform
	 */
	public Object create(ProtocolVersion clientVersion) {
		try {
			return build(clientVersion);
		} catch (Exception e) {
			return Shared.errorManager.printError(null, "An error occurred when creating " + getClass().getSimpleName(), e);
		}
	}
}