package me.neznamy.tab.platforms.velocity;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;

/**
 * TabPlayer for Velocity
 */
public class VelocityTabPlayer extends ITabPlayer{

	//the velocity player
	private Player player;
	
	//offline uuid used in tablist
	private UUID offlineId;
	
	//player's attributes on bukkit server (disguise status, invisibility status)
	private Map<String, String> attributes = new HashMap<String, String>();

	/**
	 * Constructs new instance for given player
	 * @param p - velocity player
	 */
	public VelocityTabPlayer(Player p) {
		player = p;
		if (p.getCurrentServer().isPresent()) {
			world = p.getCurrentServer().get().getServerInfo().getName();
		} else {
			//tab reload while a player is connecting, how unfortunate
			world = "<null>";
		}
		channel = ((ConnectedPlayer)player).getConnection().getChannel();
		name = p.getUsername();
		uniqueId = p.getUniqueId();
		offlineId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
		version = ProtocolVersion.fromNetworkId(player.getProtocolVersion().getProtocol());
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
		if (nmsPacket != null) ((ConnectedPlayer)player).getConnection().write(nmsPacket);
	}
	
	@Override
	public Object getSkin() {
		return player.getGameProfile().getProperties();
	}
	
	@Override
	public Player getPlayer() {
		return player;
	}
	
	@Override
	public UUID getTablistUUID() {
		return offlineId;
	}

	@Override
	public boolean isDisguised() {
		Main.plm.requestAttribute(this, "disguised");
		if (!attributes.containsKey("disguised")) return false;
		return Boolean.parseBoolean(attributes.get("disguised"));
	}
	
	@Override
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