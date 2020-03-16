package me.neznamy.tab.shared.packets;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.placeholders.Placeholders;

public enum EnumChatFormat{

	BLACK(0, '0'),
	DARK_BLUE(1, '1'),
	DARK_GREEN(2, '2'),
	DARK_AQUA(3, '3'),
	DARK_RED(4, '4'),
	DARK_PURPLE(5, '5'),
	GOLD(6, '6'),
	GRAY(7, '7'),
	DARK_GRAY(8, '8'),
	BLUE(9, '9'),
	GREEN(10, 'a'),
	AQUA(11, 'b'),
	RED(12, 'c'),
	LIGHT_PURPLE(13, 'd'),
	YELLOW(14, 'e'),
	WHITE(15, 'f'),
	OBFUSCATED(16, 'k'),
	BOLD(17, 'l'),
	STRIKETHROUGH(18, 'm'),
	UNDERLINE(19, 'n'),
	ITALIC(20, 'o'),
	RESET(21, 'r');
	
	private int networkId;
	private char character;
	private Object nmsEquivalent;

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
}