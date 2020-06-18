package me.neznamy.tab.platforms.bukkit.packets.method;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_6_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_6_R3.ChatMessage;
import net.minecraft.server.v1_6_R3.DataWatcher;
import net.minecraft.server.v1_6_R3.Entity;
import net.minecraft.server.v1_6_R3.EntityLiving;
import net.minecraft.server.v1_6_R3.EntityWither;
import net.minecraft.server.v1_6_R3.EnumChatFormat;
import net.minecraft.server.v1_6_R3.EnumGamemode;
import net.minecraft.server.v1_6_R3.NBTTagCompound;
import net.minecraft.server.v1_6_R3.Packet;
import net.minecraft.server.v1_6_R3.Packet201PlayerInfo;
import net.minecraft.server.v1_6_R3.Packet206SetScoreboardObjective;
import net.minecraft.server.v1_6_R3.Packet207SetScoreboardScore;
import net.minecraft.server.v1_6_R3.Packet208SetScoreboardDisplayObjective;
import net.minecraft.server.v1_6_R3.Packet209SetScoreboardTeam;
import net.minecraft.server.v1_6_R3.Packet20NamedEntitySpawn;
import net.minecraft.server.v1_6_R3.Packet24MobSpawn;
import net.minecraft.server.v1_6_R3.Packet29DestroyEntity;
import net.minecraft.server.v1_6_R3.Packet34EntityTeleport;
import net.minecraft.server.v1_6_R3.Packet3Chat;
import net.minecraft.server.v1_6_R3.Packet40EntityMetadata;
import net.minecraft.server.v1_6_R3.WatchableObject;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class MethodAPI_v1_6_R3 extends MethodAPI {

	public MethodAPI_v1_6_R3() {
		DataWatcher = DataWatcher.class;
		Entity = Entity.class;
		EnumChatFormat = EnumChatFormat.class;
		EnumGamemode = EnumGamemode.class;
		IChatBaseComponent = ChatMessage.class;
		PacketPlayOutScoreboardDisplayObjective = Packet208SetScoreboardDisplayObjective.class;
		PacketPlayOutScoreboardObjective = Packet206SetScoreboardObjective.class;
		PacketPlayOutScoreboardScore = Packet207SetScoreboardScore.class;
		PacketPlayOutScoreboardTeam = Packet209SetScoreboardTeam.class;
		PacketPlayOutEntityMetadata = Packet40EntityMetadata.class;
		PacketPlayOutSpawnEntityLiving = Packet24MobSpawn.class;
		PacketPlayOutNamedEntitySpawn = Packet20NamedEntitySpawn.class;
		PacketPlayOutEntityDestroy = Packet29DestroyEntity.class;
	}
	public Object stringToComponent(String string) {
		throw new UnsupportedOperationException("Not supported");
	}
	public String componentToString(Object component) {
		if (component == null) return null;
		return ((ChatMessage) component).i();
	}
	public int getPing(Player p) {
		return ((CraftPlayer)p).getHandle().ping;
	}
	public Object getChannel(Player p) throws Exception {
		return null;
	}
	public double[] getRecentTps() {
		return new double[]{-1};
	}
	public void sendPacket(Player p, Object nmsPacket) {
		((CraftPlayer)p).getHandle().playerConnection.sendPacket((Packet) nmsPacket);
	}
	public Object newPacketPlayOutEntityDestroy(int... ids) {
		return new Packet29DestroyEntity(ids);
	}
	public Object newPacketPlayOutChat(Object message, Object position) {
		return new Packet3Chat((String) message);
	}
	public Object newPacketPlayOutEntityMetadata(int entityId, Object dataWatcher, boolean force) {
		return new Packet40EntityMetadata(entityId, (DataWatcher) dataWatcher, force);
	}
	public Object newPacketPlayOutSpawnEntityLiving() {
		return new Packet24MobSpawn();
	}
	public Object newPacketPlayOutPlayerInfo(Object action) {
		return new Packet201PlayerInfo();
	}
	public Object newPacketPlayOutBoss() {
		return null;
	}
	public Object newPacketPlayOutPlayerListHeaderFooter() {
		return null;
	}
	public Object newPacketPlayOutScoreboardDisplayObjective() {
		return new Packet208SetScoreboardDisplayObjective();
	}
	public Object newPacketPlayOutScoreboardObjective() {
		return new Packet206SetScoreboardObjective();
	}
	public Object newPacketPlayOutScoreboardTeam() {
		return new Packet209SetScoreboardTeam();
	}
	public Object newDataWatcher(Object entity) {
		return new DataWatcher();
	}
	public Object newPlayerInfoData(Object profile, int ping, Object enumGamemode, Object listName) {
		return null;
	}
	public Object newDataWatcherItem(me.neznamy.tab.platforms.bukkit.packets.DataWatcher.DataWatcherObject type, Object value, boolean needsUpdate) {
		WatchableObject item = new WatchableObject((int) type.classType, type.position, value);
		item.a(needsUpdate);
		return item;
	}
	public void registerDataWatcherObject(Object dataWatcher, me.neznamy.tab.platforms.bukkit.packets.DataWatcher.DataWatcherObject type, Object value) {
		((DataWatcher)dataWatcher).a(type.position, value);
	}
	public Object newEntityArmorStand() {
		return null;
	}
	public int getEntityId(Object entityliving) {
		return ((EntityLiving)entityliving).id;
	}
	public Object newPacketPlayOutEntityTeleport(Object entityliving, Location loc) {
		EntityLiving entity = (EntityLiving) entityliving;
		entity.locX = loc.getX();
		entity.locY = loc.getY();
		entity.locZ = loc.getZ();
		entity.yaw = loc.getYaw();
		entity.pitch = loc.getPitch();
		return new Packet34EntityTeleport(entity);
	}
	public Object newEntityWither() {
		return new EntityWither(((CraftWorld)Bukkit.getWorlds().get(0)).getHandle());
	}
	public Object newPacketPlayOutScoreboardScore() {
		return new Packet207SetScoreboardScore();
	}
	public Object newPacketPlayOutScoreboardScore(String removedPlayer) {
		return new Packet207SetScoreboardScore(removedPlayer);
	}
	public Object newPacketPlayOutScoreboardScore(Object action, String objectiveName, String player, int score) {
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