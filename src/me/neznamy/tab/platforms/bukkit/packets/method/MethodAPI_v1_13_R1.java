package me.neznamy.tab.platforms.bukkit.packets.method;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R1.CraftServer;
import org.bukkit.craftbukkit.v1_13_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.mojang.authlib.GameProfile;

import io.netty.channel.Channel;
import net.minecraft.server.v1_13_R1.PacketPlayOutAttachEntity;
import net.minecraft.server.v1_13_R1.PacketPlayOutEntity;
import net.minecraft.server.v1_13_R1.PacketPlayOutMount;
import net.minecraft.server.v1_13_R1.BossBattle;
import net.minecraft.server.v1_13_R1.ChatMessageType;
import net.minecraft.server.v1_13_R1.DataWatcher;
import net.minecraft.server.v1_13_R1.DataWatcherObject;
import net.minecraft.server.v1_13_R1.DataWatcherRegistry;
import net.minecraft.server.v1_13_R1.DataWatcherSerializer;
import net.minecraft.server.v1_13_R1.Entity;
import net.minecraft.server.v1_13_R1.EntityArmorStand;
import net.minecraft.server.v1_13_R1.EntityLiving;
import net.minecraft.server.v1_13_R1.EntityWither;
import net.minecraft.server.v1_13_R1.EnumChatFormat;
import net.minecraft.server.v1_13_R1.EnumGamemode;
import net.minecraft.server.v1_13_R1.IChatBaseComponent;
import net.minecraft.server.v1_13_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_13_R1.IScoreboardCriteria;
import net.minecraft.server.v1_13_R1.NBTTagCompound;
import net.minecraft.server.v1_13_R1.Packet;
import net.minecraft.server.v1_13_R1.PacketPlayInUseEntity;
import net.minecraft.server.v1_13_R1.PacketPlayOutBoss;
import net.minecraft.server.v1_13_R1.PacketPlayOutChat;
import net.minecraft.server.v1_13_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_13_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_13_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_13_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_13_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_13_R1.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_13_R1.PacketPlayOutPlayerListHeaderFooter;
import net.minecraft.server.v1_13_R1.PacketPlayOutScoreboardDisplayObjective;
import net.minecraft.server.v1_13_R1.PacketPlayOutScoreboardObjective;
import net.minecraft.server.v1_13_R1.PacketPlayOutScoreboardScore;
import net.minecraft.server.v1_13_R1.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_13_R1.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_13_R1.ScoreboardServer;
import net.minecraft.server.v1_13_R1.ScoreboardServer.Action;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class MethodAPI_v1_13_R1 extends MethodAPI {

	public MethodAPI_v1_13_R1() {
		BarColor = BossBattle.BarColor.class;
		BarStyle = BossBattle.BarStyle.class;
		ChatMessageType = ChatMessageType.class;
		DataWatcher = DataWatcher.class;
		DataWatcherRegistry = DataWatcherRegistry.class;
		Entity = Entity.class;
		EnumChatFormat = EnumChatFormat.class;
		EnumGamemode = EnumGamemode.class;
		EnumPlayerInfoAction = EnumPlayerInfoAction.class;
		EnumScoreboardAction = ScoreboardServer.Action.class;
		EnumScoreboardHealthDisplay = IScoreboardCriteria.EnumScoreboardHealthDisplay.class;
		IChatBaseComponent = IChatBaseComponent.class;
		PacketPlayInUseEntity = PacketPlayInUseEntity.class;
		PacketPlayOutPlayerInfo = PacketPlayOutPlayerInfo.class;
		PacketPlayOutBoss = PacketPlayOutBoss.class;
		PacketPlayOutPlayerListHeaderFooter = PacketPlayOutPlayerListHeaderFooter.class;
		PacketPlayOutScoreboardDisplayObjective = PacketPlayOutScoreboardDisplayObjective.class;
		PacketPlayOutScoreboardObjective = PacketPlayOutScoreboardObjective.class;
		PacketPlayOutScoreboardScore = PacketPlayOutScoreboardScore.class;
		PacketPlayOutScoreboardTeam = PacketPlayOutScoreboardTeam.class;
		PacketPlayOutEntityMetadata = PacketPlayOutEntityMetadata.class;
		PacketPlayOutSpawnEntityLiving = PacketPlayOutSpawnEntityLiving.class;
		PacketPlayOutBoss_Action = PacketPlayOutBoss.Action.class;
		PacketPlayOutNamedEntitySpawn = PacketPlayOutNamedEntitySpawn.class;
		PacketPlayOutEntityDestroy = PacketPlayOutEntityDestroy.class;
		PacketPlayOutEntityTeleport = PacketPlayOutEntityTeleport.class;
		PacketPlayOutEntity = PacketPlayOutEntity.class;
		PacketPlayOutMount = PacketPlayOutMount.class;
		PacketPlayOutAttachEntity = PacketPlayOutAttachEntity.class;
		PlayerInfoData = PacketPlayOutPlayerInfo.PlayerInfoData.class;
	}
	public Object stringToComponent(String string) {
		if (string == null) return null;
		return ChatSerializer.a(string);
	}
	public String componentToString(Object component) {
		if (component == null) return null;
		return ChatSerializer.a((IChatBaseComponent) component);
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
		return new PacketPlayOutChat((IChatBaseComponent) chatComponent, (ChatMessageType) position);
	}
	public Object newPacketPlayOutEntityMetadata(int entityId, Object dataWatcher, boolean force) {
		return new PacketPlayOutEntityMetadata(entityId, (DataWatcher) dataWatcher, force);
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
	public Object newPlayerInfoData(Object profile, int ping, Object enumGamemode, Object listName) {
		return new PacketPlayOutPlayerInfo().new PlayerInfoData((GameProfile) profile, ping, (EnumGamemode)enumGamemode, (IChatBaseComponent) listName);
	}
	public Object newDataWatcherItem(me.neznamy.tab.platforms.bukkit.packets.DataWatcher.DataWatcherObject type, Object value, boolean needsUpdate) {
		DataWatcher.Item item = new DataWatcher.Item(new DataWatcherObject(type.position, (DataWatcherSerializer) type.classType), value);
		item.a(needsUpdate);
		return item;
	}
	public void registerDataWatcherObject(Object dataWatcher, me.neznamy.tab.platforms.bukkit.packets.DataWatcher.DataWatcherObject type, Object value) {
		((DataWatcher)dataWatcher).register(new DataWatcherObject(type.position, (DataWatcherSerializer) type.classType), value);
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
	public Object newEntityWither() {
		return new EntityWither(((CraftWorld)Bukkit.getWorlds().get(0)).getHandle());
	}
	public Object newPacketPlayOutScoreboardScore() {
		return new PacketPlayOutScoreboardScore();
	}
	public Object newPacketPlayOutScoreboardScore(String removedPlayer) {
		return null;
	}
	public Object newPacketPlayOutScoreboardScore(Object action, String objectiveName, String player, int score) {
		return new PacketPlayOutScoreboardScore((Action) action, objectiveName, player, score);
	}
	public List getDataWatcherItems(Object dataWatcher) {
		return ((DataWatcher)dataWatcher).c();
	}
	public me.neznamy.tab.platforms.bukkit.packets.DataWatcher.Item readDataWatcherItem(Object nmsItem) {
		DataWatcher.Item i = (DataWatcher.Item) nmsItem;
		int position = i.a().a();
		Object classType = i.a().b();
		Object value = i.b();
		boolean needsUpdate = i.c();
		me.neznamy.tab.platforms.bukkit.packets.DataWatcher.DataWatcherObject key = new me.neznamy.tab.platforms.bukkit.packets.DataWatcher.DataWatcherObject(position, classType);
		return new me.neznamy.tab.platforms.bukkit.packets.DataWatcher.Item(key, value).setNeedsUpdate(needsUpdate);
	}
	public String serialize(ItemStack item) {
		return CraftItemStack.asNMSCopy(item).save(new NBTTagCompound()).toString();
	}
}