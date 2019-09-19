package me.neznamy.tab.shared.packets;

import java.lang.reflect.Field;
import java.util.Map;

import com.velocitypowered.proxy.protocol.packet.HeaderAndFooter;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import net.kyori.text.Component;
import net.kyori.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;

public class PacketPlayOutPlayerListHeaderFooter extends UniversalPacketPlayOut{

	private String header;
	private String footer;

	public PacketPlayOutPlayerListHeaderFooter(String header, String footer) {
		this.header = header;
		this.footer = footer;
	}
	public Object toNMS(ProtocolVersion clientVersion) throws Exception {
		Object packet = MethodAPI.getInstance().newPacketPlayOutPlayerListHeaderFooter();
		HEADER.set(packet, Shared.mainClass.createComponent(header));
		FOOTER.set(packet, Shared.mainClass.createComponent(footer));
		return packet;
	}
	public Object toBungee(ProtocolVersion clientVersion) {
		return new PlayerListHeaderFooter((String) Shared.mainClass.createComponent(header), (String) Shared.mainClass.createComponent(footer));
	}
	public Object toVelocity(ProtocolVersion clientVersion) {
		String header = (this.header == null || this.header.length() == 0) ? "{\"translate\":\"\"}" : GsonComponentSerializer.INSTANCE.serialize((Component) Shared.mainClass.createComponent(this.header));
		String footer = (this.footer == null || this.footer.length() == 0) ? "{\"translate\":\"\"}" : GsonComponentSerializer.INSTANCE.serialize((Component) Shared.mainClass.createComponent(this.footer));
		return new HeaderAndFooter(header, footer);
	}

	private static Field HEADER;
	private static Field FOOTER;

	static {
		Map<String, Field> fields = getFields(MethodAPI.PacketPlayOutPlayerListHeaderFooter);
		if (ProtocolVersion.SERVER_VERSION.getProtocolNumber() >= ProtocolVersion.v1_13_1.getProtocolNumber()) {
			HEADER = fields.get("header");
			FOOTER = fields.get("footer");
		} else {
			HEADER = fields.get("a");
			FOOTER = fields.get("b");
		}
	}
}