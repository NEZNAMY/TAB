package me.neznamy.tab.shared.rgb;

import me.neznamy.tab.shared.packets.EnumChatFormat;

/**
 * A class used to represent any combination of RGB colors
 */
public class TextColor {

	private static EnumChatFormat[] legacyColors = EnumChatFormat.values();
	//red value
	private int red;
	
	//green value
	private int green;
	
	//blue value
	private int blue;
	
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
		int hexColor = Integer.parseInt(hexCode.substring(1), 16);
		set((hexColor >> 16) & 0xFF, (hexColor >> 8) & 0xFF, hexColor & 0xFF, getClosestColor((hexColor >> 16) & 0xFF, (hexColor >> 8) & 0xFF, hexColor & 0xFF), false, hexCode);
	}
	
	/**
	 * Constructs new instance from given 6-digit hex code and legacy color
	 * @param hexCode - 6-digit hex code
	 * @param legacyColor color to use for legacy clients
	 */
	public TextColor(String hexCode, EnumChatFormat legacyColor) {
		int hexColor = Integer.parseInt(hexCode.substring(1), 16);
		set((hexColor >> 16) & 0xFF, (hexColor >> 8) & 0xFF, hexColor & 0xFF, legacyColor, true, hexCode);
	}
	
	/**
	 * Constructs new instance with given parameter
	 * @param legacyColor - legacy color
	 */
	public TextColor(EnumChatFormat legacyColor) {
		set(legacyColor.getRed(), legacyColor.getGreen(), legacyColor.getBlue(), legacyColor, false, legacyColor.getHexCode());
	}
	
	/**
	 * Constructs new instance with given parameters
	 * @param red - red value
	 * @param green - green value
	 * @param blue - blue value
	 */
	public TextColor(int red, int green, int blue) {
		set(red, green, blue, getClosestColor(red, green, blue), false, String.format("#%06X", (red << 16) + (green << 8) + blue));
	}
	
	/**
	 * Sets all parameters to given values
	 * @param red - color's red value
	 * @param green - color's green value
	 * @param blue - color's blue value
	 * @param legacyColor - closest legacy color
	 * @param legacyColorForced - if legacy color was forced
	 * @param hexCode - color's hex code
	 */
	public void set(int red, int green, int blue, EnumChatFormat legacyColor, boolean legacyColorForced, String hexCode) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.legacyColor = legacyColor;
		this.legacyColorForced = legacyColorForced;
		this.hexCode = hexCode;
	}
	
	/**
	 * Gets closest legacy color based on rgb values
	 */
	private EnumChatFormat getClosestColor(int red, int green, int blue) {
		double minMaxDist = 9999;
		double maxDist;
		EnumChatFormat legacyColor = EnumChatFormat.WHITE;
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
				legacyColor = color;
			}
		}
		return legacyColor;
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
	 * Returns hex code of this color
	 * @return hex code of this color
	 */
	public String getHexCode() {
		return hexCode;
	}
	
	/**
	 * Converts the color into a valid color value used in color field in chat component
	 * @param rgbClient - if client accepts RGB or not
	 * @return the color converted into string acceptable by client
	 */
	public String toString(boolean rgbClient) {
		if (rgbClient && !returnLegacy) {
			EnumChatFormat legacyEquivalent = EnumChatFormat.fromRGBExact(red, green, blue);
			if (legacyEquivalent != null) {
				//not sending old colors as RGB to 1.16 clients if not needed as <1.16 servers will fail to apply color
				return legacyEquivalent.toString().toLowerCase();
			}
			return hexCode;
		} else {
			return legacyColor.toString().toLowerCase();
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
		if (string.startsWith("#")) return new TextColor(string.substring(1));
		if (string.startsWith("\u00a7")) return new TextColor(EnumChatFormat.getByChar(string.toLowerCase().charAt(1)));
		return new TextColor(EnumChatFormat.valueOf(string.toUpperCase()));
	}
}