package me.neznamy.tab.bukkit.packets;

import org.bukkit.GameMode;

public enum EnumGamemode{

	NOT_SET(EnumAPI.EnumGamemode_NOT_SET), 
	SURVIVAL(EnumAPI.EnumGamemode_SURVIVAL), 
	CREATIVE(EnumAPI.EnumGamemode_CREATIVE), 
	ADVENTURE(EnumAPI.EnumGamemode_ADVENTURE), 
	SPECTATOR(EnumAPI.EnumGamemode_SPECTATOR);

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