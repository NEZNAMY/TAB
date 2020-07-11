package me.neznamy.tab.platforms.bukkit;

import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import de.robingrether.idisguise.api.DisguiseAPI;
import io.netty.channel.Channel;
import me.neznamy.tab.platforms.bukkit.packets.method.MethodAPI;
import me.neznamy.tab.shared.Configs;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.PluginHooks;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
import me.neznamy.tab.shared.packets.IChatBaseComponent;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.EnumGamemode;
import me.neznamy.tab.shared.packets.PacketPlayOutPlayerInfo.PlayerInfoData;
import me.neznamy.tab.shared.placeholders.Placeholders;

public class TabPlayer extends ITabPlayer{

	private Player player;

	public TabPlayer(Player p) throws Exception {
		player = p;
		world = p.getWorld().getName();
		channel = (Channel) MethodAPI.getInstance().getChannel(player);
		tablistId = p.getUniqueId();
		uniqueId = p.getUniqueId();
		name = p.getName();
		version = ProtocolVersion.fromNumber(getProtocolVersion());
		init();
	}
	private int getProtocolVersion() {
		if (PluginHooks.protocolsupport){
			int version = getProtocolVersionPS();
			if (version < ProtocolVersion.SERVER_VERSION.getNetworkId()) return version;
		}
		if (PluginHooks.viaversion) {
			return getProtocolVersionVia();
		}
		return ProtocolVersion.SERVER_VERSION.getNetworkId();
	}
	private int getProtocolVersionPS(){
		try {
			Object protocolVersion = Class.forName("protocolsupport.api.ProtocolSupportAPI").getMethod("getProtocolVersion", Player.class).invoke(null, getBukkitEntity());
			int ver = (int) protocolVersion.getClass().getMethod("getId").invoke(protocolVersion);
			Shared.debug("ProtocolSupport returned protocol version " + ver + " for player " + getName());
			return ver;
		} catch (Throwable e) {
			return Shared.errorManager.printError(ProtocolVersion.SERVER_VERSION.getNetworkId(), "Failed to get protocol version of " + getName() + " using ProtocolSupport", e);
		}
	}
	private int getProtocolVersionVia(){
		try {
			Object viaAPI = Class.forName("us.myles.ViaVersion.api.Via").getMethod("getAPI").invoke(null);
			int ver = (int) viaAPI.getClass().getMethod("getPlayerVersion", UUID.class).invoke(viaAPI, getUniqueId());
			Shared.debug("ViaVersion returned protocol version " + ver + " for player " + getName());
			return ver;
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
		int ping = MethodAPI.getInstance().getPing(player);
		if (ping > 10000 || ping < 0) ping = -1;
		return ping;
	}
	@Override
	public void sendPacket(Object nmsPacket) {
		if (nmsPacket != null) MethodAPI.getInstance().sendPacket(player, nmsPacket);
	}
	@Override
	public void sendMessage(String message) {
		if (message == null || message.length() == 0) return;
		player.sendMessage(Placeholders.color(message));
	}
	@Override
	public void sendRawMessage(String message) {
		if (message == null || message.length() == 0) return;
		player.sendMessage(message);
	}
	@Override
	public boolean hasInvisibility() {
		return player.hasPotionEffect(PotionEffectType.INVISIBILITY);
	}
	@Override
	public boolean getTeamPush() {
		if (PluginHooks.libsDisguises && isDisguisedLD()) return false;
		if (PluginHooks.idisguise != null && ((DisguiseAPI)PluginHooks.idisguise).isDisguised(player)) return false; 
		return Configs.getCollisionRule(world);
	}
	private boolean isDisguisedLD() {
		try {
			return (boolean) Class.forName("me.libraryaddict.disguise.DisguiseAPI").getMethod("isDisguised", Entity.class).invoke(null, player);
		} catch (Exception e) {
			return Shared.errorManager.printError(false, "Failed to check disguise status of " + getName() + " using LibsDisguises", e);
		}
	}
	@Override
	public Object getSkin() {
		return null;
	}
	@Override
	public PlayerInfoData getInfoData() {
		String name = player.getPlayerListName().equals(getName()) ? null : player.getPlayerListName();
		return new PlayerInfoData(this.name, tablistId, null, 0, EnumGamemode.CREATIVE, new IChatBaseComponent(name));
	}
	@Override
	public Player getBukkitEntity() {
		return player;
	}
}