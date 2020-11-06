package me.neznamy.tab.platforms.bukkit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.potion.PotionEffectType;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import me.neznamy.tab.platforms.bukkit.nms.NMSHook;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOut;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.placeholders.Placeholders;
import us.myles.ViaVersion.api.Via;

/**
 * TabPlayer for Bukkit
 */
public class BukkitTabPlayer extends ITabPlayer {

	private Player player;

	public BukkitTabPlayer(Player p) throws Exception {
		player = p;
		world = p.getWorld().getName();
		channel = (Channel) NMSHook.getChannel(player);
		uniqueId = p.getUniqueId();
		name = p.getName();
		version = ProtocolVersion.fromNetworkId(getProtocolVersion());
		init();
	}

	private int getProtocolVersion() {
		if (Bukkit.getPluginManager().isPluginEnabled("ProtocolSupport")){
			int version = getProtocolVersionPS();
			if (version < ProtocolVersion.SERVER_VERSION.getNetworkId()) return version;
		}
		if (Bukkit.getPluginManager().isPluginEnabled("ViaVersion")) {
			return getProtocolVersionVia();
		}
		return ProtocolVersion.SERVER_VERSION.getNetworkId();
	}

	private int getProtocolVersionPS(){
		try {
			Object protocolVersion = Class.forName("protocolsupport.api.ProtocolSupportAPI").getMethod("getProtocolVersion", Player.class).invoke(null, player);
			int version = (int) protocolVersion.getClass().getMethod("getId").invoke(protocolVersion);
			Shared.debug("ProtocolSupport returned protocol version " + version + " for " + getName());
			return version;
		} catch (Throwable e) {
			return Shared.errorManager.printError(ProtocolVersion.SERVER_VERSION.getNetworkId(), "Failed to get protocol version of " + getName() + " using ProtocolSupport", e);
		}
	}

	private int getProtocolVersionVia(){
		try {
			int version = Via.getAPI().getPlayerVersion(uniqueId);
			Shared.debug("ViaVersion returned protocol version " + version + " for " + getName());
			return version;
		} catch (Throwable e) {
			return Shared.errorManager.printError(ProtocolVersion.SERVER_VERSION.getNetworkId(), "Failed to get protocol version of " + getName() + " using ViaVersion", e);
		}
	}

	@Override
	public boolean hasPermission(String permission) {
		return player.hasPermission(permission);
	}

	@Override
	public long getPing() {
		int ping;
		try {
			ping = NMSHook.getPing(player);
		} catch (Exception e) {
			return -1;
		}
		if (ping > 10000 || ping < 0) ping = -1;
		return ping;
	}

	@Override
	public void sendPacket(Object nmsPacket) {
		if (!player.isOnline()) return;
		if (nmsPacket != null) {
			try {
				if (nmsPacket instanceof PacketPlayOut) {
					NMSHook.sendPacket(player, ((PacketPlayOut)nmsPacket).toNMS(version));
					return;
				}
				if (Bukkit.getPluginManager().isPluginEnabled("ViaVersion") && nmsPacket instanceof ByteBuf) {
					Via.getAPI().sendRawPacket(uniqueId, (ByteBuf) nmsPacket);
					return;
				}
				NMSHook.sendPacket(player, nmsPacket);
			} catch (InvocationTargetException e) {
				Shared.errorManager.printError("An error occurred when sending " + nmsPacket.getClass().getSimpleName(), e.getTargetException());
			} catch (Throwable e) {
				Shared.errorManager.printError("An error occurred when sending " + nmsPacket.getClass().getSimpleName(), e);
			}
		}
	}

	@Override
	public void sendMessage(String message, boolean translateColors) {
		if (message == null || message.length() == 0) return;
		player.sendMessage(translateColors ? Placeholders.color(message): message);
	}

	@Override
	public boolean hasInvisibilityPotion() {
		return player.hasPotionEffect(PotionEffectType.INVISIBILITY);
	}
	
	@Override
	public boolean isDisguised() {
		return isDisguisedLD() || isDisguisediDis();
	}
	
	private boolean isDisguisedLD() {
		try {
			if (!Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) return false;
			return (boolean) Class.forName("me.libraryaddict.disguise.DisguiseAPI").getMethod("isDisguised", Entity.class).invoke(null, player);
		} catch (Exception e) {
			return Shared.errorManager.printError(false, "Failed to check disguise status using LibsDisguises", e);
		}
	}
	
	private boolean isDisguisediDis() {
		try {
			if (!Bukkit.getPluginManager().isPluginEnabled("iDisguise")) return false;
			RegisteredServiceProvider<?> provider = Bukkit.getServicesManager().getRegistration(Class.forName("de.robingrether.idisguise.api.DisguiseAPI"));
			Object iDisguise = provider.getProvider();
			Method m = iDisguise.getClass().getMethod("isDisguised", Player.class);
			m.setAccessible(true);
			return (boolean) m.invoke(iDisguise, player);
		} catch (Exception e) {
			return Shared.errorManager.printError(false, "Failed to check disguise status using iDisguise", e);
		}
	}

	@Override
	public Object getSkin() {
		return null;
	}

	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public void setAttribute(String attribute, String value) {

	}
}