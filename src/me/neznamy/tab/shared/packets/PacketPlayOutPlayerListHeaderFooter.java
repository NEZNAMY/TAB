package me.neznamy.tab.shared.packets;

import java.lang.reflect.Field;
import java.util.List;

import com.velocitypowered.proxy.protocol.packet.HeaderAndFooter;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;

public class PacketPlayOutPlayerListHeaderFooter extends UniversalPacketPlayOut{

	public IChatBaseComponent header;
	public IChatBaseComponent footer;

	public PacketPlayOutPlayerListHeaderFooter(String header, String footer) {
		this.header = new IChatBaseComponent(header);
		this.footer = new IChatBaseComponent(footer);
	}
	public PacketPlayOutPlayerListHeaderFooter(IChatBaseComponent header, IChatBaseComponent footer) {
		this.header = header;
		this.footer = footer;
	}
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		Object packet = MethodAPI.getInstance().newPacketPlayOutPlayerListHeaderFooter();
		HEADER.set(packet, MethodAPI.getInstance().ICBC_fromString(header.toString()));
		FOOTER.set(packet, MethodAPI.getInstance().ICBC_fromString(footer.toString()));
		return packet;
	}
	public Object toBungee(ProtocolVersion clientVersion) {
		return new PlayerListHeaderFooter(header.toString(), footer.toString());
	}
	public Object toVelocity(ProtocolVersion clientVersion) {
		return new HeaderAndFooter(header.toString(), footer.toString());
	}

	private static List<Field> fields = getFields(MethodAPI.PacketPlayOutPlayerListHeaderFooter, MethodAPI.IChatBaseComponent);
	private static final Field HEADER = getObjectAt(fields, 0);
	private static final Field FOOTER = getObjectAt(fields, 1);
}