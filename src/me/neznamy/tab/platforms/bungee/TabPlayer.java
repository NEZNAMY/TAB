package me.neznamy.tab.platforms.bungee;

import java.lang.reflect.Field;

import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOut;
import me.neznamy.tab.shared.*;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.DefinedPacket;

public class TabPlayer extends ITabPlayer{

	public ProxiedPlayer player;

	public TabPlayer(ProxiedPlayer p) throws Exception {
		player = p;
		world = p.getServer().getInfo().getName();
		channel = ((ChannelWrapper) wrapperField.get(player.getPendingConnection())).getHandle();
		tablistId = p.getUniqueId();
		init(p.getName(), p.getUniqueId());
		version = ProtocolVersion.fromNumber(player.getPendingConnection().getVersion());
	}
	public String getGroupFromPermPlugin() {
		try {
			if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) return PluginHooks.LuckPerms_getPrimaryGroup(this);
			if (ProxyServer.getInstance().getPluginManager().getPlugin("BungeePerms") != null) return PluginHooks.BungeePerms_getMainGroup(this);
		} catch (Throwable ex) {
			Shared.error(null, "Failed to get permission group of " + player.getName() + " (permission plugin: " + Shared.mainClass.getPermissionPlugin() + ")", ex);
		}
		return player.getGroups().toArray(new String[0])[0];
	}
	public String[] getGroupsFromPermPlugin() {
		return new String[] {getGroupFromPermPlugin()};
	}
	public boolean hasPermission(String permission) {
		return player.hasPermission(permission);
	}
	public long getPing() {
		return player.getPing();
	}
	public void sendPacket(Object nmsPacket) {
		if (nmsPacket != null) player.unsafe().sendPacket((DefinedPacket) nmsPacket);
	}
	@SuppressWarnings("deprecation")
	public void sendMessage(String message) {
		if (message == null || message.length() == 0) return;
		player.sendMessage(message);
	}
	private static final Field wrapperField = PacketPlayOut.getFields(InitialHandler.class).get("ch");
}