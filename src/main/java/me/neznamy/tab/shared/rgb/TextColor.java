package me.neznamy.tab.shared.rgb;

import me.neznamy.tab.shared.packets.EnumChatFormat;

/**
 * A class used to represent any combination of RGB colors
 */
public class TextColor {

	//red value
	private int red;
	
	//green value
	private int green;
	
	//blue value
	private int blue;
	
	//closest legacy color
	private EnumChatFormat legacyColor;

	
	/**
	 * Creates a new instance based on hex code as string
	 * @param hexCode - a 6-digit combination of hex numbers
	 */
	public TextColor(String hexCode) {
		int hexColor = Integer.parseInt(hexCode, 16);
		red = ((hexColor >> 16) & 0xFF);
		green = ((hexColor >> 8) & 0xFF);
		blue = (hexColor & 0xFF);
		loadClosestColor();
	}
	
	/**
	 * Creates a new instance with given parameter
	 * @param legacyColor - legacy color
	 */
	public TextColor(EnumChatFormat legacyColor) {
		red = legacyColor.getRed();
		green = legacyColor.getGreen();
		blue = legacyColor.getBlue();
		this.legacyColor = legacyColor;
	}
	
	/**
	 * Creates a new instance with given parameters
	 * @param red - red value
	 * @param green - green value
	 * @param blue - blue value
	 */
	public TextColor(int red, int green, int blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		loadClosestColor();
	}
	
	/**
	 * Creates a new instance with given parameters
	 * @param red - red value
	 * @param green - green value
	 * @param blue - blue value
	 * @param legacyColor
	 */
	public TextColor(int red, int green, int blue, EnumChatFormat legacyColor) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.legacyColor = legacyColor;
	}
	
	/**
	 * Loads closest legacy color based on current rgb values
	 */
	private void loadClosestColor() {
		double minDist = 9999;
		double dist;
		legacyColor = EnumChatFormat.WHITE;
		for (EnumChatFormat color : EnumChatFormat.values()) {
			int rDiff = (int) Math.pow(color.getRed() - red, 2);
			int gDiff = (int) Math.pow(color.getGreen() - green, 2);
			int bDiff = (int) Math.pow(color.getBlue() - blue, 2);
			dist = Math.sqrt(rDiff + gDiff + bDiff);
			if (dist < minDist) {
				minDist = dist;
				legacyColor = color;
			}
		}
	}
	
	/**
	 * Returns amount of red
	 * @return amount of red
	 */
	public int getRed() {
		return red;
	}
	
	/**
	 * Returns amount of green
	 * @return amount of green
	 */
	public int getGreen() {
		return green;
	}
	
	/**
	 * Returns amount of blue
	 * @return amount of blue
	 */
	public int getBlue() {
		return blue;
	}
	
	/**
	 * Returns defined legacy color
	 * @return defined legacy color
	 */
	public EnumChatFormat getLegacyColor() {
		return legacyColor;
	}
	
	/**
	 * Converts the color into a valid color value used in color field in chat component
	 * @param rgbClient - if client accepts RGB or not
	 * @return the color converted into string acceptable by client
	 */
	public String toString(boolean rgbClient) {
		if (rgbClient) {
			EnumChatFormat legacyEquivalent = EnumChatFormat.fromRGBExact(red, green, blue);
			if (legacyEquivalent != null) {
				//not sending old colors as RGB to 1.16 clients if not needed as <1.16 servers will fail to apply color
				return legacyEquivalent.toString().toLowerCase();
			}
			return "#" + toHexString();
		} else {
			return legacyColor.toString().toLowerCase();
		}
	}
	

	/**
	 * Returns a 6-digit HEX output from given colors
	 * @param red - red
	 * @param green - green
	 * @param blue - blue
	 * @return the hex string
	 */
	public String toHexString() {
		String s = Integer.toHexString((red << 16) + (green << 8) + blue);
		while (s.length() < 6) s = "0" + s;
		return s;
	}
	
	/**
	 * Reads the string and turns into text color. String is either #RRGGBB or a lowercased legacy color
	 * @param string - string from color field in chat component
	 * @return An instance from specified string
	 */
	public static TextColor fromString(String string) {
		if (string == null) return null;
		if (string.startsWith("#")) {
			return new TextColor(string.substring(1));
		} else {
			return new TextColor(EnumChatFormat.valueOf(string.toUpperCase()));
		}
	}
}