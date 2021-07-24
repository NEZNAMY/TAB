package me.neznamy.tab.api.protocol;

import java.lang.reflect.InvocationTargetException;

import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.chat.IChatBaseComponent;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutPlayerListHeaderFooter implements CrossPlatformPacket {

	//tablist header
	private IChatBaseComponent header;
	
	//tablist footer
	private IChatBaseComponent footer;

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
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	@Override
	public Object build(ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		return TabAPI.getInstance().getPlatform().getPacketBuilder().build(this, clientVersion);
	}

	@Override
	public String toString() {
		return String.format("PacketPlayOutPlayerListHeaderFooter{header=%s,footer=%s}", header, footer);
	}

	public IChatBaseComponent getFooter() {
		return footer;
	}

	public IChatBaseComponent getHeader() {
		return header;
	}
}