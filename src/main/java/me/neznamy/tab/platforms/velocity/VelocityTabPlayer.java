package me.neznamy.tab.platforms.velocity;

import java.util.HashMap;
import java.util.Map;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;

import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.placeholders.Placeholders;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;

/**
 * TabPlayer for Velocity
 */
public class VelocityTabPlayer extends ITabPlayer{

	//the velocity player
	private Player player;
	
	//player's attributes on bukkit server (disguise status, invisibility status)
	private Map<String, String> attributes = new HashMap<String, String>();

	/**
	 * Constructs new instance for given player
	 * @param p - velocity player
	 */
	public VelocityTabPlayer(Player p) {
		player = p;
		world = p.getCurrentServer().get().getServerInfo().getName();
		channel = ((ConnectedPlayer)player).getConnection().getChannel();
		uniqueId = p.getUniqueId();
		name = p.getUsername();
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
	public void sendMessage(String message, boolean translateColors) {
		if (message == null || message.length() == 0) return;
		player.sendMessage(Identity.nil(), Component.text(translateColors ? Placeholders.color(message): message));
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