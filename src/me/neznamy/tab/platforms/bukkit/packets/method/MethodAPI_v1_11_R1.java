package me.neznamy.tab.platforms.bukkit.packets.method;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_11_R1.CraftServer;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_11_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;

import io.netty.channel.Channel;
import net.minecraft.server.v1_11_R1.*;
import net.minecraft.server.v1_11_R1.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class MethodAPI_v1_11_R1 extends MethodAPI {

	public GameProfile getProfile(Player p) {
		return ((CraftPlayer)p).getHandle().getProfile();
	}
	public Object ICBC_fromString(String string) {
		return IChatBaseComponent.ChatSerializer.a(string);
	}
	public String CCM_fromComponent(Object ichatbasecomponent) {
		return CraftChatMessage.fromComponent((IChatBaseComponent) ichatbasecomponent);
	}
	public int getPing(Player p) {
		return ((CraftPlayer)p).getHandle().ping;
	}
	public Channel getChannel(Player p) {
		return ((CraftPlayer)p).getHandle().playerConnection.networkManager.channel;
	}
	public double[] getRecentTps() {
		return ((CraftServer)Bukkit.getServer()).getServer().recentTps;
	}
	public void sendPacket(Player p, Object nmsPacket) {
		((CraftPlayer)p).getHandle().playerConnection.sendPacket((Packet<?>) nmsPacket);
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
		return new PacketPlayOutBoss();
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
		return ((PacketPlayOutPlayerInfo)packetPlayOutPlayerInfo).new PlayerInfoData(profile, ping, (EnumGamemode)enumGamemode, (IChatBaseComponent) listName);
	}
	public Object newDataWatcherItem(me.neznamy.tab.platforms.bukkit.packets.DataWatcherObject type, Object value, boolean needsUpdate) {
		DataWatcher.Item item = new DataWatcher.Item(new DataWatcherObject(type.getPosition(), (DataWatcherSerializer) type.getClassType()), value);
		item.a(needsUpdate);
		return item;
	}
	public void DataWatcher_register(Object dataWatcher, me.neznamy.tab.platforms.bukkit.packets.DataWatcherObject type, Object value) {
		((DataWatcher)dataWatcher).register(new DataWatcherObject(type.getPosition(), (DataWatcherSerializer) type.getClassType()), value);
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
}