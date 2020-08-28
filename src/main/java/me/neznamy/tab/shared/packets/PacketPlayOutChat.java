package me.neznamy.tab.shared.packets;

import java.lang.reflect.Constructor;
import java.util.UUID;

import me.neznamy.tab.platforms.bukkit.packets.NMSHook;
import me.neznamy.tab.shared.ProtocolVersion;
import net.md_5.bungee.protocol.packet.Chat;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutChat extends UniversalPacketPlayOut{

	private static Class<?> PacketPlayOutChat;
	private static Class<?> ChatMessageType_;
	private static Constructor<?> newPacketPlayOutChat;
	
	private IChatBaseComponent message;
	private ChatMessageType type;
	
	public static void initializeClass() throws Exception {
		try {
			//1.7+
			PacketPlayOutChat = getNMSClass("PacketPlayOutChat");
		} catch (ClassNotFoundException e) {
			//1.6-
			PacketPlayOutChat = getNMSClass("Packet3Chat");
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 12) {
			ChatMessageType_ = getNMSClass("ChatMessageType");
		}
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 16) {
			newPacketPlayOutChat = PacketPlayOutChat.getConstructor(NMSHook.IChatBaseComponent, ChatMessageType_, UUID.class);
		} else if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 12) {
			newPacketPlayOutChat = PacketPlayOutChat.getConstructor(NMSHook.IChatBaseComponent, ChatMessageType_);
		} else if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
			newPacketPlayOutChat = PacketPlayOutChat.getConstructor(NMSHook.IChatBaseComponent, byte.class);
		} else if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 6) {
			try {
				//v1_7_R4
				newPacketPlayOutChat = PacketPlayOutChat.getConstructor(NMSHook.IChatBaseComponent, int.class);
			} catch (Exception e) {
				newPacketPlayOutChat = PacketPlayOutChat.getConstructor(NMSHook.IChatBaseComponent, boolean.class);
			}
		} else {
			newPacketPlayOutChat = PacketPlayOutChat.getConstructor(String.class, boolean.class);
		}
	}
	
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
		} else if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 6) {
			try {
				//v1_7_R4
				return newPacketPlayOutChat.newInstance(NMSHook.stringToComponent(message.toString(clientVersion)), (int)type.getId());
			} catch (Exception e) {
				return newPacketPlayOutChat.newInstance(NMSHook.stringToComponent(message.toString(clientVersion)), true);
			}
		} else {
			return newPacketPlayOutChat.newInstance(message.toColoredText(), true);
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