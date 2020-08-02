package me.neznamy.tab.platforms.bukkit.packets.method;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.neznamy.tab.platforms.bukkit.Main;
import me.neznamy.tab.platforms.bukkit.packets.DataWatcher.DataWatcherObject;
import me.neznamy.tab.platforms.bukkit.packets.DataWatcher.Item;

public abstract class MethodAPI {

	private static MethodAPI instance;
	
	public static Class<?> BarColor;
	public static Class<?> BarStyle;
	public static Class<?> ChatMessageType;
	public static Class<?> DataWatcher;
	public static Class<?> DataWatcherRegistry;
	public static Class<?> Entity;
	public static Class<?> EnumChatFormat;
	public static Class<?> EnumGamemode;
	public static Class<?> EnumPlayerInfoAction;
	public static Class<?> EnumScoreboardAction;
	public static Class<?> EnumScoreboardHealthDisplay;
	public static Class<?> IChatBaseComponent;
	public static Class<?> PacketPlayInUseEntity;
	public static Class<?> PacketPlayOutPlayerInfo;
	public static Class<?> PacketPlayOutBoss;
	public static Class<?> PacketPlayOutPlayerListHeaderFooter;
	public static Class<?> PacketPlayOutScoreboardDisplayObjective;
	public static Class<?> PacketPlayOutScoreboardObjective;
	public static Class<?> PacketPlayOutScoreboardScore;
	public static Class<?> PacketPlayOutScoreboardTeam;
	public static Class<?> PacketPlayOutEntityMetadata;
	public static Class<?> PacketPlayOutSpawnEntityLiving;
	public static Class<?> PacketPlayOutBoss_Action;
	public static Class<?> PacketPlayOutNamedEntitySpawn;
	public static Class<?> PacketPlayOutEntityDestroy;
	public static Class<?> PacketPlayOutEntityTeleport;
	public static Class<?> PacketPlayOutEntity;
	public static Class<?> PacketPlayOutMount;
	public static Class<?> PacketPlayOutAttachEntity;
	public static Class<?> PlayerInfoData;
	
	public static MethodAPI getInstance() {
		return instance;
	}
	public abstract Object stringToComponent(String string);
	public abstract String componentToString(Object component);
	public abstract int getPing(Player p);
	public abstract Object getChannel(Player p) throws Exception;
	public abstract double[] getRecentTps();
	public abstract void sendPacket(Player p, Object nmsPacket);
	public abstract Object newPacketPlayOutEntityDestroy(int... ids);
	public abstract Object newPacketPlayOutChat(Object chatComponent, Object position);
	public abstract Object newPacketPlayOutEntityMetadata(int entityId, Object dataWatcher, boolean force);
	public abstract Object newPacketPlayOutSpawnEntityLiving();
	public abstract Object newPacketPlayOutPlayerInfo(Object action);
	public abstract Object newPacketPlayOutBoss();
	public abstract Object newPacketPlayOutPlayerListHeaderFooter();
	public abstract Object newPacketPlayOutScoreboardDisplayObjective();
	public abstract Object newPacketPlayOutScoreboardObjective();
	public abstract Object newPacketPlayOutScoreboardTeam();
	public abstract Object newDataWatcher(Object entity);
	public abstract Object newPlayerInfoData(Object profile, int ping, Object enumGamemode, Object listName);
	public abstract Object newDataWatcherItem(DataWatcherObject type, Object value, boolean needsUpdate);
	public abstract void registerDataWatcherObject(Object dataWatcher, DataWatcherObject type, Object value);
	public abstract Object newEntityArmorStand();
	public abstract Object newEntityWither();
	public abstract int getEntityId(Object entityliving);
	public abstract Object newPacketPlayOutEntityTeleport(Object entityliving, Location loc);
	public abstract Object newPacketPlayOutScoreboardScore();
	public abstract Object newPacketPlayOutScoreboardScore(String removedPlayer); //<1.13
	public abstract Object newPacketPlayOutScoreboardScore(Object action, String objectiveName, String player, int score); //1.13+
	public abstract List<Object> getDataWatcherItems(Object dataWatcher);
	public abstract Item readDataWatcherItem(Object nmsItem);
	public abstract String serialize(ItemStack item);
	
	public double getTPS() {
		try {
			return getRecentTps()[0];
		} catch (Throwable t) {
			return -1;
		}
	}

	static {
		try {
			instance = (MethodAPI) Class.forName(MethodAPI.class.getPackage().getName()+".MethodAPI_" + Main.serverPackage).getConstructor().newInstance();
		} catch (Throwable e) {
			//bungee or velocity
		}
	}
}