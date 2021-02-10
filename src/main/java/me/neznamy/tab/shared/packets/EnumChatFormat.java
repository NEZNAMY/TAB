package me.neznamy.tab.shared.packets;

import me.neznamy.tab.shared.TAB;

/**
 * A class representing the n.m.s.EnumChatFormat class to make work with it much easier
 */
public enum EnumChatFormat {

	BLACK(0, '0', 0x000000),
	DARK_BLUE(1, '1', 0x0000AA),
	DARK_GREEN(2, '2', 0x00AA00),
	DARK_AQUA(3, '3', 0x00AAAA),
	DARK_RED(4, '4', 0xAA0000),
	DARK_PURPLE(5, '5', 0xAA00AA),
	GOLD(6, '6', 0xFFAA00),
	GRAY(7, '7', 0xAAAAAA),
	DARK_GRAY(8, '8', 0x555555),
	BLUE(9, '9', 0x5555FF),
	GREEN(10, 'a', 0x55FF55),
	AQUA(11, 'b', 0x55FFFF),
	RED(12, 'c', 0xFF5555),
	LIGHT_PURPLE(13, 'd', 0xFF55FF),
	YELLOW(14, 'e', 0xFFFF55),
	WHITE(15, 'f', 0xFFFFFF),
	OBFUSCATED(16, 'k'),
	BOLD(17, 'l'),
	STRIKETHROUGH(18, 'm'),
	UNDERLINE(19, 'n'),
	ITALIC(20, 'o'),
	RESET(21, 'r');

	//network id of the color
	private int networkId;
	
	//characer representing the color
	private char character;
	
	//red value of this color
	private int red;
	
	//green value of this color;
	private int green;
	
	//blue value of this color
	private int blue;

	/**
	 * Constructs new instance with given parameters
	 * @param networkId - network id of the color
	 * @param character - character representing the color
	 * @param hexColor - hex code of the color
	 */
	private EnumChatFormat(int networkId, char character, int hexColor) {
		this(networkId, character);
		red = (hexColor >> 16) & 0xFF;
		green = (hexColor >> 8) & 0xFF;
		blue = hexColor & 0xFF;
	}
	
	/**
	 * Constructs new instance with given parameters
	 * @param networkId - network id of the color
	 * @param character - character representing the color
	 */
	private EnumChatFormat(int networkId, char character) {
		this.networkId = networkId;
		this.character = character;
	}
	
	/**
	 * Returns network id of this color
	 * @return network if of this color
	 */
	public int getNetworkId() {
		return networkId;
	}
	
	/**
	 * Returns red value of this color
	 * @return red value
	 */
	public int getRed() {
		return red;
	}
	
	/**
	 * Returns green value of this color
	 * @return green value
	 */
	public int getGreen() {
		return green;
	}
	
	/**
	 * Returns blue value of this color
	 * @return blue value
	 */
	public int getBlue() {
		return blue;
	}
	
	/**
	 * Returns enum value based on inserted color or null if character is not valid
	 * @param c - color code character (0-9, a-f, k-o, r)
	 * @return instance from the character
	 */
	public static EnumChatFormat getByChar(char c) {
		for (EnumChatFormat format : values()) {
			if (format.character == c) return format;
		}
		return null;
	}
	
	/**
	 * Returns enum value of last colors used in given string
	 * @param string - string to check last colors of
	 * @return last used color code in given string
	 */
	public static EnumChatFormat lastColorsOf(String string) {
		if (string == null || string.length() == 0) return EnumChatFormat.WHITE;
		String last = TAB.getInstance().getPlaceholderManager().getLastColors(IChatBaseComponent.fromColoredText(string).toLegacyText());
		if (last != null && last.length() > 0) {
			char c = last.toCharArray()[1];
			for (EnumChatFormat e : values()) {
				if (e.character == c) return e;
			}
		}
		return EnumChatFormat.WHITE;
	}
	
	/**
	 * Returns \u00a7 followed by color's character
	 * @return \u00a7 followed by color's character
	 */
	public String getFormat() {
		return '\u00a7' + "" + character;
	}
	
	public char getCharacter() {
		return character;
	}
	
	/**
	 * Returns enum value with exact red, green and blue values or null if no match
	 * @param red - exact red value
	 * @param green - exact green value
	 * @param blue - exact blue value
	 * @return enum value or null if no such combination exists
	 */
	public static EnumChatFormat fromRGBExact(int red, int green, int blue){
		for (EnumChatFormat format : values()) {
			if (format.red == red && format.green == green && format.blue == blue) return format;
		}
		return null;
	}
}