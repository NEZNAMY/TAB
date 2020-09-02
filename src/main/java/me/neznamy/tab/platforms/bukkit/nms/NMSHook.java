package me.neznamy.tab.platforms.bukkit.nms;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;

import io.netty.channel.Channel;
import me.neznamy.tab.platforms.bukkit.features.PetFix;
import me.neznamy.tab.platforms.bukkit.features.unlimitedtags.PacketListener;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherItem;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherRegistry;
import me.neznamy.tab.shared.packets.PacketPlayOutBoss;
import me.neznamy.tab.shared.packets.PacketPlayOutChat;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerListHeaderFooter;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardDisplayObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardObjective;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardScore;
import me.neznamy.tab.shared.packets.PacketPlayOutScoreboardTeam;

/**
 * The core class for NMS hooks and compatibility check
 */
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
			"v1_16_R1", "v1_16_R2"
		);
	
	public static  Class<?> EnumChatFormat;
	private static Class<?> ChatSerializer;
	public static Class<?> IChatBaseComponent;
	private static Field PING;
	private static Field PLAYER_CONNECTION;
	private static Field NETWORK_MANAGER;
	private static Field CHANNEL;
	private static Method getHandle;
	private static Method sendPacket;
	private static Method SERIALIZE;
	private static Method DESERIALIZE;
	
	public static Object stringToComponent(String json) throws Exception {
		if (json == null) return null;
		return DESERIALIZE.invoke(null, json);
	}
	public static String componentToString(Object component) throws Exception {
		if (component == null) return null;
		return (String) SERIALIZE.invoke(null, component);
	}
	
	public static Object getChannel(Player p) throws Exception {
		if (CHANNEL == null) return null;
		return CHANNEL.get(NETWORK_MANAGER.get(PLAYER_CONNECTION.get(getHandle.invoke(p))));
	}

	public static void sendPacket(Player p, Object nmsPacket) throws Exception {
		sendPacket.invoke(PLAYER_CONNECTION.get(getHandle.invoke(p)), nmsPacket);
	}
	
	public static int getPing(Player p) throws Exception {
		return PING.getInt(getHandle.invoke(p));
	}

	public static boolean isVersionSupported(String serverPackage){
		try {
			try {
				IChatBaseComponent = PacketPlayOut.getNMSClass("IChatBaseComponent");
			} catch (ClassNotFoundException e) {
				IChatBaseComponent = PacketPlayOut.getNMSClass("ChatMessage");
			}
			DataWatcher.initializeClass();
			DataWatcherItem.initializeClass();
			DataWatcherRegistry.initializeClass();
			PacketPlayOutAnimation.initializeClass();
			PacketPlayOutChat.initializeClass();
			PacketPlayOutEntityDestroy.initializeClass();
			PacketPlayOutEntityMetadata.initializeClass();
			PacketPlayOutEntityTeleport.initializeClass();
			PacketPlayOutScoreboardDisplayObjective.initializeClass();
			PacketPlayOutScoreboardObjective.initializeClass();
			PacketPlayOutScoreboardScore.initializeClass();
			PacketPlayOutScoreboardTeam.initializeClass();
			PacketPlayOutSpawnEntityLiving.initializeClass();
			EnumChatFormat = PacketPlayOut.getNMSClass("EnumChatFormat");
			PING = PacketPlayOut.getNMSClass("EntityPlayer").getDeclaredField("ping");
			PLAYER_CONNECTION = PacketPlayOut.getNMSClass("EntityPlayer").getDeclaredField("playerConnection");
			NETWORK_MANAGER = PLAYER_CONNECTION.getType().getField("networkManager");
			getHandle = Class.forName("org.bukkit.craftbukkit." + serverPackage + ".entity.CraftPlayer").getMethod("getHandle");
			sendPacket = PacketPlayOut.getNMSClass("PlayerConnection").getMethod("sendPacket", PacketPlayOut.getNMSClass("Packet"));
			int minor = Integer.parseInt(serverPackage.split("_")[1]);
			if (minor >= 7) {
				PacketPlayOutPlayerInfo.initializeClass();
				try {
					//v1_8_R2+
					ChatSerializer = PacketPlayOut.getNMSClass("IChatBaseComponent$ChatSerializer");
				} catch (ClassNotFoundException e) {
					//v1_8_R1-
					ChatSerializer = PacketPlayOut.getNMSClass("ChatSerializer");
				}
				SERIALIZE = ChatSerializer.getMethod("a", IChatBaseComponent);
				DESERIALIZE = ChatSerializer.getMethod("a", String.class);
			}
			if (minor >= 8) {
				PacketListener.initializeClass();
				PacketPlayOutPlayerListHeaderFooter.initializeClass();
				CHANNEL = PacketPlayOut.getFields(PacketPlayOut.getNMSClass("NetworkManager"), Channel.class).get(0);
			}
			if (minor >= 9) {
				PacketPlayOutBoss.initializeClass();
				PetFix.initializeClass();
			}
			return SUPPORTED_VERSIONS.contains(serverPackage);
		} catch (Throwable e) {
//			e.printStackTrace();
			return false;
		}
	}
}