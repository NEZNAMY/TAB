package me.neznamy.tab.bukkit.packets;

import org.bukkit.GameMode;

public enum EnumGamemode{

	NOT_SET(EnumConstant.EnumGamemode_NOT_SET), 
	SURVIVAL(EnumConstant.EnumGamemode_SURVIVAL), 
	CREATIVE(EnumConstant.EnumGamemode_CREATIVE), 
	ADVENTURE(EnumConstant.EnumGamemode_ADVENTURE), 
	SPECTATOR(EnumConstant.EnumGamemode_SPECTATOR);

	private Object nmsEquivalent;
	
	private EnumGamemode(Object nmsEquivalent) {
		this.nmsEquivalent = nmsEquivalent;
	}
	public static EnumGamemode fromNMS(Object nmsCommand) {
		return EnumGamemode.valueOf(nmsCommand.toString());
    }
	public static EnumGamemode fromBukkit(GameMode gamemode) {
		if (gamemode == null) return EnumGamemode.NOT_SET;
		return EnumGamemode.valueOf(gamemode.toString());
    }
    public Object toNMS() {
    	return nmsEquivalent;
    }
    public GameMode toBukkit() {
    	if (this == NOT_SET) return GameMode.SURVIVAL;
    	return GameMode.valueOf(toString());
    }
}