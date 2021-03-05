package me.neznamy.tab.platforms.bukkit;

import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.potion.PotionEffectType;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import us.myles.ViaVersion.api.Via;

/**
 * TabPlayer for Bukkit
 */
public class BukkitTabPlayer extends ITabPlayer {

	private Player player;
	private Object playerConnection;

	public BukkitTabPlayer(Player p){
		player = p;
		world = p.getWorld().getName();
		try {
			playerConnection = NMSStorage.getInstance().PLAYER_CONNECTION.get(NMSStorage.getInstance().getHandle.invoke(player));
		} catch (Exception e) {
			TAB.getInstance().getErrorManager().printError("Failed to get playerConnection of " + p.getName(), e);
		}
		try {
			if (NMSStorage.getInstance().CHANNEL != null)
				channel = (Channel) NMSStorage.getInstance().CHANNEL.get(NMSStorage.getInstance().NETWORK_MANAGER.get(playerConnection));
		} catch (Exception e) {
			TAB.getInstance().getErrorManager().printError("Failed to get channel of " + p.getName(), e);
		}
		uniqueId = p.getUniqueId();
		name = p.getName();
		version = ProtocolVersion.fromNetworkId(getProtocolVersion());
		init();
	}

	private int getProtocolVersion() {
		if (Bukkit.getPluginManager().isPluginEnabled("ProtocolSupport")){
			int version = getProtocolVersionPS();
			//some PS versions return -1 on unsupported server versions instead of throwing exception
			if (version != -1 && version < ProtocolVersion.SERVER_VERSION.getNetworkId()) return version;
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
			TAB.getInstance().debug("ProtocolSupport returned protocol version " + version + " for " + getName());
			return version;
		} catch (Throwable e) {
			return TAB.getInstance().getErrorManager().printError(ProtocolVersion.SERVER_VERSION.getNetworkId(), "Failed to get protocol version of " + getName() + " using ProtocolSupport", e);
		}
	}

	private int getProtocolVersionVia(){
		try {
			int version = Via.getAPI().getPlayerVersion(uniqueId);
			TAB.getInstance().debug("ViaVersion returned protocol version " + version + " for " + getName());
			return version;
		} catch (Throwable e) {
			return TAB.getInstance().getErrorManager().printError(ProtocolVersion.SERVER_VERSION.getNetworkId(), "Failed to get protocol version of " + getName() + " using ViaVersion", e);
		}
	}

	@Override
	public boolean hasPermission(String permission) {
		return player.hasPermission(permission);
	}

	@Override
	public long getPing() {
		try {
			int ping = NMSStorage.getInstance().PING.getInt(NMSStorage.getInstance().getHandle.invoke(player));
			if (ping > 10000 || ping < 0) ping = -1;
			return ping;
		} catch (Exception e) {
			return -1;
		}
	}

	@Override
	public void sendPacket(Object nmsPacket) {
		if (nmsPacket == null || !player.isOnline()) return;
		try {
			if (Bukkit.getPluginManager().isPluginEnabled("ViaVersion") && nmsPacket instanceof ByteBuf) {
				Via.getAPI().sendRawPacket(uniqueId, (ByteBuf) nmsPacket);
				return;
			}
			NMSStorage.getInstance().sendPacket.invoke(playerConnection, nmsPacket);
		} catch (Throwable e) {
			TAB.getInstance().getErrorManager().printError("An error occurred when sending " + nmsPacket.getClass().getSimpleName(), e);
		}
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
		} catch (Throwable e) {
			return TAB.getInstance().getErrorManager().printError(false, "Failed to check disguise status using LibsDisguises", e);
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
		} catch (Throwable e) {
			return TAB.getInstance().getErrorManager().printError(false, "Failed to check disguise status using iDisguise", e);
		}
	}

	@Override
	public Object getSkin() {
		try {
			return Class.forName("com.mojang.authlib.GameProfile").getMethod("getProperties").invoke(NMSStorage.getInstance().getProfile.invoke(NMSStorage.getInstance().getHandle.invoke(player)));
		} catch (Throwable e) {
			return TAB.getInstance().getErrorManager().printError(null, "Failed to get skin of " + getName(), e);
		}
	}

	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public boolean isOnline() {
		return player.isOnline();
	}
}