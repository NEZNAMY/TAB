package me.neznamy.tab.platforms.bukkit.packets.method;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R1.CraftServer;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;
import com.mojang.authlib.GameProfile;

import io.netty.channel.Channel;
import me.neznamy.tab.platforms.bukkit.packets.DataWatcherObject;
import me.neznamy.tab.shared.Shared;
import net.minecraft.server.v1_8_R1.*;

public class MethodAPI_v1_8_R1 extends MethodAPI {

	public GameProfile getProfile(Player p) {
		return ((CraftPlayer)p).getHandle().getProfile();
	}
	public Object ICBC_fromString(String string) {
		return ChatSerializer.a(string);
	}
	public String CCM_fromComponent(Object ichatbasecomponent) {
		return CraftChatMessage.fromComponent((IChatBaseComponent) ichatbasecomponent);
	}
	public int getPing(Player p) {
		return ((CraftPlayer)p).getHandle().ping;
	}
	public Channel getChannel(Player p) throws Exception {
		return (Channel) NetworkManager_CHANNEL.get(((CraftPlayer)p).getHandle().playerConnection.networkManager);
	}
	public double[] getRecentTps() {
		return ((CraftServer)Bukkit.getServer()).getServer().recentTps;
	}
	public void sendPacket(Player p, Object nmsPacket) {
		((CraftPlayer)p).getHandle().playerConnection.sendPacket((Packet) nmsPacket);
	}
	public Object newPacketPlayOutEntityDestroy(int... ids) {
		return new PacketPlayOutEntityDestroy(ids);
	}
	public Object newPacketPlayOutChat(Object chatComponent, Object position) {
		return new PacketPlayOutChat((IChatBaseComponent) chatComponent, (Byte) position);
	}
	public Object newPacketPlayOutEntityMetadata(int entityId, Object dataWatcher, boolean force) {
		return new PacketPlayOutEntityMetadata(entityId, (DataWatcher) dataWatcher, force);
	}
	public Object newPacketPlayOutEntityTeleport() {
		return new PacketPlayOutEntityTeleport();
	}
	public Object newPacketPlayOutSpawnEntityLiving() {
		return new PacketPlayOutSpawnEntityLiving();
	}
	public Object newPacketPlayOutPlayerInfo(Object action) {
		return new PacketPlayOutPlayerInfo((EnumPlayerInfoAction)action);
	}
	public Object newPacketPlayOutBoss() {
		return null;
	}
	public Object newPacketPlayOutPlayerListHeaderFooter() {
		return new PacketPlayOutPlayerListHeaderFooter();
	}
	public Object newPacketPlayOutScoreboardDisplayObjective() {
		return new PacketPlayOutScoreboardDisplayObjective();
	}
	public Object newPacketPlayOutScoreboardObjective() {
		return new PacketPlayOutScoreboardObjective();
	}
	public Object newPacketPlayOutScoreboardTeam() {
		return new PacketPlayOutScoreboardTeam();
	}
	public Object newDataWatcher(Object entity) {
		return new DataWatcher((Entity) entity);
	}
	public Object newPlayerInfoData(Object packetPlayOutPlayerInfo, GameProfile profile, int ping, Object enumGamemode, Object listName) {
		return new PlayerInfoData((PacketPlayOutPlayerInfo) packetPlayOutPlayerInfo, profile, ping, (EnumGamemode)enumGamemode, (IChatBaseComponent) listName);
	}
	public Object newDataWatcherItem(DataWatcherObject type, Object value, boolean needsUpdate) {
		WatchableObject item = new WatchableObject((int) type.getClassType(), type.getPosition(), value);
		item.a(needsUpdate);
		return item;
	}
	public void DataWatcher_register(Object dataWatcher, me.neznamy.tab.platforms.bukkit.packets.DataWatcherObject type, Object value) {
		((DataWatcher)dataWatcher).a(type.getPosition(), value);
	}
	public Object newEntityArmorStand() {
		return new EntityArmorStand(((CraftWorld)Bukkit.getWorlds().get(0)).getHandle());
	}
	public int getEntityId(Object entityliving) {
		return ((EntityLiving)entityliving).getId();
	}
	public Object newPacketPlayOutEntityTeleport(Object entityliving, Location loc) {
		EntityLiving entity = (EntityLiving) entityliving;
		entity.locX = loc.getX();
		entity.locY = loc.getY();
		entity.locZ = loc.getZ();
		entity.yaw = loc.getYaw();
		entity.pitch = loc.getPitch();
		return new PacketPlayOutEntityTeleport(entity);
	}
	public Object newPacketPlayOutEntityTeleport(Player p) {
		return new PacketPlayOutEntityTeleport(((CraftPlayer)p).getHandle());
	}
	public Object newEntityWither() {
		return new EntityWither(((CraftWorld)Bukkit.getWorlds().get(0)).getHandle());
	}
	
	private static Field NetworkManager_CHANNEL;
	
	static {
		try {
			(NetworkManager_CHANNEL = NetworkManager.class.getDeclaredField("i")).setAccessible(true);
		} catch (Throwable e) {
			Shared.error("Failed to initialize MethodAPI_v1_8_R1 class", e);
		}
	}
}