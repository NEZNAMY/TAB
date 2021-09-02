package me.neznamy.tab.api.protocol;

import me.neznamy.tab.api.chat.IChatBaseComponent;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutPlayerListHeaderFooter implements TabPacket {

	/** Tablist header */
	private IChatBaseComponent header;

	/** Tablist footer */
	private IChatBaseComponent footer;

	/**
	 * Constructs new instance with given parameters. They are converted to {@link IChatBaseComponent}
	 * using {@link IChatBaseComponent#optimizedComponent(String)} method.
	 * 
	 * @param	header
	 * 			Tablist header
	 * @param	footer
	 * 			Tablist footer
	 */
	public PacketPlayOutPlayerListHeaderFooter(String header, String footer) {
		this.header = IChatBaseComponent.optimizedComponent(header);
		this.footer = IChatBaseComponent.optimizedComponent(footer);
	}

	/**
	 * Constructs new instance with given parameters.
	 * 
	 * @param	header
	 * 			Tablist header
	 * @param	footer
	 * 			Tablist footer
	 */
	public PacketPlayOutPlayerListHeaderFooter(IChatBaseComponent header, IChatBaseComponent footer) {
		this.header = header;
		this.footer = footer;
	}

	@Override
	public String toString() {
		return String.format("PacketPlayOutPlayerListHeaderFooter{header=%s,footer=%s}", header, footer);
	}

	/**
	 * Returns {@link #header}
	 * @return	header
	 */
	public IChatBaseComponent getHeader() {
		return header;
	}

	/**
	 * Returns {@link #footer}
	 * @return	footer
	 */
	public IChatBaseComponent getFooter() {
		return footer;
	}
}