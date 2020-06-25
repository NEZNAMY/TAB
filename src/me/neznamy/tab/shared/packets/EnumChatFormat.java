package me.neznamy.tab.shared.packets;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.placeholders.Placeholders;

public enum EnumChatFormat{

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

	private int networkId;
	private char character;
	private Object nmsEquivalent;
	public int hexColor;
	public int red, green, blue;

	private EnumChatFormat(int networkId, char character, int hexColor) {
		this(networkId, character);
		this.hexColor = hexColor;
		red = (hexColor >> 16) & 0xFF;
		green = (hexColor >> 8) & 0xFF;
		blue = hexColor & 0xFF;
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private EnumChatFormat(int networkId, char character) {
		this.networkId = networkId;
		this.character = character;
		if (MethodAPI.getInstance() != null) this.nmsEquivalent = Enum.valueOf((Class<Enum>)MethodAPI.EnumChatFormat, toString());
	}
	public Object toNMS() {
		return nmsEquivalent;
	}
	public int getNetworkId() {
		return networkId;
	}
	public static EnumChatFormat getByChar(char c) {
		for (EnumChatFormat format : values()) {
			if (format.character == c) return format;
		}
		return null;
	}
	public static EnumChatFormat lastColorsOf(String string) {
		if (string == null || string.length() == 0) return EnumChatFormat.RESET;
		String last = Placeholders.getLastColors(string);
		if (last != null && last.length() > 0) {
			char c = last.toCharArray()[1];
			for (EnumChatFormat e : values()) {
				if (e.character == c) return e;
			}
		}
		return EnumChatFormat.RESET;
	}
	public String getFormat() {
		return Placeholders.colorChar + "" + character;
	}
	public static EnumChatFormat fromRGBExact(int red, int green, int blue){
		for (EnumChatFormat format : values()) {
			if (format.red == red && format.green == green && format.blue == blue) return format;
		}
		return null;
	}
}