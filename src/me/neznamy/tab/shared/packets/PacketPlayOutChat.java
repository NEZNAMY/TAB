package me.neznamy.tab.shared.packets;

import java.lang.reflect.Constructor;
import java.util.UUID;

import me.neznamy.tab.platforms.bukkit.packets.NMSHook;
import me.neznamy.tab.shared.ProtocolVersion;
import net.md_5.bungee.protocol.packet.Chat;

public class PacketPlayOutChat extends UniversalPacketPlayOut{

	private static Class<?> PacketPlayOutChat = getNMSClass("PacketPlayOutChat", "Packet3Chat");
	private static Class<?> ChatMessageType_ = getNMSClass("ChatMessageType");
	private static Constructor<?> newPacketPlayOutChat = getConstructor(PacketPlayOutChat, 3, 2, 1);
	
	private IChatBaseComponent message;
	private ChatMessageType type;
	
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
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 16) {
			return newPacketPlayOutChat.newInstance(NMSHook.stringToComponent(message.toString(clientVersion)), type.toNMS(), UUID.randomUUID());
		} else if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 12) {
			return newPacketPlayOutChat.newInstance(NMSHook.stringToComponent(message.toString(clientVersion)), type.toNMS());
		} else if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			return newPacketPlayOutChat.newInstance(NMSHook.stringToComponent(message.toString(clientVersion)), type.getId());
		} else if (ProtocolVersion.SERVER_VERSION.getMinorVersion() == 7) {
			try {
				//v1_7_R4
				return newPacketPlayOutChat.newInstance(NMSHook.stringToComponent(message.toString(clientVersion)), (int)type.getId(), true);
			} catch (Exception e) {
				return newPacketPlayOutChat.newInstance(NMSHook.stringToComponent(message.toString(clientVersion)), true);
			}
		} else {
			return newPacketPlayOutChat.newInstance(message.toString(clientVersion), type.getId());
		}
	}
	public Object toBungee(ProtocolVersion clientVersion) {
		Chat chat = new Chat(message.toString(clientVersion), type.getId());
		try {
			chat.setSender(UUID.randomUUID());
		} catch (NoSuchMethodError e) {
			//old bungeecord version
		}
		return chat;
	}
	public Object toVelocity(ProtocolVersion clientVersion) {
		return new com.velocitypowered.proxy.protocol.packet.Chat(message.toString(clientVersion), type.getId(), UUID.randomUUID());
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
			if (ChatMessageType_ != null) {
				nmsEquivalent = Enum.valueOf((Class<Enum>)ChatMessageType_, toString());
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