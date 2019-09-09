package me.neznamy.tab.shared.packets;

import me.neznamy.tab.platforms.bukkit.packets.EnumConstant;

public enum EnumChatFormat{

	BLACK(0, '0', EnumConstant.EnumChatFormat_BLACK), 
	DARK_BLUE(1, '1', EnumConstant.EnumChatFormat_DARK_BLUE), 
	DARK_GREEN(2, '2', EnumConstant.EnumChatFormat_DARK_GREEN), 
	DARK_AQUA(3, '3', EnumConstant.EnumChatFormat_DARK_AQUA), 
	DARK_RED(4, '4', EnumConstant.EnumChatFormat_DARK_RED), 
	DARK_PURPLE(5, '5', EnumConstant.EnumChatFormat_DARK_PURPLE), 
	GOLD(6, '6', EnumConstant.EnumChatFormat_GOLD), 
	GRAY(7, '7', EnumConstant.EnumChatFormat_GRAY), 
	DARK_GRAY(8, '8', EnumConstant.EnumChatFormat_DARK_GRAY), 
	BLUE(9, '9', EnumConstant.EnumChatFormat_BLUE), 
	GREEN(10, 'a', EnumConstant.EnumChatFormat_GREEN), 
	AQUA(11, 'b', EnumConstant.EnumChatFormat_AQUA),
	RED(12, 'c', EnumConstant.EnumChatFormat_RED),
	LIGHT_PURPLE(13, 'd', EnumConstant.EnumChatFormat_LIGHT_PURPLE), 
	YELLOW(14, 'e', EnumConstant.EnumChatFormat_YELLOW), 
	WHITE(15, 'f', EnumConstant.EnumChatFormat_WHITE), 
	OBFUSCATED(16, 'k', EnumConstant.EnumChatFormat_OBFUSCATED), 
	BOLD(17, 'l', EnumConstant.EnumChatFormat_BOLD), 
	STRIKETHROUGH(18, 'm', EnumConstant.EnumChatFormat_STRIKETHROUGH), 
	UNDERLINE(19, 'n', EnumConstant.EnumChatFormat_UNDERLINE), 
	ITALIC(20, 'o', EnumConstant.EnumChatFormat_ITALIC), 
	RESET(21, 'r', EnumConstant.EnumChatFormat_RESET);
	
	private int bungeeEquivalent;
	private char character;
	private Object nmsEquivalent;

	private EnumChatFormat(int bungeeEquivalent, char character, Object nmsEquivalent) {
		this.bungeeEquivalent = bungeeEquivalent;
		this.character = character;
		this.nmsEquivalent = nmsEquivalent;
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