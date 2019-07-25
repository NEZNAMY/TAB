package me.neznamy.tab.bukkit.packets.method;

import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;

import io.netty.channel.Channel;
import me.neznamy.tab.bukkit.packets.NMSClass;
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
	public abstract Object newPacketPlayOutEntityDestroy(int[] ids);
	public abstract Object newPacketPlayOutChat(Object chatComponent, Object position);

	public double getTPS() {
		if (!spigot) return -1;
		return getRecentTps()[0];
	}

	static {
		try {
			instance = (MethodAPI) Class.forName(MethodAPI.class.getPackage().getName()+".MethodAPI_" + NMSClass.version).getConstructor().newInstance();
			try {
				Class.forName("org.spigotmc.SpigotConfig");
				spigot = true;
			} catch (Exception e) {
				spigot = false;
			}
		} catch (Exception e) {
			Shared.error("Failed to initialize MethodAPI class", e);
		}
	}
}