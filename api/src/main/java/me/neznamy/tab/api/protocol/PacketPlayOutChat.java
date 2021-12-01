package me.neznamy.tab.api.protocol;

import me.neznamy.tab.api.chat.IChatBaseComponent;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutChat implements TabPacket {

	/** Message to be sent */
	private final IChatBaseComponent message;

	/** Message position */
	private final ChatMessageType type;

	/**
	 * Constructs new instance with given parameters
	 * 
	 * @param	message
	 * 			Chat message to be sent
	 * @param	type
	 * 			Message position
	 */
	public PacketPlayOutChat(IChatBaseComponent message, ChatMessageType type) {
		this.message = message;
		this.type = type;
	}

	@Override
	public String toString() {
		return String.format("PacketPlayOutChat{message=%s,type=%s}", message, type);
	}

	/**
	 * Returns {@link #message}
	 * @return	message
	 */
	public IChatBaseComponent getMessage() {
		return message;
	}

	/**
	 * Returns {@link #type}
	 * @return	type
	 */
	public ChatMessageType getType() {
		return type;
	}

	/**
	 * An enum representing positions of a chat message
	 * Calling ordinal() will return type's network ID.
	 */
	public enum ChatMessageType {

		CHAT,
		SYSTEM,
		GAME_INFO
	}
}