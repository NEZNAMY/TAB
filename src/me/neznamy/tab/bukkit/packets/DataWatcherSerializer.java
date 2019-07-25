package me.neznamy.tab.bukkit.packets;

import me.neznamy.tab.shared.Shared;

public class DataWatcherSerializer {

	public static Object Byte;
	public static Object Short;
	public static Object Integer;
	public static Object Float;
	public static Object String;
	public static Object IChatBaseComponent;
	public static Object Optional_IChatBaseComponent;
	@Deprecated
	public static Object Optional_ItemStack;
	public static Object ItemStack;
	public static Object Optional_IBlockData;
	public static Object Boolean;
	public static Object ParticleParam;
	public static Object Vector3f;
	public static Object BlockPosition;
	public static Object Optional_BlockPosition;
	public static Object EnumDirection;
	public static Object Optional_UUID;
	public static Object NBTTagCompound;

	static {
		try {
			if (NMSClass.versionNumber >= 9) {
				Class<?> DWR = NMSClass.get("DataWatcherRegistry");
				Byte = DWR.getDeclaredField("a").get(null);
				Integer = DWR.getDeclaredField("b").get(null);
				Float = DWR.getDeclaredField("c").get(null);
				String = DWR.getDeclaredField("d").get(null);
				IChatBaseComponent = DWR.getDeclaredField("e").get(null);
				if (NMSClass.versionNumber >= 13) {
					Optional_IChatBaseComponent = DWR.getDeclaredField("f").get(null);
					ItemStack = DWR.getDeclaredField("g").get(null);
					Optional_IBlockData = DWR.getDeclaredField("h").get(null);
					Boolean = DWR.getDeclaredField("i").get(null);
					ParticleParam = DWR.getDeclaredField("j").get(null);
					Vector3f = DWR.getDeclaredField("k").get(null);
					BlockPosition = DWR.getDeclaredField("l").get(null);
					Optional_BlockPosition = DWR.getDeclaredField("m").get(null);
					EnumDirection = DWR.getDeclaredField("n").get(null);
					Optional_UUID = DWR.getDeclaredField("o").get(null);
					NBTTagCompound = DWR.getDeclaredField("p").get(null);
				} else {
					Optional_IBlockData = DWR.getDeclaredField("g").get(null);
					Boolean = DWR.getDeclaredField("h").get(null);
					Vector3f = DWR.getDeclaredField("i").get(null);
					BlockPosition = DWR.getDeclaredField("j").get(null);
					Optional_BlockPosition = DWR.getDeclaredField("k").get(null);
					EnumDirection = DWR.getDeclaredField("l").get(null);
					Optional_UUID = DWR.getDeclaredField("m").get(null);
					if (NMSClass.versionNumber >= 12) {
						NBTTagCompound = DWR.getDeclaredField("n").get(null);
					}
					if (NMSClass.versionNumber >= 11) {
						ItemStack = DWR.getDeclaredField("f").get(null);
					} else {
						Optional_ItemStack = DWR.getDeclaredField("f").get(null);
					}
				}
			} else {
				Byte = 0;
				Short = 1;
				Integer = 2;
				Float = 3;
				String = 4;
				ItemStack = 5;
				BlockPosition = 6;
				Vector3f = 7;
			}
		} catch (Exception e) {
			Shared.error("Failed to initialize DataWatcherSerializer class", e);
		}
	}
}