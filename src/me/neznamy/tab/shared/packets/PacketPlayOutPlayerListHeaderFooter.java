package me.neznamy.tab.shared.packets;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;

public class PacketPlayOutPlayerListHeaderFooter extends UniversalPacketPlayOut{

	private String header;
	private String footer;
	
	public PacketPlayOutPlayerListHeaderFooter(String header, String footer) {
		this.header = header;
		this.footer = footer;
	}
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		Object packet = newPacketPlayOutPlayerListHeaderFooter.newInstance();
		PacketPlayOutPlayerListHeaderFooter_HEADER.set(packet, Shared.mainClass.createComponent(header));
		PacketPlayOutPlayerListHeaderFooter_FOOTER.set(packet, Shared.mainClass.createComponent(footer));
		return packet;
	}
	public Object toBungee(ProtocolVersion clientVersion) {
		return new PlayerListHeaderFooter((String) Shared.mainClass.createComponent(header), (String) Shared.mainClass.createComponent(footer));
	}
	
	private static Class<?> PacketPlayOutPlayerListHeaderFooter;
	private static Constructor<?> newPacketPlayOutPlayerListHeaderFooter;
	private static Field PacketPlayOutPlayerListHeaderFooter_HEADER;
	private static Field PacketPlayOutPlayerListHeaderFooter_FOOTER;
	
	static {
		try {
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 8) {
				PacketPlayOutPlayerListHeaderFooter = getNMSClass("PacketPlayOutPlayerListHeaderFooter");
				newPacketPlayOutPlayerListHeaderFooter = PacketPlayOutPlayerListHeaderFooter.getConstructor();
				try {
					// 1_13_R1-
					(PacketPlayOutPlayerListHeaderFooter_HEADER = PacketPlayOutPlayerListHeaderFooter.getDeclaredField("a")).setAccessible(true);
					(PacketPlayOutPlayerListHeaderFooter_FOOTER = PacketPlayOutPlayerListHeaderFooter.getDeclaredField("b")).setAccessible(true);
				} catch (Exception e) {
					// 1_13_R2+
					(PacketPlayOutPlayerListHeaderFooter_HEADER = PacketPlayOutPlayerListHeaderFooter.getDeclaredField("header")).setAccessible(true);
					(PacketPlayOutPlayerListHeaderFooter_FOOTER = PacketPlayOutPlayerListHeaderFooter.getDeclaredField("footer")).setAccessible(true);
				}
			}
		} catch (Exception e) {
			Shared.error("Failed to initialize PacketPlayOutPlayerListHeaderFooter", e);
		}
	}
}