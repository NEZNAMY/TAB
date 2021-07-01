package me.neznamy.tab.shared.packets;

import java.lang.reflect.InvocationTargetException;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutPlayerListHeaderFooter extends UniversalPacketPlayOut {

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
	protected Object build(ProtocolVersion clientVersion) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		return TAB.getInstance().getPacketBuilder().build(this, clientVersion);
	}
	
	/**
	 * An override to toString() method for better output
	 */
	@Override
	public String toString() {
		return String.format("PacketPlayOutPlayerListHeaderFooter{header=%s,footer=%s}", getHeader(), getFooter());
	}

	public IChatBaseComponent getFooter() {
		return footer;
	}

	public IChatBaseComponent getHeader() {
		return header;
	}
}