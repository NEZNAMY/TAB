package me.neznamy.tab.shared.packets;

import java.lang.reflect.InvocationTargetException;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutChat extends UniversalPacketPlayOut {

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
	protected Object build(ProtocolVersion clientVersion) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, SecurityException {
		return TAB.getInstance().getPacketBuilder().build(this, clientVersion);
	}
	
	/**
	 * An override to toString() method for better output
	 */
	@Override
	public String toString() {
		return String.format("PacketPlayOutChat{message=%s,type=%s}", getMessage(), getType());
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