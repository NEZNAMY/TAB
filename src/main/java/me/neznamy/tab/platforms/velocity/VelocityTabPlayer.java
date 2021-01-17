package me.neznamy.tab.platforms.velocity;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.velocitypowered.api.proxy.Player;

import io.netty.channel.Channel;
import me.neznamy.tab.shared.ITabPlayer;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.cpu.TabFeature;

/**
 * TabPlayer for Velocity
 */
public class VelocityTabPlayer extends ITabPlayer{

	//the velocity player
	private Player player;
	
	//offline uuid used in tablist
	private UUID offlineId;

	/**
	 * Constructs new instance for given player
	 * @param p - velocity player
	 * @throws Exception - when reflection fails
	 */
	public VelocityTabPlayer(Player p) throws Exception {
		player = p;
		if (p.getCurrentServer().isPresent()) {
			world = p.getCurrentServer().get().getServerInfo().getName();
		} else {
			//tab reload while a player is connecting, how unfortunate
			world = "<null>";
		}
		Object minecraftConnection = player.getClass().getMethod("getConnection").invoke(player);
		channel = (Channel) minecraftConnection.getClass().getMethod("getChannel").invoke(minecraftConnection);
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
		if (nmsPacket != null && player.isActive()) channel.writeAndFlush(nmsPacket, channel.voidPromise());
	}
	
	@Override
	public void sendPacket(Object nmsPacket, TabFeature feature) {
		if (nmsPacket != null && player.isActive()) {
			channel.writeAndFlush(nmsPacket, channel.voidPromise());
			TAB.getInstance().getCPUManager().packetSent(feature);
		}
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
	public boolean hasInvisibilityPotion() {
		Main.plm.requestAttribute(this, "invisible");
		if (!attributes.containsKey("invisible")) return false;
		return Boolean.parseBoolean(attributes.get("invisible"));
	}
}