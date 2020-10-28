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
	 * Constructs new instance with all argments
	 * Private, use TextColor.of methods
	 * @param red - red value
	 * @param green - green value
	 * @param blue - blue value
	 * @param legacyColor - closest legacy color
	 */
	private TextColor(int red, int green, int blue, EnumChatFormat legacyColor) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.legacyColor = legacyColor;
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
	 * Returns the closest legacy color
	 * @return closest legacy color
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
			return "#" + RGBUtils.toHexString(red, green, blue);
		} else {
			return legacyColor.toString().toLowerCase();
		}
	}
	
	/**
	 * Reads the string and turns into text color. String is either #RRGGBB or a lowercased legacy color
	 * @param string - string from color field in chat component
	 * @return An instance from specified string
	 */
	public static TextColor fromString(String string) {
		if (string == null) return null;
		if (string.startsWith("#")) {
			return of(string.substring(1));
		} else {
			return of(EnumChatFormat.valueOf(string.toUpperCase()));
		}
	}
	
	/**
	 * Returns a new instance based on hex code as string
	 * @param hexCode - a 6-digit combination of hex numbers
	 * @return TextColor from hex color
	 */
	public static TextColor of(String hexCode) {
		int hexColor = Integer.parseInt(hexCode, 16);
		int red = ((hexColor >> 16) & 0xFF);
		int green = ((hexColor >> 8) & 0xFF);
		int blue = (hexColor & 0xFF);
		return of(red, green, blue);
	}
	
	/**
	 * Returns a new instance based on legacy color
	 * @param legacyColor - legacy color
	 * @return TextColor from legacy color
	 */
	public static TextColor of(EnumChatFormat legacyColor) {
		int red = legacyColor.getRed();
		int green = legacyColor.getGreen();
		int blue = legacyColor.getBlue();
		return new TextColor(red, green, blue, legacyColor);
	}
	
	/**
	 * Returns a new instance based on color bytes
	 * @param red - red value
	 * @param green - green value
	 * @param blue - blue value
	 * @return TextColor from RGB combination
	 */
	public static TextColor of(int red, int green, int blue) {
		double minDist = 9999;
		double dist;
		EnumChatFormat legacyColor = EnumChatFormat.WHITE;
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
		return new TextColor(red, green, blue, legacyColor);
	}
}