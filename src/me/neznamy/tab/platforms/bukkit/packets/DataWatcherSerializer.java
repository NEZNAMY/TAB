package me.neznamy.tab.platforms.bukkit.packets;

import java.lang.reflect.Field;
import java.util.Map;

import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.ProtocolVersion;
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
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
				Map<String, Field> fields = PacketPlayOut.getStaticFields(MethodAPI.DataWatcherRegistry);
				Byte = fields.get("a").get(null);
				Integer = fields.get("b").get(null);
				Float = fields.get("c").get(null);
				String = fields.get("d").get(null);
				IChatBaseComponent = fields.get("e").get(null);
				if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
					Optional_IChatBaseComponent = fields.get("f").get(null);
					ItemStack = fields.get("g").get(null);
					Optional_IBlockData = fields.get("h").get(null);
					Boolean = fields.get("i").get(null);
					ParticleParam = fields.get("j").get(null);
					Vector3f = fields.get("k").get(null);
					BlockPosition = fields.get("l").get(null);
					Optional_BlockPosition = fields.get("m").get(null);
					EnumDirection = fields.get("n").get(null);
					Optional_UUID = fields.get("o").get(null);
					NBTTagCompound = fields.get("p").get(null);
				} else {
					Optional_IBlockData = fields.get("g").get(null);
					Boolean = fields.get("h").get(null);
					Vector3f = fields.get("i").get(null);
					BlockPosition = fields.get("j").get(null);
					Optional_BlockPosition = fields.get("k").get(null);
					EnumDirection = fields.get("l").get(null);
					Optional_UUID = fields.get("m").get(null);
					if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 12) {
						NBTTagCompound = fields.get("n").get(null);
					}
					if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 11) {
						ItemStack = fields.get("f").get(null);
					} else {
						Optional_ItemStack = fields.get("f").get(null);
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
			Shared.error("Failed to initialize class ", e);
		}
	}
}