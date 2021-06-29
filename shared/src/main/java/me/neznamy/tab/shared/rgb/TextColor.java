package me.neznamy.tab.shared.rgb;

import me.neznamy.tab.shared.packets.EnumChatFormat;

/**
 * A class used to represent any combination of RGB colors
 */
public class TextColor {

	private static EnumChatFormat[] legacyColors = EnumChatFormat.values();
	
	//red value
	private Integer red;
	
	//green value
	private Integer green;
	
	//blue value
	private Integer blue;
	
	//closest legacy color
	private EnumChatFormat legacyColor;
	
	//hex code as string prefixed with #
	private String hexCode;
	
	//true if legacy color was forced via constructor, false if automatically
	private boolean legacyColorForced;
	
	//true if toString should return legacy color
	private boolean returnLegacy;
	
	/**
	 * Constructs new instance based on hex code as string
	 * @param hexCode - a 6-digit combination of hex numbers
	 */
	public TextColor(String hexCode) {
		this.hexCode = hexCode;
	}
	
	/**
	 * Constructs new instance from given 6-digit hex code and legacy color
	 * @param hexCode - 6-digit hex code
	 * @param legacyColor color to use for legacy clients
	 */
	public TextColor(String hexCode, EnumChatFormat legacyColor) {
		this.hexCode = hexCode;
		this.legacyColorForced = true;
		this.legacyColor = legacyColor;
	}
	
	/**
	 * Constructs new instance with given parameter
	 * @param legacyColor - legacy color
	 */
	public TextColor(EnumChatFormat legacyColor) {
		this.red = legacyColor.getRed();
		this.green = legacyColor.getGreen();
		this.blue = legacyColor.getBlue();
		this.hexCode = legacyColor.getHexCode();
	}
	
	/**
	 * Constructs new instance with given parameters
	 * @param red - red value
	 * @param green - green value
	 * @param blue - blue value
	 */
	public TextColor(int red, int green, int blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}
	
	/**
	 * Gets closest legacy color based on rgb values
	 */
	private EnumChatFormat getClosestColor(int red, int green, int blue) {
		double minMaxDist = 9999;
		double maxDist;
		EnumChatFormat closestColor = EnumChatFormat.WHITE;
		for (EnumChatFormat color : legacyColors) {
			int rDiff = color.getRed() - red;
			int gDiff = color.getGreen() - green;
			int bDiff = color.getBlue() - blue;
			if (rDiff < 0) rDiff = -rDiff;
			if (gDiff < 0) gDiff = -gDiff;
			if (bDiff < 0) bDiff = -bDiff;
			maxDist = rDiff;
			if (gDiff > maxDist) maxDist = gDiff;
			if (bDiff > maxDist) maxDist = bDiff;
			if (maxDist < minMaxDist) {
				minMaxDist = maxDist;
				closestColor = color;
			}
		}
		return closestColor;
	}
	
	/**
	 * Returns amount of red
	 * @return amount of red
	 */
	public int getRed() {
		if (red == null) {
			int hexColor = Integer.parseInt(hexCode.substring(1), 16);
			red = (hexColor >> 16) & 0xFF;
			green = (hexColor >> 8) & 0xFF;
			blue = hexColor & 0xFF;
		}
		return red;
	}
	
	/**
	 * Returns amount of green
	 * @return amount of green
	 */
	public int getGreen() {
		if (green == null) {
			int hexColor = Integer.parseInt(hexCode.substring(1), 16);
			red = (hexColor >> 16) & 0xFF;
			green = (hexColor >> 8) & 0xFF;
			blue = hexColor & 0xFF;
		}
		return green;
	}
	
	/**
	 * Returns amount of blue
	 * @return amount of blue
	 */
	public int getBlue() {
		if (blue == null) {
			int hexColor = Integer.parseInt(hexCode.substring(1), 16);
			red = (hexColor >> 16) & 0xFF;
			green = (hexColor >> 8) & 0xFF;
			blue = hexColor & 0xFF;
		}
		return blue;
	}
	
	/**
	 * Returns defined legacy color
	 * @return defined legacy color
	 */
	public EnumChatFormat getLegacyColor() {
		if (legacyColor == null) {
			legacyColor = getClosestColor(getRed(), getGreen(), getBlue());
		}
		return legacyColor;
	}
	
	/**
	 * Returns hex code of this color
	 * @return hex code of this color
	 */
	public String getHexCode() {
		if (hexCode == null) {
			hexCode = String.format("#%06X", (red << 16) + (green << 8) + blue);
		}
		return hexCode;
	}
	
	/**
	 * Converts the color into a valid color value used in color field in chat component
	 * @return the color converted into string acceptable by client
	 */
	public String toString() {
		if (!returnLegacy) {
			EnumChatFormat legacyEquivalent = EnumChatFormat.fromRGBExact(getRed(), getGreen(), getBlue());
			if (legacyEquivalent != null) {
				//not sending old colors as RGB to 1.16 clients if not needed as <1.16 servers will fail to apply color
				return legacyEquivalent.toString().toLowerCase();
			}
			return getHexCode();
		} else {
			return getLegacyColor().toString().toLowerCase();
		}
	}
	
	/**
	 * Returns true if legacy color was forced with a constructor, false if automatically
	 * @return true if forced, false if not
	 */
	public boolean isLegacyColorForced() {
		return legacyColorForced;
	}
	
	/**
	 * Sets returnLegacy flag to given value
	 * @param returnLegacy - true if color should return legacy
	 */
	public void setReturnLegacy(boolean returnLegacy) {
		this.returnLegacy = returnLegacy;
	}
	
	/**
	 * Reads the string and turns into text color. String is either #RRGGBB or a lowercased legacy color
	 * @param string - string from color field in chat component
	 * @return An instance from specified string
	 */
	public static TextColor fromString(String string) {
		if (string == null) return null;
		if (string.startsWith("#")) return new TextColor(string);
		return new TextColor(EnumChatFormat.valueOf(string.toUpperCase()));
	}
}