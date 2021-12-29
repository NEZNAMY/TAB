package me.neznamy.tab.api.chat;

import me.neznamy.tab.api.chat.rgb.RGBUtils;

/**
 * An enum containing all possible legacy color codes and magic codes. Also contains handy color-related methods.
 */
public enum EnumChatFormat {

	BLACK('0', "000000"),
	DARK_BLUE('1', "0000AA"),
	DARK_GREEN('2', "00AA00"),
	DARK_AQUA('3', "00AAAA"),
	DARK_RED('4', "AA0000"),
	DARK_PURPLE('5', "AA00AA"),
	GOLD('6', "FFAA00"),
	GRAY('7', "AAAAAA"),
	DARK_GRAY('8', "555555"),
	BLUE('9', "5555FF"),
	GREEN('a', "55FF55"),
	AQUA('b', "55FFFF"),
	RED('c', "FF5555"),
	LIGHT_PURPLE('d', "FF55FF"),
	YELLOW('e', "FFFF55"),
	WHITE('f', "FFFFFF"),
	OBFUSCATED('k'),
	BOLD('l'),
	STRIKETHROUGH('m'),
	UNDERLINE('n'),
	ITALIC('o'),
	RESET('r');

	/** Creating a constant to avoid memory allocations on each request */
	private static final EnumChatFormat[] VALUES = values();

	/** The symbol minecraft uses to colorize text */
	public static final char COLOR_CHAR = 0x00a7;

	/** The color symbol in form of a string */
	public static final String COLOR_STRING = String.valueOf(COLOR_CHAR);

	/** Character representing the color or magic code */
	private final char character;
	
	/** Red value of this constant, 0 for magic codes */
	private final short red;

	/** Green value of this constant, 0 for magic codes */
	private final short green;

	/** Blue value of this constant, 0 for magic codes */
	private final short blue;
	
	/** Color as 6-digit hex code, null for magic codes */
	private final String hexCode;
	
	/** Color symbol followed by constant's character */
	private final String chatFormat;

	/**
	 * Constructs new color instance with given character and hex code
	 * @param	character
	 * 			character which the color goes by
	 * @param	hexCode
	 * 			6-digit hex code of the color
	 */
	EnumChatFormat(char character, String hexCode) {
		this.character = character;
		this.chatFormat = String.valueOf(COLOR_CHAR) + character;
		this.hexCode = hexCode;
		int hexColor = Integer.parseInt(hexCode, 16);
		red = (short) ((hexColor >> 16) & 0xFF);
		green = (short) ((hexColor >> 8) & 0xFF);
		blue = (short) (hexColor & 0xFF);
	}
	
	/**
	 * Constructs new magic code instance with given character
	 * @param	character
	 * 			character representing the magic code
	 */
	EnumChatFormat(char character) {
		this.character = character;
		this.chatFormat = String.valueOf(COLOR_CHAR) + character;
		red = 0;
		green = 0;
		blue = 0;
		hexCode = null;
	}

	/**
	 * Returns red value of this color code
	 * @return	red value
	 */
	public short getRed() {
		return red;
	}
	
	/**
	 * Returns green value of this color code
	 * @return	green value
	 */
	public short getGreen() {
		return green;
	}
	
	/**
	 * Returns blue value of this color code
	 * @return	blue value
	 */
	public short getBlue() {
		return blue;
	}
	
	/**
	 * Returns enum value based on provided character or null if character is not valid
	 * @param	c
	 * 			color code character (0-9, a-f, k-o, r)
	 * @return	instance from the character or null if character is not valid
	 */
	public static EnumChatFormat getByChar(char c) {
		for (EnumChatFormat format : VALUES) {
			if (format.character == c) return format;
		}
		return null;
	}
	
	/**
	 * Returns enum value of last colors used in given string.
	 * If it's null, empty or does not contain color codes, WHITE is returned.
	 * @param	string
	 * 			string to check last colors of
	 * @return	last used color code in given string or WHITE if nothing is found
	 */
	public static EnumChatFormat lastColorsOf(String string) {
		if (string == null || string.length() == 0) return EnumChatFormat.WHITE;
		String legacyText = RGBUtils.getInstance().convertRGBtoLegacy(string);
		String last = getLastColors(legacyText);
		if (last.length() > 0) {
			char c = last.toCharArray()[1];
			for (EnumChatFormat e : VALUES) {
				if (e.character == c) return e;
			}
		}
		return EnumChatFormat.WHITE;
	}
	
	/**
	 * Returns color char followed by color's character
	 * @return color char followed by color's character
	 */
	public String getFormat() {
		return chatFormat;
	}
	
	/**
	 * Returns character representing this color
	 * @return character representing this color
	 */
	public char getCharacter() {
		return character;
	}
	
	/**
	 * Returns hex code of this format, null if this is a magic code
	 * @return hex code of this format
	 */
	public String getHexCode() {
		return hexCode;
	}
	
	/**
	 * Returns enum value with exact red, green and blue values or null if no match was found
	 * @param	red
	 * 			exact red value
	 * @param	green
	 * 			exact green value
	 * @param	blue
	 * 			exact blue value
	 * @return	enum value or null if no such combination exists
	 */
	public static EnumChatFormat fromRGBExact(int red, int green, int blue){
		for (EnumChatFormat format : VALUES) {
			if (format.red == red && format.green == green && format.blue == blue) return format;
		}
		return null;
	}

	/**
	 * Color translation method taken from bukkit, which converts '&' symbol into
	 * the actual color character if followed by a valid color character.
	 * @param	textToTranslate
	 * 			text to replace color symbol in
	 * @return	colorized string from provided text
	 */
	public static String color(String textToTranslate){
		if (textToTranslate == null) return null;
		if (!textToTranslate.contains("&")) return textToTranslate;
		char[] b = textToTranslate.toCharArray();
		for (int i = 0; i < b.length - 1; i++) {
			if ((b[i] == '&') && ("0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[(i + 1)]) > -1)){
				b[i] = COLOR_CHAR;
				b[(i + 1)] = Character.toLowerCase(b[(i + 1)]);
			}
		}
		return new String(b);
	}

	/**
	 * Turns back the color symbol into '&' symbol in provided text.
	 * @param	text
	 * 			text to revert colors in
	 * @return	reverted text
	 */
	public static String decolor(String text) {
		if (text == null) return null;
		if (!text.contains(COLOR_STRING)) return text;
		return text.replace(COLOR_CHAR, '&');
	}

	/**
	 * Code taken from bukkit, which returns last color codes used in provided text.
	 * @param	input
	 * 			text to get last colors from
	 * @return	last colors used in provided text or empty string if nothing was found
	 */
	public static String getLastColors(String input) {
		StringBuilder result = new StringBuilder();
		int length = input.length();
		for (int index = length - 1; index > -1; index--){
			char section = input.charAt(index);
			if ((section == COLOR_CHAR || section == '&') && (index < length - 1)){
				char c = input.charAt(index + 1);
				if ("0123456789AaBbCcDdEeFfKkLlMmNnOoRr".contains(String.valueOf(c))) {
					result.insert(0, COLOR_CHAR);
					result.insert(1, c);
					if ("0123456789AaBbCcDdEeFfRr".contains(String.valueOf(c))) {
						break;
					}
				}
			}
		}
		return result.toString();
	}
}