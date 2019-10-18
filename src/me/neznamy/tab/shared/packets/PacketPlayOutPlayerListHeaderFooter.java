package me.neznamy.tab.shared.packets;

import java.lang.reflect.Field;
import java.util.Map;

import com.velocitypowered.proxy.protocol.packet.HeaderAndFooter;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
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
		HEADER.set(packet, MethodAPI.getInstance().ICBC_fromString(Shared.jsonFromText(header)));
		FOOTER.set(packet, MethodAPI.getInstance().ICBC_fromString(Shared.jsonFromText(footer)));
		return packet;
	}
	public Object toBungee(ProtocolVersion clientVersion) {
		return new PlayerListHeaderFooter(Shared.jsonFromText(header), Shared.jsonFromText(footer));
	}
	public Object toVelocity(ProtocolVersion clientVersion) {
		return new HeaderAndFooter(Shared.jsonFromText(header), Shared.jsonFromText(footer));
	}

	private static final Field HEADER;
	private static final Field FOOTER;

	static {
		Map<String, Field> fields = getFields(MethodAPI.PacketPlayOutPlayerListHeaderFooter);
		if (ProtocolVersion.SERVER_VERSION.getNetworkId() >= ProtocolVersion.v1_13_1.getNetworkId()) {
			HEADER = fields.get("header");
			FOOTER = fields.get("footer");
		} else {
			HEADER = fields.get("a");
			FOOTER = fields.get("b");
		}
	}
}