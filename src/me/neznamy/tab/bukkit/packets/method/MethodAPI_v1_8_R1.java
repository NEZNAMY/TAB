package me.neznamy.tab.bukkit.packets.method;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R1.CraftServer;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;
import com.mojang.authlib.GameProfile;

import io.netty.channel.Channel;
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
	public Object newPacketPlayOutEntityDestroy(int[] ids) {
		return new PacketPlayOutEntityDestroy(ids);
	}
	public Object newPacketPlayOutChat(Object chatComponent, Object position) {
		return new PacketPlayOutChat((IChatBaseComponent) chatComponent, (Byte) position);
	}
	public Object newPacketPlayOutEntityMetadata(int entityId, Object dataWatcher, boolean force) {
		return new PacketPlayOutEntityMetadata(entityId, (DataWatcher) dataWatcher, force);
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