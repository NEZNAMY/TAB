package me.neznamy.tab.shared.packets;

import me.neznamy.tab.bukkit.packets.EnumAPI;

public enum EnumChatFormat{

	BLACK(0, '0', EnumAPI.EnumChatFormat_BLACK), 
	DARK_BLUE(1, '1', EnumAPI.EnumChatFormat_DARK_BLUE), 
	DARK_GREEN(2, '2', EnumAPI.EnumChatFormat_DARK_GREEN), 
	DARK_AQUA(3, '3', EnumAPI.EnumChatFormat_DARK_AQUA), 
	DARK_RED(4, '4', EnumAPI.EnumChatFormat_DARK_RED), 
	DARK_PURPLE(5, '5', EnumAPI.EnumChatFormat_DARK_PURPLE), 
	GOLD(6, '6', EnumAPI.EnumChatFormat_GOLD), 
	GRAY(7, '7', EnumAPI.EnumChatFormat_GRAY), 
	DARK_GRAY(8, '8', EnumAPI.EnumChatFormat_DARK_GRAY), 
	BLUE(9, '9', EnumAPI.EnumChatFormat_BLUE), 
	GREEN(10, 'a', EnumAPI.EnumChatFormat_GREEN), 
	AQUA(11, 'b', EnumAPI.EnumChatFormat_AQUA),
	RED(12, 'c', EnumAPI.EnumChatFormat_RED),
	LIGHT_PURPLE(13, 'd', EnumAPI.EnumChatFormat_LIGHT_PURPLE), 
	YELLOW(14, 'e', EnumAPI.EnumChatFormat_YELLOW), 
	WHITE(15, 'f', EnumAPI.EnumChatFormat_WHITE), 
	OBFUSCATED(16, 'k', EnumAPI.EnumChatFormat_OBFUSCATED), 
	BOLD(17, 'l', EnumAPI.EnumChatFormat_BOLD), 
	STRIKETHROUGH(18, 'm', EnumAPI.EnumChatFormat_STRIKETHROUGH), 
	UNDERLINE(19, 'n', EnumAPI.EnumChatFormat_UNDERLINE), 
	ITALIC(20, 'o', EnumAPI.EnumChatFormat_ITALIC), 
	RESET(21, 'r', EnumAPI.EnumChatFormat_RESET);
	
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