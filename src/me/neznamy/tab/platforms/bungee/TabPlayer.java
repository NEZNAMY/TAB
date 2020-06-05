package me.neznamy.tab.platforms.bungee;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import me.neznamy.tab.platforms.bukkit.packets.PacketPlayOut;
import me.neznamy.tab.shared.*;
import me.neznamy.tab.shared.placeholders.Placeholders;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;

public class TabPlayer extends ITabPlayer{
	
	private static final Field wrapperField = PacketPlayOut.getFields(InitialHandler.class).get("ch");
	private static Object directionData;
	private static Method getId;
	
	static {
		try {
			Field f = Protocol.class.getDeclaredField("TO_CLIENT");
			f.setAccessible(true);
			directionData = f.get(Protocol.GAME);
			getId = directionData.getClass().getDeclaredMethod("getId", Class.class, int.class);
			getId.setAccessible(true);
		} catch (Exception e) {
			Shared.errorManager.criticalError("Failed to initialize fields for packet analysis", e);
		}
	}
	
	private ProxiedPlayer player;

	public TabPlayer(ProxiedPlayer p) throws Exception {
		player = p;
		if (p.getServer() != null) {
			world = p.getServer().getInfo().getName();
		} else {
			world = "-";
		}
		channel = ((ChannelWrapper) wrapperField.get(player.getPendingConnection())).getHandle();
		tablistId = p.getUniqueId();
		uniqueId = p.getUniqueId();
		name = p.getName();
		version = ProtocolVersion.fromNumber(player.getPendingConnection().getVersion());
		init();
	}
	@Override
	public String getGroupFromPermPlugin() {
		if (PluginHooks.luckPerms) return PluginHooks.LuckPerms_getPrimaryGroup(this);
		if (PluginHooks.ultrapermissions) {
			String[] groups = PluginHooks.UltraPermissions_getAllGroups(this);
			if (groups.length == 0) return "null";
			return groups[0];
		}
		if (ProxyServer.getInstance().getPluginManager().getPlugin("BungeePerms") != null) return PluginHooks.BungeePerms_getMainGroup(this);
		String[] groups = player.getGroups().toArray(new String[0]);
		return groups[groups.length-1];
	}
	@Override
	public String[] getGroupsFromPermPlugin() {
		return new String[] {getGroupFromPermPlugin()};
	}
	@Override
	public boolean hasPermission(String permission) {
		return player.hasPermission(permission);
	}
	@Override
	public long getPing() {
		return player.getPing();
	}
	@Override
	public void sendPacket(Object nmsPacket) {
		if (nmsPacket != null) player.unsafe().sendPacket((DefinedPacket) nmsPacket);
	}
	@Override
	@SuppressWarnings("deprecation")
	public void sendMessage(String message) {
		if (message == null || message.length() == 0) return;
		player.sendMessage(Placeholders.color(message));
	}
	@Override
	@SuppressWarnings("deprecation")
	public void sendRawMessage(String message) {
		if (message == null || message.length() == 0) return;
		player.sendMessage(message);
	}
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
	@Override
	public ProxiedPlayer getBungeeEntity() {
		return player;
	}
	public int getPacketId(Class<? extends DefinedPacket> clazz) {
		try {
			return (int) getId.invoke(directionData, clazz, version.getNetworkId());
		} catch (Exception e) {
			Shared.errorManager.printError("Failed to get packet id for packet " + clazz + " with client version " + version.getFriendlyName());
			return 0;
		}
	}
}