package me.neznamy.tab.platforms.bungee;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import de.myzelyam.api.vanish.BungeeVanishAPI;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.Shared;
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
public class BungeeTabPlayer extends ITabPlayer {
	
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
			(wrapperField = InitialHandler.class.getDeclaredField("ch")).setAccessible(true);
		} catch (Exception e) {
			Shared.errorManager.criticalError("Failed to initialize fields for packet analysis", e);
		}
	}
	
	//proxy player
	private ProxiedPlayer player;
	
	//player's attributes on bukkit server (disguise status, invisibility status)
	private Map<String, String> attributes = new HashMap<String, String>();

	/**
	 * Constructs new instance for given player
	 * @param p - velocity player
	 * @throws Exception - if reflection fails
	 */
	public BungeeTabPlayer(ProxiedPlayer p) throws Exception {
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
	public ProxiedPlayer getPlayer() {
		return player;
	}
	
	public int getPacketId(Class<? extends DefinedPacket> clazz) {
		try {
			return (int) getId.invoke(directionData, clazz, player.getPendingConnection().getVersion());
		} catch (Exception e) {
			Shared.errorManager.printError("Failed to get packet id for packet " + clazz + " with client version " + player.getPendingConnection().getVersion(), e);
			return -1;
		}
	}
	
	@Override
	public ProtocolVersion getVersion() {
		return ProtocolVersion.fromNetworkId(player.getPendingConnection().getVersion());
	}
	
	@Override
	public boolean isVanished() {
		return ProxyServer.getInstance().getPluginManager().getPlugin("PremiumVanish") != null && BungeeVanishAPI.isInvisible(player);
	}
	
	@Override
	public boolean isDisguised() {
		Main.plm.requestAttribute(this, "disguised");
		if (!attributes.containsKey("disguised")) return false;
		return Boolean.parseBoolean(attributes.get("disguised"));
	}
	
	public void setAttribute(String attribute, String value) {
		attributes.put(attribute, value);
	}

	@Override
	public boolean hasInvisibilityPotion() {
		Main.plm.requestAttribute(this, "invisible");
		if (!attributes.containsKey("invisible")) return false;
		return Boolean.parseBoolean(attributes.get("invisible"));
	}
}