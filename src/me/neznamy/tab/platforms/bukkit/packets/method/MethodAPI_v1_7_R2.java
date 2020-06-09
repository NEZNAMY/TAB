package me.neznamy.tab.platforms.bukkit.packets.method;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R2.CraftServer;
import org.bukkit.craftbukkit.v1_7_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_7_R2.ChatSerializer;
import net.minecraft.server.v1_7_R2.DataWatcher;
import net.minecraft.server.v1_7_R2.Entity;
import net.minecraft.server.v1_7_R2.EntityLiving;
import net.minecraft.server.v1_7_R2.EntityWither;
import net.minecraft.server.v1_7_R2.EnumChatFormat;
import net.minecraft.server.v1_7_R2.EnumGamemode;
import net.minecraft.server.v1_7_R2.IChatBaseComponent;
import net.minecraft.server.v1_7_R2.NBTTagCompound;
import net.minecraft.server.v1_7_R2.Packet;
import net.minecraft.server.v1_7_R2.PacketPlayInUseEntity;
import net.minecraft.server.v1_7_R2.PacketPlayOutChat;
import net.minecraft.server.v1_7_R2.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_7_R2.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_7_R2.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_7_R2.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_7_R2.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_7_R2.PacketPlayOutScoreboardDisplayObjective;
import net.minecraft.server.v1_7_R2.PacketPlayOutScoreboardObjective;
import net.minecraft.server.v1_7_R2.PacketPlayOutScoreboardScore;
import net.minecraft.server.v1_7_R2.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_7_R2.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_7_R2.WatchableObject;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class MethodAPI_v1_7_R2 extends MethodAPI {
	
	public MethodAPI_v1_7_R2() {
		DataWatcher = DataWatcher.class;
		Entity = Entity.class;
		EnumChatFormat = EnumChatFormat.class;
		EnumGamemode = EnumGamemode.class;
		IChatBaseComponent = IChatBaseComponent.class;
		PacketPlayInUseEntity = PacketPlayInUseEntity.class;
		PacketPlayOutPlayerInfo = PacketPlayOutPlayerInfo.class;
		PacketPlayOutScoreboardDisplayObjective = PacketPlayOutScoreboardDisplayObjective.class;
		PacketPlayOutScoreboardObjective = PacketPlayOutScoreboardObjective.class;
		PacketPlayOutScoreboardScore = PacketPlayOutScoreboardScore.class;
		PacketPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.class;
		PacketPlayOutEntityMetadata = PacketPlayOutEntityMetadata.class;
		PacketPlayOutSpawnEntityLiving = PacketPlayOutSpawnEntityLiving.class;
		PacketPlayOutNamedEntitySpawn = PacketPlayOutNamedEntitySpawn.class;
		PacketPlayOutEntityDestroy = PacketPlayOutEntityDestroy.class;
	}
	public Object ICBC_fromString(String string) {
		if (string == null) return null;
		return ChatSerializer.a(string);
	}
	public String ICBC_toString(Object component) {
		if (component == null) return null;
		return ChatSerializer.a((IChatBaseComponent) component);
	}
	public int getPing(Player p) {
		return ((CraftPlayer)p).getHandle().ping;
	}
	public Object getChannel(Player p) throws Exception {
		return null;
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
		return new PacketPlayOutChat((IChatBaseComponent) chatComponent);
	}
	public Object newPacketPlayOutEntityMetadata(int entityId, Object dataWatcher, boolean force) {
		return new PacketPlayOutEntityMetadata(entityId, (DataWatcher) dataWatcher, force);
	}
	public Object newPacketPlayOutSpawnEntityLiving() {
		return new PacketPlayOutSpawnEntityLiving();
	}
	public Object newPacketPlayOutPlayerInfo(Object action) {
		return new PacketPlayOutPlayerInfo();
	}
	public Object newPacketPlayOutBoss() {
		return null;
	}
	public Object newPacketPlayOutPlayerListHeaderFooter() {
		return null;
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
	public Object newPlayerInfoData(Object profile, int ping, Object enumGamemode, Object listName) {
		return null;
	}
	public Object newDataWatcherItem(me.neznamy.tab.platforms.bukkit.packets.DataWatcher.DataWatcherObject type, Object value, boolean needsUpdate) {
		WatchableObject item = new WatchableObject((int) type.classType, type.position, value);
		item.a(needsUpdate);
		return item;
	}
	public void DataWatcher_register(Object dataWatcher, me.neznamy.tab.platforms.bukkit.packets.DataWatcher.DataWatcherObject type, Object value) {
		((DataWatcher)dataWatcher).a(type.position, value);
	}
	public Object newEntityArmorStand() {
		return null;
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
	public Object newEntityWither() {
		return new EntityWither(((CraftWorld)Bukkit.getWorlds().get(0)).getHandle());
	}
	public Object newPacketPlayOutScoreboardScore() {
		return new PacketPlayOutScoreboardScore();
	}
	public Object newPacketPlayOutScoreboardScore_legacy(String removedPlayer) {
		return new PacketPlayOutScoreboardScore(removedPlayer);
	}
	public Object newPacketPlayOutScoreboardScore_1_13(Object action, String objectiveName, String player, int score) {
		return null;
	}
	public List getDataWatcherItems(Object dataWatcher) {
		return ((DataWatcher)dataWatcher).c();
	}
	public me.neznamy.tab.platforms.bukkit.packets.DataWatcher.Item readDataWatcherItem(Object nmsItem) {
		WatchableObject i = (WatchableObject) nmsItem;
		int position = i.a();
		Object classType = i.c();
		Object value = i.b();
		boolean needsUpdate = i.d();
		me.neznamy.tab.platforms.bukkit.packets.DataWatcher.DataWatcherObject key = new me.neznamy.tab.platforms.bukkit.packets.DataWatcher.DataWatcherObject(position, classType);
		return new me.neznamy.tab.platforms.bukkit.packets.DataWatcher.Item(key, value).setNeedsUpdate(needsUpdate);
	}
	public String serialize(ItemStack item) {
		return CraftItemStack.asNMSCopy(item).save(new NBTTagCompound()).toString();
	}
}