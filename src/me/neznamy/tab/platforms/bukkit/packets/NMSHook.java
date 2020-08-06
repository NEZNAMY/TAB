package me.neznamy.tab.platforms.bukkit.packets;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;

import io.netty.channel.Channel;

public class NMSHook {
	
	private static final List<String> SUPPORTED_VERSIONS = Arrays.asList(
			"v1_5_R1", "v1_5_R2", "v1_5_R3",
			"v1_6_R1", "v1_6_R2", "v1_6_R3",
			"v1_7_R1", "v1_7_R2", "v1_7_R3", "v1_7_R4",
			"v1_8_R1", "v1_8_R2", "v1_8_R3",
			"v1_9_R1", "v1_9_R2",
			"v1_10_R1",
			"v1_11_R1",
			"v1_12_R1",
			"v1_13_R1", "v1_13_R2",
			"v1_14_R1",
			"v1_15_R1",
			"v1_16_R1"
		);
	
	public static final Class<?> EnumChatFormat = PacketPlayOut.getNMSClass("EnumChatFormat");
	
	private static Class<?> ChatSerializer = PacketPlayOut.getNMSClass("IChatBaseComponent$ChatSerializer", "ChatSerializer");
	private static Field PING = PacketPlayOut.getField(PacketPlayOut.getNMSClass("EntityPlayer"), "ping");
	private static Field PLAYER_CONNECTION = PacketPlayOut.getField(PacketPlayOut.getNMSClass("EntityPlayer"), "playerConnection");
	private static Field CHANNEL;
	private static Method sendPacket;
	private static Method SERIALIZE;
	private static Method DESERIALIZE;
	
	public static Object stringToComponent(String json) {
		if (json == null) return null;
		try {
			return DESERIALIZE.invoke(null, json);
		} catch (Exception e) {
			return null;
		}
	}
	public static String componentToString(Object component) {
		if (component == null) return null;
		try {
			return (String) SERIALIZE.invoke(null, component);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Object getChannel(Player p){
		try {
			Object connection = PLAYER_CONNECTION.get(p.getClass().getMethod("getHandle").invoke(p));
			Object networkManager = connection.getClass().getDeclaredField("networkManager").get(connection);
			return CHANNEL.get(networkManager);
		} catch (Exception e) {
			return null;
		}
	}

	public static void sendPacket(Player p, Object nmsPacket) throws Exception{
		sendPacket.invoke(PLAYER_CONNECTION.get(p.getClass().getMethod("getHandle").invoke(p)), nmsPacket);
	}
	
	public static int getPing(Player p) throws Exception{
		return PING.getInt(p.getClass().getMethod("getHandle").invoke(p));
	}

	public static boolean isVersionSupported(String serverPackage){
		try {
			sendPacket = PacketPlayOut.getNMSClass("PlayerConnection").getMethod("sendPacket", PacketPlayOut.getNMSClass("Packet"));
			try {
				//1.7+
				SERIALIZE = ChatSerializer.getMethod("a", PacketPlayOut.getNMSClass("IChatBaseComponent"));
				DESERIALIZE = ChatSerializer.getMethod("a", String.class);
				
				//1.8+
				CHANNEL = PacketPlayOut.getFields(PacketPlayOut.getNMSClass("NetworkManager"), Channel.class).get(0);
			} catch (Throwable t) {
				
			}
			return SUPPORTED_VERSIONS.contains(serverPackage);
		} catch (Throwable e) {
			return false;
		}
	}
}