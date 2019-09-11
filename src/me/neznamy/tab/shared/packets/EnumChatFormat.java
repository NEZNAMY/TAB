package me.neznamy.tab.shared.packets;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;

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
	
	private int bungeeEquivalent;
	private char character;
	private Object nmsEquivalent;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private EnumChatFormat(int bungeeEquivalent, char character) {
		this.bungeeEquivalent = bungeeEquivalent;
		this.character = character;
		this.nmsEquivalent = Enum.valueOf((Class<Enum>)MethodAPI.EnumChatFormat, toString());
	}
	public static EnumChatFormat getByCharacter(char c) {
		for (EnumChatFormat e : values()) {
			if (e.character == c) return e;
		}
		return EnumChatFormat.RESET;
	}
	public Object toNMS() {
		return nmsEquivalent;
	}
	public int toBungee() {
		return bungeeEquivalent;
	}
}