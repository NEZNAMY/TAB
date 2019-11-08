package me.neznamy.tab.shared.packets;

import java.lang.reflect.Field;
import java.util.List;

import com.velocitypowered.proxy.protocol.packet.HeaderAndFooter;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;

public class PacketPlayOutPlayerListHeaderFooter extends UniversalPacketPlayOut{

	public String header;
	public String footer;

	public PacketPlayOutPlayerListHeaderFooter(String header, String footer) {
		this.header = header;
		this.footer = footer;
	}
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		Object packet = MethodAPI.getInstance().newPacketPlayOutPlayerListHeaderFooter();
		HEADER.set(packet, MethodAPI.getInstance().ICBC_fromString(new IChatBaseComponent(header).toString()));
		FOOTER.set(packet, MethodAPI.getInstance().ICBC_fromString(new IChatBaseComponent(footer).toString()));
		return packet;
	}
	public Object toBungee(ProtocolVersion clientVersion) {
		return new PlayerListHeaderFooter(new IChatBaseComponent(header).toString(), new IChatBaseComponent(footer).toString());
	}
	public Object toVelocity(ProtocolVersion clientVersion) {
		return new HeaderAndFooter(new IChatBaseComponent(header).toString(), new IChatBaseComponent(footer).toString());
	}

	private static List<Field> fields = getFields(MethodAPI.PacketPlayOutPlayerListHeaderFooter, MethodAPI.IChatBaseComponent);
	private static Field HEADER = getObjectAt(fields, 0);
	private static Field FOOTER = getObjectAt(fields, 1);
}