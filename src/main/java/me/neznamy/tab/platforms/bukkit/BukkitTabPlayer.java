package me.neznamy.tab.platforms.bukkit;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import de.robingrether.idisguise.api.DisguiseAPI;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import me.neznamy.tab.platforms.bukkit.nms.NMSHook;
import me.neznamy.tab.platforms.bukkit.nms.PacketPlayOut;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.config.Configs;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;
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
			return (int) protocolVersion.getClass().getMethod("getId").invoke(protocolVersion);
		} catch (Throwable e) {
			return Shared.errorManager.printError(ProtocolVersion.SERVER_VERSION.getNetworkId(), "Failed to get protocol version of " + getName() + " using ProtocolSupport", e);
		}
	}

	private int getProtocolVersionVia(){
		try {
			return Via.getAPI().getPlayerVersion(uniqueId);
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
	public boolean hasInvisibility() {
		return player.hasPotionEffect(PotionEffectType.INVISIBILITY);
	}

	@Override
	public boolean getTeamPush() {
		return !isDisguised() && Configs.getCollisionRule(world);
	}
	
	public boolean isDisguised() {
		if (Bukkit.getPluginManager().isPluginEnabled("LibsDisguises") && me.libraryaddict.disguise.DisguiseAPI.isDisguised(player)) return true;
		if (PluginHooks.idisguise != null && ((DisguiseAPI)PluginHooks.idisguise).isDisguised(player)) return true;
		return false;
	}

	@Override
	public Object getSkin() {
		return null;
	}

	@Override
	public PlayerInfoData getInfoData() {
		String name = player.getPlayerListName().equals(getName()) ? null : player.getPlayerListName();
		return new PlayerInfoData(this.name, uniqueId, null, 0, EnumGamemode.CREATIVE, new IChatBaseComponent(name));
	}

	@Override
	public Player getPlayer() {
		return player;
	}
}