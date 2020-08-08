package me.neznamy.tab.platforms.bukkit.packets;

import java.util.Map;

import me.neznamy.tab.shared.ProtocolVersion;

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
	public static Object VillagerData;
	public static Object OptionalInt;
	public static Object EntityPose;

	static {
		if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 9) {
			Map<String, Object> fields = PacketPlayOut.getStaticFields(PacketPlayOut.getNMSClass("DataWatcherRegistry"));
			Byte = fields.get("a");
			Integer = fields.get("b");
			Float = fields.get("c");
			String = fields.get("d");
			IChatBaseComponent = fields.get("e");
			if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 13) {
				Optional_IChatBaseComponent = fields.get("f");
				ItemStack = fields.get("g");
				Optional_IBlockData = fields.get("h");
				Boolean = fields.get("i");
				ParticleParam = fields.get("j");
				Vector3f = fields.get("k");
				BlockPosition = fields.get("l");
				Optional_BlockPosition = fields.get("m");
				EnumDirection = fields.get("n");
				Optional_UUID = fields.get("o");
				NBTTagCompound = fields.get("p");
				if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 15) {
					VillagerData = fields.get("q");
					OptionalInt = fields.get("r");
					EntityPose = fields.get("s");
				}
			} else {
				Optional_IBlockData = fields.get("g");
				Boolean = fields.get("h");
				Vector3f = fields.get("i");
				BlockPosition = fields.get("j");
				Optional_BlockPosition = fields.get("k");
				EnumDirection = fields.get("l");
				Optional_UUID = fields.get("m");
				if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 12) {
					NBTTagCompound = fields.get("n");
				}
				if (ProtocolVersion.SERVER_VERSION.getMinorVersion() >= 11) {
					ItemStack = fields.get("f");
				} else {
					Optional_ItemStack = fields.get("f");
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
	}
}