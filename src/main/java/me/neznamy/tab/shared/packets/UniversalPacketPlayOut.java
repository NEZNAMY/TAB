package me.neznamy.tab.shared.packets;

import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOut;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.placeholders.Placeholders;

/**
 * Abstract class to be extended by packets which can be sent on all 3 supported platforms
 */
public abstract class UniversalPacketPlayOut extends PacketPlayOut {

	/**
	 * Converts the class into an instance of net.md-5.bungee.protocol.DefinedPacket
	 * @param clientVersion - version of player to create packet for
	 * @return the bungee packet
	 */
	public abstract Object toBungee(ProtocolVersion clientVersion);
	
	/**
	 * Converts the class into an instance of com.velocitypowered.proxy.protocol.MinecraftPacket
	 * @param clientVersion - version of player to create packet for
	 * @return the velocity packet
	 */
	public abstract Object toVelocity(ProtocolVersion clientVersion);
	
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
	 * Calls platform-specific build call to build the packet
	 * @param clientVersion - player version
	 * @return built packet depending on platform
	 */
	public Object build(ProtocolVersion clientVersion) {
		try {
			return Shared.platform.buildPacket(this, clientVersion);
		} catch (Exception e) {
			return Shared.errorManager.printError(null, "An error occurred when creating " + getClass().getSimpleName(), e);
		}
	}
}