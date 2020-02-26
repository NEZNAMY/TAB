package me.neznamy.tab.platforms.bukkit.packets.method;

import java.lang.reflect.Field;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.neznamy.tab.platforms.bukkit.packets.DataWatcher.Item;
import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOut;
import me.neznamy.tab.platforms.bukkit.packets.DataWatcher.DataWatcherObject;

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
	public static Class<?> PacketPlayOutAttachEntity;
	public static Class<?> PacketPlayOutMount;
	public static Class<?> PacketPlayOutNamedEntitySpawn;
	public static Class<?> PacketPlayOutEntityDestroy;
	public static Class<?> PacketPlayOutEntityTeleport;
	public static Class<?> PacketPlayOutRelEntityMove;
	public static Class<?> PacketPlayOutRelEntityMoveLook;
	public static Class<?> PacketPlayOutEntity;
	public static Class<?> PlayerInfoData;
	
	public static Field PacketPlayInUseEntity_ENTITY;
	public static Field PacketPlayOutEntityMetadata_LIST;
	
	public static MethodAPI getInstance() {
		return instance;
	}
	public abstract Object ICBC_fromString(String string);
	public abstract String CCM_fromComponent(Object ichatbasecomponent);
	public abstract int getPing(Player p);
	public abstract Object getChannel(Player p) throws Exception;
	public abstract double[] getRecentTps();
	public abstract void sendPacket(Player p, Object nmsPacket);
	public abstract Object newPacketPlayOutEntityDestroy(int... ids);
	public abstract Object newPacketPlayOutChat(Object chatComponent, Object position);
	public abstract Object newPacketPlayOutEntityMetadata(int entityId, Object dataWatcher, boolean force);
	public abstract Object newPacketPlayOutEntityTeleport();
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
	public abstract void DataWatcher_register(Object dataWatcher, DataWatcherObject type, Object value);
	public abstract Object newEntityArmorStand();
	public abstract Object newEntityWither();
	public abstract int getEntityId(Object entityliving);
	public abstract Object newPacketPlayOutEntityTeleport(Object entityliving, Location loc);
	public abstract Object newPacketPlayOutEntityTeleport(Player p);
	public abstract Object newPacketPlayOutScoreboardScore();
	public abstract Object newPacketPlayOutScoreboardScore_legacy(String removedPlayer);
	public abstract Object newPacketPlayOutScoreboardScore_1_13(Object action, String objectiveName, String player, int score);
	public abstract List<Object> getDataWatcherItems(Object dataWatcher);
	public abstract Item readDataWatcherItem(Object nmsItem);
	
	public double getTPS() {
		try {
			return getRecentTps()[0];
		} catch (Throwable t) {
			return -1;
		}
	}

	static {
		try {
			instance = (MethodAPI) Class.forName(MethodAPI.class.getPackage().getName()+".MethodAPI_" + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]).getConstructor().newInstance();
			PacketPlayInUseEntity_ENTITY = PacketPlayOut.getFields(PacketPlayInUseEntity).get("a");
			PacketPlayOutEntityMetadata_LIST = PacketPlayOut.getFields(PacketPlayOutEntityMetadata).get("b");
		} catch (Throwable e) {
			//bungee or velocity
		}
	}
}