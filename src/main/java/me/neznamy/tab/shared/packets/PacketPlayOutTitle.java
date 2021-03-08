package me.neznamy.tab.shared.packets;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;

/**
 * A class representing platform specific packet class
 */
public class PacketPlayOutTitle extends UniversalPacketPlayOut {

	//packet action
	public EnumTitleAction action;
	
	//displayed text
	public String text;
	
	//ticks to spend fading in
	public int fadeIn;
	
	//ticks to stay
	public int stay;
	
	//ticks to spend fading out
	public int fadeOut;

	/**
	 * Constructs new instance with all arguments
	 * @param action - packet action
	 * @param text - displayed text
	 * @param fadeIn - ticks to spend fading in
	 * @param stay - ticks to stay
	 * @param fadeOut - ticks to spend fading out
	 */
	private PacketPlayOutTitle(EnumTitleAction action, String text, int fadeIn, int stay, int fadeOut) {
		this.action = action;
		this.text = text;
		this.fadeIn = fadeIn;
		this.stay = stay;
		this.fadeOut = fadeOut;
	}
	
	/**
	 * Creates new packet with action set to TITLE with given text
	 * @param text - text to display
	 * @return Created packet with with TITLE action and given text
	 */
	public static PacketPlayOutTitle TITLE(String text) {
		return new PacketPlayOutTitle(EnumTitleAction.TITLE, text, 0, 0, 0);
	}
	
	/**
	 * Creates new packet with action set to SUBTITLE with given text.
	 * Note: subtitle only displays after title is sent. If you don't want any title, send a title packet with invisible text such as &r
	 * @param text - text to display
	 * @return Created packet with with0 SUBTITLE action and given text
	 */
	public static PacketPlayOutTitle SUBTITLE(String text) {
		return new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, text, 0, 0, 0);
	}
	
	/**
	 * Creates new packet with action set to ACTIONBAR with given text.
	 * Note: This was added in 1.11
	 * @param text - text to display
	 * @return Created packet with with ACTIONBAR action and given text
	 */
	public static PacketPlayOutTitle ACTIONBAR(String text) {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() < 11) throw new IllegalStateException("Not supported on <1.11");
		return new PacketPlayOutTitle(EnumTitleAction.ACTIONBAR, text, 0, 0, 0);
	}
	
	/**
	 * Creates new packet with action set to TIMES with given parameters
	 * @param fadeIn - ticks to fade in
	 * @param stay - ticks to stay
	 * @param fadeOut - ticks to fade out
	 * @return Created packet with TIMES action and given parameters
	 */
	public static PacketPlayOutTitle TIMES(int fadeIn, int stay, int fadeOut) {
		return new PacketPlayOutTitle(EnumTitleAction.TIMES, null, fadeIn, stay, fadeOut);
	}
	
	/**
	 * Clears current title/subtitle and prevents any new from appearing
	 * @return Created packet with CLEAR action
	 */
	public static PacketPlayOutTitle CLEAR() {
		return new PacketPlayOutTitle(EnumTitleAction.CLEAR, null, 0, 0, 0);
	}
	
	/**
	 * Resets title times to their original values and makes titles appear again if CLEAR was used before
	 * @return Created packet with RESET action
	 */
	public static PacketPlayOutTitle RESET() {
		return new PacketPlayOutTitle(EnumTitleAction.RESET, null, 0, 0, 0);
	}
	
	@Override
	protected Object build(ProtocolVersion clientVersion) throws Exception {
		return TAB.getInstance().getPacketBuilder().build(this, clientVersion);
	}
	
	/**
	 * An override to toString() method for better output
	 */
	@Override
	public String toString() {
		return String.format("PacketPlayOutTitle{action%s,text=%s,fadeIn=%s,stay=%s,fadeOut=%s}", action, text, fadeIn, stay, fadeOut);
	}

	/**
	 * List of possible packet actions
	 */
	public enum EnumTitleAction {
		
		TITLE,
		SUBTITLE,
		ACTIONBAR,
		TIMES,
		CLEAR,
		RESET;
	}
}