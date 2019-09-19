package me.neznamy.tab.shared.packets;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;
import net.md_5.bungee.protocol.packet.Chat;

public class PacketPlayOutChat extends UniversalPacketPlayOut{

	private String json;
	private ChatMessageType type;
	
	public PacketPlayOutChat(String json, ChatMessageType type) {
		this.json = json;
		this.type = type;
	}
	public Object toNMS(ProtocolVersion clientVersion) {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 12) {
			return MethodAPI.getInstance().newPacketPlayOutChat(MethodAPI.getInstance().ICBC_fromString(json), type.toNMS());
		} else if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 7) {
			return MethodAPI.getInstance().newPacketPlayOutChat(MethodAPI.getInstance().ICBC_fromString(json), type.toByte());
		} else {
			return MethodAPI.getInstance().newPacketPlayOutChat(json, type.toByte());
		}
	}
	public Object toBungee(ProtocolVersion clientVersion) {
		return new Chat(json, type.toByte());
	}
	public Object toVelocity(ProtocolVersion clientVersion) {
		return new com.velocitypowered.proxy.protocol.packet.Chat(json, type.toByte());
	}
	
	public enum ChatMessageType{

		CHAT((byte)0), 
		SYSTEM((byte)1), 
		GAME_INFO((byte)2);

		private byte byteEquivalent;
		private Object nmsEquivalent;

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private ChatMessageType(byte byteEquivalent) {
			this.byteEquivalent = byteEquivalent;
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 12 && ProtocolVersion.SERVER_VERSION != ProtocolVersion.BUNGEE) {
				nmsEquivalent = Enum.valueOf((Class<Enum>)MethodAPI.ChatMessageType, toString());
			}
		}
		public byte toByte() {
			return byteEquivalent;
		}
		public Object toNMS() {
			return nmsEquivalent;
		}
	}
}