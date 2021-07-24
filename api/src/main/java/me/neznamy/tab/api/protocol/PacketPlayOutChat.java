package me.neznamy.tab.api.protocol;

import java.lang.reflect.InvocationTargetException;

import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.chat.IChatBaseComponent;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutChat implements CrossPlatformPacket {

	//the message to be sent
	private IChatBaseComponent message;
	
	//position of the message
	private ChatMessageType type;

	/**
	 * Constructs new instance of the class with given message and position
	 * @param message - message to be sent
	 * @param type - message position
	 */
	public PacketPlayOutChat(IChatBaseComponent message, ChatMessageType type) {
		this.message = message;
		this.type = type;
	}

	/**
	 * Calls build method of packet builder instance and returns output
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InstantiationException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	@Override
	public Object build(ProtocolVersion clientVersion) throws IllegalAccessException, InvocationTargetException, InstantiationException {
		return TabAPI.getInstance().getPlatform().getPacketBuilder().build(this, clientVersion);
	}
	
	/**
	 * An override to toString() method for better output
	 */
	@Override
	public String toString() {
		return String.format("PacketPlayOutChat{message=%s,type=%s}", message, type);
	}

	public IChatBaseComponent getMessage() {
		return message;
	}

	public ChatMessageType getType() {
		return type;
	}

	/**
	 * An enum representing positions of a chat message
	 */
	public enum ChatMessageType {

		CHAT,
		SYSTEM,
		GAME_INFO;
	}
}