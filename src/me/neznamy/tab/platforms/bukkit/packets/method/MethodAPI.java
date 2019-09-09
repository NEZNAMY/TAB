package me.neznamy.tab.platforms.bukkit.packets.method;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;

import io.netty.channel.Channel;
import me.neznamy.tab.platforms.bukkit.packets.DataWatcherObject;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;

public abstract class MethodAPI {

	private static MethodAPI instance;
	private static boolean spigot;
	
	public static MethodAPI getInstance() {
		return instance;
	}
	public abstract GameProfile getProfile(Player p);
	public abstract Object ICBC_fromString(String string);
	public abstract String CCM_fromComponent(Object ichatbasecomponent);
	public abstract int getPing(Player p);
	public abstract Channel getChannel(Player p) throws Exception;
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
	public abstract Object newPlayerInfoData(Object packetPlayOutPlayerInfo, GameProfile profile, int ping, Object enumGamemode, Object listName);
	public abstract Object newDataWatcherItem(DataWatcherObject type, Object value, boolean needsUpdate);
	public abstract void DataWatcher_register(Object dataWatcher, DataWatcherObject type, Object value);
	public abstract Object newEntityArmorStand();
	public abstract Object newEntityWither();
	public abstract int getEntityId(Object entityliving);
	public abstract Object newPacketPlayOutEntityTeleport(Object entityliving, Location loc);
	public abstract Object newPacketPlayOutEntityTeleport(Player p);
	
	public double getTPS() {
		if (!spigot) return -1;
		return getRecentTps()[0];
	}

	static {
		try {
			instance = (MethodAPI) Class.forName(MethodAPI.class.getPackage().getName()+".MethodAPI_" + ProtocolVersion.packageName).getConstructor().newInstance();
			try {
				Class.forName("org.spigotmc.SpigotConfig");
				spigot = true;
			} catch (Throwable e) {
				spigot = false;
			}
		} catch (Throwable e) {
			Shared.error("Failed to initialize MethodAPI class", e);
		}
	}
}