package me.neznamy.tab.platforms.bungee;

import java.lang.reflect.Field;

import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOut;
import me.neznamy.tab.shared.*;
import me.neznamy.tab.shared.placeholders.Placeholders;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.DefinedPacket;

public class TabPlayer extends ITabPlayer{

	public ProxiedPlayer player;

	public TabPlayer(ProxiedPlayer p) throws Exception {
		player = p;
		world = p.getServer().getInfo().getName();
		channel = ((ChannelWrapper) wrapperField.get(player.getPendingConnection())).getHandle();
		tablistId = p.getUniqueId();
		uniqueId = p.getUniqueId();
		name = p.getName();
		version = ProtocolVersion.fromNumber(player.getPendingConnection().getVersion());
		init();
	}
	public String getGroupFromPermPlugin() {
		if (ProxyServer.getInstance().getPluginManager().getPlugin("LuckPerms") != null) return PluginHooks.LuckPerms_getPrimaryGroup(this);
		if (ProxyServer.getInstance().getPluginManager().getPlugin("BungeePerms") != null) return PluginHooks.BungeePerms_getMainGroup(this);
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
		player.sendMessage(Placeholders.color(message));
	}
	private static final Field wrapperField = PacketPlayOut.getFields(InitialHandler.class).get("ch");

	@Override
	public Object getSkin() {
		LoginResult loginResult = ((InitialHandler)player.getPendingConnection()).getLoginProfile();
		if (loginResult == null) return new String[0][0];
		String[][] s = new String[loginResult.getProperties().length][3];
		for (int i = 0;i<loginResult.getProperties().length;i++){
			LoginResult.Property pr = loginResult.getProperties()[i];
			s[i][0] = pr.getName();
			s[i][1] = pr.getValue();
			s[i][2] = pr.getSignature();
		}
		return s;
	}
}