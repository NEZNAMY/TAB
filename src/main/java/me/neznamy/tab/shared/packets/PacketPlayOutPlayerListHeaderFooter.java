package me.neznamy.tab.shared.packets;

import me.neznamy.tab.shared.ProtocolVersion;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutPlayerListHeaderFooter extends UniversalPacketPlayOut {

	public IChatBaseComponent header;
	public IChatBaseComponent footer;

	public PacketPlayOutPlayerListHeaderFooter(String header, String footer) {
		this.header = IChatBaseComponent.optimizedComponent(header);
		this.footer = IChatBaseComponent.optimizedComponent(footer);
	}
	
	public PacketPlayOutPlayerListHeaderFooter(IChatBaseComponent header, IChatBaseComponent footer) {
		this.header = header;
		this.footer = footer;
	}

	@Override
	protected Object build(ProtocolVersion clientVersion) throws Exception {
		return builder.build(this, clientVersion);
	}
}