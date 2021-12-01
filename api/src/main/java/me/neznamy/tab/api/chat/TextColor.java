package me.neznamy.tab.api.chat;

import me.neznamy.tab.api.util.Preconditions;

/**
 * A class representing a component color, which can be either RGB or legacy code.
 */
public class TextColor {

	/**
	 * RGB values from 0 to 255.
	 * They are only initialized if they are actually used to avoid
	 * unnecessary memory allocations with string operations.
	 */
	private short red = -1;
	private short green = -1;
	private short blue = -1;
	
	/** Closest legacy color to this color object. */
	private EnumChatFormat legacyColor;
	
	/** HEX code of this color as a 6-digit hexadecimal string without "#" prefix. */
	private String hexCode;
	
	/**
	 * Boolean value whether the legacy color was forced with constructor or should be
	 * automatically assigned as closest color.
	 * This value is used in gradients when converting text for legacy players. */
	private boolean legacyColorForced;

	/**
	 * Constructs new instance as a clone of the provided color.
	 * @param	color
	 * 			color to create a clone of
	 * @throws	IllegalArgumentException
	 * 			if color is {@code null}
	 */
	public TextColor(TextColor color) {
		Preconditions.checkNotNull(color, "color cannot be null");
		red = color.red;
		green = color.green;
		blue = color.blue;
		legacyColor = color.legacyColor;
		hexCode = color.hexCode;
		legacyColorForced = color.legacyColorForced;
	}
	
	/**
	 * Constructs new instance from provided 6-digit hex code string
	 * @param	hexCode
	 * 			a 6-digit combination of hex numbers as a string
	 * @throws	IllegalArgumentException
	 * 			if hexCode is {@code null}
	 */
	public TextColor(String hexCode) {
		Preconditions.checkNotNull(hexCode, "hex code cannot be null");
		this.hexCode = hexCode;
	}
	
	/**
	 * Constructs new instance from provided 6-digit hex code and forced legacy color
	 * @param	hexCode
	 * 			6-digit combination of hex numbers as a string
	 * @param	legacyColor
	 * 			color to use for legacy clients instead of using the closest legacy color
	 * @throws	IllegalArgumentException
	 * 			if {@code hexCode} is {@code null} or {@code legacyColor} is {@code null}
	 */
	public TextColor(String hexCode, EnumChatFormat legacyColor) {
		Preconditions.checkNotNull(hexCode, "hex code cannot be null");
		Preconditions.checkNotNull(legacyColor, "legacy color cannot be null");
		this.hexCode = hexCode;
		this.legacyColorForced = true;
		this.legacyColor = legacyColor;
	}
	
	/**
	 * Constructs new instance from provided legacy color
	 * @param	legacyColor
	 * 			legacy color to construct the instance from
	 * @throws	IllegalArgumentException
	 * 			if {@code legacyColor} is {@code null}
	 */
	public TextColor(EnumChatFormat legacyColor) {
		Preconditions.checkNotNull(legacyColor, "legacy color cannot be null");
		this.red = legacyColor.getRed();
		this.green = legacyColor.getGreen();
		this.blue = legacyColor.getBlue();
		this.hexCode = legacyColor.getHexCode();
	}
	
	/**
	 * Constructs new instance with red, green and blue values
	 * @param	red
	 * 			red value
	 * @param	green
	 * 			green value
	 * @param	blue
	 * 			blue value
	 * @throws	IllegalArgumentException
	 * 			if {@code red}, {@code green} or {@code blue} is out of range ({@code 0-255})
	 */
	public TextColor(int red, int green, int blue) {
		Preconditions.checkRange(red, 0, 255);
		Preconditions.checkRange(green, 0, 255);
		Preconditions.checkRange(blue, 0, 255);
		this.red = (short) red;
		this.green = (short) green;
		this.blue = (short) blue;
	}
	
	/**
	 * Loads the closest legacy color based currently provided values
	 */
	private EnumChatFormat loadClosestColor() {
		double minMaxDist = 9999;
		double maxDist;
		EnumChatFormat closestColor = EnumChatFormat.WHITE;
		for (EnumChatFormat color : EnumChatFormat.values()) {
			int rDiff = Math.abs(color.getRed() - getRed());
			int gDiff = Math.abs(color.getGreen() - getGreen());
			int bDiff = Math.abs(color.getBlue() - getBlue());
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
	 * Returns {@code red} value
	 * @return	red value
	 */
	public int getRed() {
		if (red == -1) loadColors();
		return red;
	}

	/**
	 * Returns {@code green} value
	 * @return	green value
	 */
	public int getGreen() {
		if (green == -1) loadColors();
		return green;
	}

	/**
	 * Returns {@code blue} value
	 * @return	blue value
	 */
	public int getBlue() {
		if (blue == -1) loadColors();
		return blue;
	}

	/**
	 * Loads red, green and blue values based on current hex code string
	 */
	private void loadColors(){
		int hexColor = Integer.parseInt(hexCode, 16);
		red = (short) ((hexColor >> 16) & 0xFF);
		green = (short) ((hexColor >> 8) & 0xFF);
		blue = (short) (hexColor & 0xFF);
	}
	
	/**
	 * Returns the closest legacy color of this color object.
	 * If the color was defined in constructor, it's returned.
	 * Otherwise, the closest color is calculated the then returned.
	 * @return	closest legacy color
	 */
	public EnumChatFormat getLegacyColor() {
		if (legacyColor == null) {
			legacyColor = loadClosestColor();
		}
		return legacyColor;
	}
	
	/**
	 * Returns the rgb combination as a 6-digit hex code string
	 * @return	the rgb combination as a 6-digit hex code string
	 */
	public String getHexCode() {
		if (hexCode == null) {
			hexCode = String.format("%06X", (red << 16) + (green << 8) + blue);
		}
		return hexCode;
	}
	
	/**
	 * Converts the color into a valid color value used in color field in chat component.
	 * That is either 6-digit hex code prefixed with '#', or lowercase legacy color.
	 * @return	the color serialized for use in chat component
	 */
	public String toString() {
		EnumChatFormat legacyEquivalent = EnumChatFormat.fromRGBExact(getRed(), getGreen(), getBlue());
		if (legacyEquivalent != null) {
			//not sending old colors as RGB to 1.16 clients if not needed as <1.16 servers will fail to apply color
			return legacyEquivalent.toString().toLowerCase();
		}
		return "#" + getHexCode();
	}
	
	/**
	 * Returns true if legacy color was forced with a constructor, false if not
	 * @return	true if forced, false if not
	 */
	public boolean isLegacyColorForced() {
		return legacyColorForced;
	}

	/**
	 * Reads the string as it appears in chat component and turns it into the color object.
	 * If the entered string is null, returns null.
	 * If it's prefixed with '#', it's considered as a hex code.
	 * Otherwise, it is considered being a lowercase legacy color.
	 * @param	string
	 * 			string from color field in chat component
	 * @return	An instance from specified string or null if string is null
	 */
	public static TextColor fromString(String string) {
		if (string == null) return null;
		if (string.startsWith("#")) return new TextColor(string.substring(1));
		return new TextColor(EnumChatFormat.valueOf(string.toUpperCase()));
	}
}