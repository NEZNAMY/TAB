package me.neznamy.tab.shared.packets;

import me.neznamy.tab.shared.ProtocolVersion;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutChat extends UniversalPacketPlayOut {

	public IChatBaseComponent message;
	public ChatMessageType type;

	public PacketPlayOutChat(String message) {
		this.message = IChatBaseComponent.optimizedComponent(message);
		this.type = ChatMessageType.CHAT;
	}

	public PacketPlayOutChat(String message, ChatMessageType type) {
		this.message = IChatBaseComponent.optimizedComponent(message);
		this.type = type;
	}

	public PacketPlayOutChat(IChatBaseComponent message) {
		this.message = message;
		this.type = ChatMessageType.CHAT;
	}

	public PacketPlayOutChat(IChatBaseComponent message, ChatMessageType type) {
		this.message = message;
		this.type = type;
	}

	@Override
	protected Object build(ProtocolVersion clientVersion) throws Exception {
		return builder.build(this, clientVersion);
	}

	public enum ChatMessageType {

		CHAT,
		SYSTEM,
		GAME_INFO;
	}
}