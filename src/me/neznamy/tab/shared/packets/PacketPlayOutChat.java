package me.neznamy.tab.shared.packets;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;
import net.md_5.bungee.protocol.packet.Chat;

public class PacketPlayOutChat extends UniversalPacketPlayOut{

	private IChatBaseComponent message;
	private ChatMessageType type;
	
	public PacketPlayOutChat(String message) {
		this.message = IChatBaseComponent.fromColoredText(message);
		this.type = ChatMessageType.CHAT;
	}
	public PacketPlayOutChat(String message, ChatMessageType type) {
		this.message = IChatBaseComponent.fromColoredText(message);
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
	public Object toNMS(ProtocolVersion clientVersion) {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 12) {
			return MethodAPI.getInstance().newPacketPlayOutChat(MethodAPI.getInstance().ICBC_fromString(message.toString(clientVersion)), type.toNMS());
		} else if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 7) {
			return MethodAPI.getInstance().newPacketPlayOutChat(MethodAPI.getInstance().ICBC_fromString(message.toString(clientVersion)), type.getId());
		} else {
			return MethodAPI.getInstance().newPacketPlayOutChat(message.toString(clientVersion), type.getId());
		}
	}
	public Object toBungee(ProtocolVersion clientVersion) {
		return new Chat(message.toString(clientVersion), type.getId());
	}
	public Object toVelocity(ProtocolVersion clientVersion) {
		return new com.velocitypowered.proxy.protocol.packet.Chat(message.toString(clientVersion), type.getId());
	}
	
	public enum ChatMessageType{

		CHAT((byte)0),
		SYSTEM((byte)1),
		GAME_INFO((byte)2);

		private byte id;
		private Object nmsEquivalent;

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private ChatMessageType(byte id) {
			this.id = id;
			if (MethodAPI.ChatMessageType != null) {
				nmsEquivalent = Enum.valueOf((Class<Enum>)MethodAPI.ChatMessageType, toString());
			}
		}
		public byte getId() {
			return id;
		}
		public Object toNMS() {
			return nmsEquivalent;
		}
	}
}