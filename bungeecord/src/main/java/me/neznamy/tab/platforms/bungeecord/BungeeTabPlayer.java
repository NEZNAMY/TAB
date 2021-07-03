package me.neznamy.tab.platforms.bungeecord;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.myzelyam.api.vanish.BungeeVanishAPI;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PluginMessageHandler;
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
	
	/**
	 * Initializes fields to get player channel later
	 */
	static {
		try {
			Field f = Protocol.class.getDeclaredField("TO_CLIENT");
			f.setAccessible(true);
			directionData = f.get(Protocol.GAME);
			getId = directionData.getClass().getDeclaredMethod("getId", Class.class, int.class);
			getId.setAccessible(true);
			wrapperField = InitialHandler.class.getDeclaredField("ch");
			wrapperField.setAccessible(true);
		} catch (Exception e) {
			TAB.getInstance().getErrorManager().criticalError("Failed to initialize fields for packet analysis", e);
		}
	}
	
	//proxy player
	private ProxiedPlayer player;

	/**
	 * Constructs new instance for given player
	 * @param p - velocity player
	 * @throws IllegalAccessException 
	 */
	public BungeeTabPlayer(ProxiedPlayer p, PluginMessageHandler plm) throws IllegalAccessException {
		super (plm);
		player = p;
		if (p.getServer() != null) {
			world = p.getServer().getInfo().getName();
		} else {
			world = "-";
		}
		channel = ((ChannelWrapper) wrapperField.get(player.getPendingConnection())).getHandle();
		uniqueId = p.getUniqueId();
		name = p.getName();
		init();
	}
	
	@Override
	public boolean hasPermission0(String permission) {
		return player.hasPermission(permission);
	}
	
	@Override
	public long getPing() {
		return player.getPing();
	}
	
	@Override
	public void sendPacket(Object nmsPacket) {
		if (nmsPacket != null && player.isConnected()) player.unsafe().sendPacket((DefinedPacket) nmsPacket);
	}
	
	@Override
	public Object getSkin() {
		LoginResult loginResult = ((InitialHandler)player.getPendingConnection()).getLoginProfile();
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
		return player;
	}
	
	/**
	 * Returns packet ID for this player of provided packet class
	 * @param clazz - packet class
	 * @return - packet ID
	 */
	public int getPacketId(Class<? extends DefinedPacket> clazz) {
		try {
			return (int) getId.invoke(directionData, clazz, player.getPendingConnection().getVersion());
		} catch (Exception e) {
			TAB.getInstance().getErrorManager().printError("Failed to get packet id for packet " + clazz + " with client version " + player.getPendingConnection().getVersion(), e);
			return -1;
		}
	}
	
	@Override
	public ProtocolVersion getVersion() {
		return ProtocolVersion.fromNetworkId(player.getPendingConnection().getVersion());
	}
	
	@Override
	public boolean isVanished() {
		if (ProxyServer.getInstance().getPluginManager().getPlugin("PremiumVanish") != null && BungeeVanishAPI.isInvisible(player)) return true;
		return super.isVanished();
	}

	@Override
	public boolean isOnline() {
		return player.isConnected();
	}

	@Override
	public int getGamemode() {
		return ((UserConnection)player).getGamemode();
	}
}