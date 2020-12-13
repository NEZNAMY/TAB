package me.neznamy.tab.shared.packets;

import me.neznamy.tab.shared.ProtocolVersion;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutPlayerListHeaderFooter extends UniversalPacketPlayOut {

	//header
	public IChatBaseComponent header;
	
	//footer
	public IChatBaseComponent footer;

	/**
	 * Constructs a new instance with given parameters
	 * @param header - header
	 * @param footer - footer
	 */
	public PacketPlayOutPlayerListHeaderFooter(String header, String footer) {
		this.header = IChatBaseComponent.optimizedComponent(header);
		this.footer = IChatBaseComponent.optimizedComponent(footer);
	}
	
	/**
	 * Constructs a new instance with given parameters
	 * @param header - header
	 * @param footer - footer
	 */
	public PacketPlayOutPlayerListHeaderFooter(IChatBaseComponent header, IChatBaseComponent footer) {
		this.header = header;
		this.footer = footer;
	}

	/**
	 * Calls build method of packet builder instance and returns output
	 */
	@Override
	protected Object build(ProtocolVersion clientVersion) throws Exception {
		return builder.build(this, clientVersion);
	}
	
	/**
	 * An override to toString() method for better output
	 */
	@Override
	public String toString() {
		return "PacketPlayOutPlayerListHeaderFooter{header=" + header.toString(ProtocolVersion.SERVER_VERSION) + 
				",footer=" + footer.toString(ProtocolVersion.SERVER_VERSION) + "}";
	}
}