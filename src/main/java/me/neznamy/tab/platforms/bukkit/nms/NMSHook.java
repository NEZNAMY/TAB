package me.neznamy.tab.platforms.bukkit.nms;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.neznamy.tab.platforms.bukkit.nms.datawatcher.DataWatcherRegistry;

/**
 * The core class for NMS hooks and compatibility check
 */
public class NMSHook {
	
	//list of officially supported server versions
	private final static List<String> SUPPORTED_VERSIONS = Arrays.asList(
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
	
	public static DataWatcherRegistry registry;
	public static NMSStorage nms;
	
	/**
	 * Returns netty channel of player
	 * @param p player to get channel of
	 * @return the channel
	 * @throws Exception if something fails
	 */
	public static Object getChannel(Player p) throws Exception {
		if (nms.CHANNEL == null) return null;
		return nms.CHANNEL.get(nms.NETWORK_MANAGER.get(nms.PLAYER_CONNECTION.get(nms.getHandle.invoke(p))));
	}

	/**
	 * Sends a packet to player
	 * @param p player to send packet to
	 * @param nmsPacket packet to send
	 * @throws Exception if something fails
	 */
	public static void sendPacket(Player p, Object nmsPacket) throws Exception {
		nms.sendPacket.invoke(nms.PLAYER_CONNECTION.get(nms.getHandle.invoke(p)), nmsPacket);
	}
	
	/**
	 * Returns ping of player
	 * @param p player to get ping of
	 * @return ping
	 * @throws Exception if something fails
	 */
	public static int getPing(Player p) throws Exception {
		return nms.PING.getInt(nms.getHandle.invoke(p));
	}

	/**
	 * Initializes all used NMS classes, constructors, fields and methods and returns true if everything went successfully and version is marked as compatible
	 * @return true if compatible, false if not
	 */
	public static boolean isVersionSupported(){
		String serverPackage = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		try {
			nms = new NMSStorage();
			registry = new DataWatcherRegistry(nms);
			if (SUPPORTED_VERSIONS.contains(serverPackage)) {
				return true;
			} else {
				Bukkit.getConsoleSender().sendMessage("\u00a7c[TAB] This plugin version does not claim to support your server version. This jar has only been tested on 1.7.x - 1.16.4. Disabling.");
			}
		} catch (Throwable e) {
			if (SUPPORTED_VERSIONS.contains(serverPackage)) {
				Bukkit.getConsoleSender().sendMessage("\u00a7c[TAB] Your server version is marked as compatible, but a compatibility issue was found. Please report the error below (include your server version & fork too)");
				e.printStackTrace();
			} else {
				Bukkit.getConsoleSender().sendMessage("\u00a7c[TAB] Your server version is completely unsupported. This plugin version only supports 1.7.x - 1.16.4. Disabling.");
			}
		}
		return false;
	}
	
	public Class<?> getNMSClass(String name) throws ClassNotFoundException {
		return Class.forName("net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3] + "." + name);
	}
}