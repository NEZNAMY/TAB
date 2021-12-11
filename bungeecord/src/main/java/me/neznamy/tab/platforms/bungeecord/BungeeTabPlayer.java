package me.neznamy.tab.platforms.bungeecord;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.myzelyam.api.vanish.BungeeVanishAPI;
import me.neznamy.tab.api.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.proxy.ProxyTabPlayer;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;

/**
 * TabPlayer for BungeeCord
 */
public class BungeeTabPlayer extends ProxyTabPlayer {
	
	//bungee internals to get player channel
	private static Field wrapperField;
	private static Object directionData;
	private static Method getId;
	
	static {
		try {
			Field f = Protocol.class.getDeclaredField("TO_CLIENT");
			f.setAccessible(true);
			directionData = f.get(Protocol.GAME);
			getId = directionData.getClass().getDeclaredMethod("getId", Class.class, int.class);
			getId.setAccessible(true);
			wrapperField = InitialHandler.class.getDeclaredField("ch");
			wrapperField.setAccessible(true);
		} catch (ReflectiveOperationException e) {
			TAB.getInstance().getErrorManager().criticalError("Failed to initialize fields for packet analysis", e);
		}
	}

	/**
	 * Constructs new instance for given player
	 * @param p - BungeeCord player
	 */
	public BungeeTabPlayer(ProxiedPlayer p) {
		super(p, p.getUniqueId(), p.getName(), p.getServer() != null ? p.getServer().getInfo().getName() : "-");
		try {
			channel = ((ChannelWrapper) wrapperField.get(getPlayer().getPendingConnection())).getHandle();
		} catch (IllegalAccessException e) {
			TAB.getInstance().getErrorManager().criticalError("Failed to get channel of " + getPlayer().getName(), e);
		}
	}
	
	@Override
	public boolean hasPermission0(String permission) {
		long time = System.nanoTime();
		boolean value = getPlayer().hasPermission(permission);
		TAB.getInstance().getCPUManager().addMethodTime("hasPermission", System.nanoTime()-time);
		return value;
	}
	
	@Override
	public int getPing() {
		return getPlayer().getPing();
	}
	
	@Override
	public void sendPacket(Object nmsPacket) {
		long time = System.nanoTime();
		if (nmsPacket != null && getPlayer().isConnected()) getPlayer().unsafe().sendPacket((DefinedPacket) nmsPacket);
		TAB.getInstance().getCPUManager().addMethodTime("sendPacket", System.nanoTime()-time);
	}
	
	@Override
	public Object getSkin() {
		LoginResult loginResult = ((InitialHandler)getPlayer().getPendingConnection()).getLoginProfile();
		if (loginResult == null || loginResult.getProperties() == null) return new String[0][0];
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
	public ProxiedPlayer getPlayer() {
		return (ProxiedPlayer) player;
	}
	
	/**
	 * Returns packet ID for this player of provided packet class
	 * @param clazz - packet class
	 * @return - packet ID
	 */
	public int getPacketId(Class<? extends DefinedPacket> clazz) {
		try {
			return (int) getId.invoke(directionData, clazz, getPlayer().getPendingConnection().getVersion());
		} catch (ReflectiveOperationException e) {
			TAB.getInstance().getErrorManager().printError("Failed to get packet id for packet " + clazz + " with client version " + getPlayer().getPendingConnection().getVersion(), e);
			return -1;
		}
	}
	
	@Override
	public ProtocolVersion getVersion() {
		return ProtocolVersion.fromNetworkId(getPlayer().getPendingConnection().getVersion());
	}
	
	@Override
	public boolean isVanished() {
		if (ProxyServer.getInstance().getPluginManager().getPlugin("PremiumVanish") != null && BungeeVanishAPI.isInvisible(getPlayer())) return true;
		return super.isVanished();
	}

	@Override
	public boolean isOnline() {
		return getPlayer().isConnected();
	}

	@Override
	public int getGamemode() {
		return ((UserConnection)player).getGamemode();
	}
}