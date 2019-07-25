package me.neznamy.tab.bukkit.packets;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;

import com.mojang.authlib.GameProfile;

import me.neznamy.tab.shared.Shared;

public class PacketAPI{
	
	private static final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	private static final int versionNumber = Integer.parseInt(version.split("_")[1]);
	private static final boolean versionSupported = (versionNumber >= 8 && versionNumber < 14) || version.equals("v1_14_R1");
	
	public static Class<?> PacketPlayInUseEntity;
	public static Field PacketPlayInUseEntity_ENTITYID;
	
	public static Class<?> PacketPlayInChat;
	public static Field PacketPlayInChat_MESSAGE;
	
	public static Class<?> PacketPlayOutBed;
	public static Field PacketPlayOutBed_ENTITY;
	
	public static Class<?> PacketPlayOutAnimation;
	public static Field PacketPlayOutAnimation_ENTITY;
	public static Field PacketPlayOutAnimation_ACTION;
	
	public static Field GameProfile_properties;
	public static Field GameProfile_legacy;
    
	static{
		try {
			if (versionSupported) {
				(GameProfile_properties = GameProfile.class.getDeclaredField("properties")).setAccessible(true);
				(GameProfile_legacy = GameProfile.class.getDeclaredField("legacy")).setAccessible(true);
				if (versionNumber < 14) (PacketPlayOutBed_ENTITY = (PacketPlayOutBed = NMSClass.get("PacketPlayOutBed")).getDeclaredField("a")).setAccessible(true);
				(PacketPlayInChat_MESSAGE = (PacketPlayInChat = NMSClass.get("PacketPlayInChat")).getDeclaredField("a")).setAccessible(true);
				(PacketPlayInUseEntity_ENTITYID = (PacketPlayInUseEntity = NMSClass.get("PacketPlayInUseEntity")).getDeclaredField("a")).setAccessible(true);
				(PacketPlayOutAnimation_ENTITY = (PacketPlayOutAnimation = NMSClass.get("PacketPlayOutAnimation")).getDeclaredField("a")).setAccessible(true);
				(PacketPlayOutAnimation_ACTION = PacketPlayOutAnimation.getDeclaredField("b")).setAccessible(true);
			}
		} catch (Exception e) {
			Shared.error("Failed to initialize PacketAPI class", e);
		}
	}
	public static boolean isVersionSupported() {
		return versionSupported;
	}
}