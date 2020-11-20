package me.neznamy.tab.platforms.bukkit.nms;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;

import io.netty.channel.Channel;
import me.neznamy.tab.platforms.bukkit.BukkitPacketBuilder;
import me.neznamy.tab.platforms.bukkit.features.PetFix;
import me.neznamy.tab.platforms.bukkit.features.unlimitedtags.PacketListener;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcher;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherItem;
import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherRegistry;

/**
 * The core class for NMS hooks and compatibility check
 */
public class NMSHook {
	
	//list of officially supported server versions
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
			"v1_16_R1", "v1_16_R2", "v1_16_R3"
		);
	
	//used NMS classes, fields and methods
	public static Class<?> IChatBaseComponent;
	private static Field PING;
	private static Field PLAYER_CONNECTION;
	private static Field NETWORK_MANAGER;
	private static Field CHANNEL;
	private static Method getHandle;
	private static Method sendPacket;
	private static Method SERIALIZE;
	private static Method DESERIALIZE;
	
	/**
	 * Converts json string into a component
	 * @param json json as string
	 * @return NMS component
	 * @throws Exception if something fails
	 */
	public static Object stringToComponent(String json) throws Exception {
		if (json == null) return null;
		return DESERIALIZE.invoke(null, json);
	}
	
	/**
	 * Converts NMS component into a string
	 * @param component component to convert
	 * @return json in string format
	 * @throws Exception if something fails
	 */
	public static String componentToString(Object component) throws Exception {
		if (component == null) return null;
		return (String) SERIALIZE.invoke(null, component);
	}
	
	/**
	 * Returns netty channel of player
	 * @param p player to get channel of
	 * @return the channel
	 * @throws Exception if something fails
	 */
	public static Object getChannel(Player p) throws Exception {
		if (CHANNEL == null) return null;
		return CHANNEL.get(NETWORK_MANAGER.get(PLAYER_CONNECTION.get(getHandle.invoke(p))));
	}

	/**
	 * Sends a packet to player
	 * @param p player to send packet to
	 * @param nmsPacket packet to send
	 * @throws Exception if something fails
	 */
	public static void sendPacket(Player p, Object nmsPacket) throws Exception {
		sendPacket.invoke(PLAYER_CONNECTION.get(getHandle.invoke(p)), nmsPacket);
	}
	
	/**
	 * Returns ping of player
	 * @param p player to get ping of
	 * @return ping
	 * @throws Exception if something fails
	 */
	public static int getPing(Player p) throws Exception {
		return PING.getInt(getHandle.invoke(p));
	}

	/**
	 * Initializes all used NMS classes, constructors, fields and methods and returns null if everything went successfully and version is marked as compatible
	 * @param serverPackage NMS class package, such as "v1_16_R1"
	 * @return null if compatible, non-null error message if not
	 */
	public static String checkCompatibility(String serverPackage){
		try {
			int minor = Integer.parseInt(serverPackage.split("_")[1]);
			if (minor >= 7) {
				IChatBaseComponent = PacketPlayOut.getNMSClass("IChatBaseComponent");
			} else if (minor == 6) {
				IChatBaseComponent = PacketPlayOut.getNMSClass("ChatMessage");
			}
			BukkitPacketBuilder.initializeClass();
			DataWatcher.initializeClass();
			DataWatcherItem.initializeClass();
			DataWatcherRegistry.initializeClass();
			PacketPlayOutAnimation.initializeClass();
			PacketPlayOutEntityDestroy.initializeClass();
			PacketPlayOutEntityMetadata.initializeClass();
			PacketPlayOutEntityTeleport.initializeClass();
			PacketPlayOutSpawnEntityLiving.initializeClass();
			PING = PacketPlayOut.getNMSClass("EntityPlayer").getDeclaredField("ping");
			PLAYER_CONNECTION = PacketPlayOut.getNMSClass("EntityPlayer").getDeclaredField("playerConnection");
			NETWORK_MANAGER = PLAYER_CONNECTION.getType().getField("networkManager");
			getHandle = Class.forName("org.bukkit.craftbukkit." + serverPackage + ".entity.CraftPlayer").getMethod("getHandle");
			sendPacket = PacketPlayOut.getNMSClass("PlayerConnection").getMethod("sendPacket", PacketPlayOut.getNMSClass("Packet"));
			
			if (minor >= 7) {
				Class<?> ChatSerializer;
				try {
					//v1_8_R2+
					ChatSerializer = PacketPlayOut.getNMSClass("IChatBaseComponent$ChatSerializer");
				} catch (ClassNotFoundException e) {
					//v1_8_R1-
					ChatSerializer = PacketPlayOut.getNMSClass("ChatSerializer");
				}
				SERIALIZE = ChatSerializer.getMethod("a", IChatBaseComponent);
				DESERIALIZE = ChatSerializer.getMethod("a", String.class);
			} else if (minor == 6) {
				DESERIALIZE = IChatBaseComponent.getMethod("d", String.class);
			}
			if (minor >= 8) {
				PacketListener.initializeClass();
				CHANNEL = PacketPlayOut.getFields(PacketPlayOut.getNMSClass("NetworkManager"), Channel.class).get(0);
			}
			if (minor >= 9) {
				PetFix.initializeClass();
			}
			if (SUPPORTED_VERSIONS.contains(serverPackage)) {
				return null;
			} else {
				return "Your server version is not marked as compatible. Disabling..";
			}
		} catch (Throwable e) {
			if (SUPPORTED_VERSIONS.contains(serverPackage)) {
				String msg = "Your server version is marked as compatible, but a compatibility issue was found. Please report the error above (include your server version & fork too)";
				e.printStackTrace();
				return msg;
			} else {
				return "Your server version is completely unsupported. Disabling..";
			}
		}
	}
}